#!/bin/bash
set -e

# ============================================
# Place Info Service - Production Deployment Script
# ============================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="docker-compose.yml"
INFRA_COMPOSE_FILE="docker-compose.infra.yml"
SERVICE_NAME="place-info-server"
HEALTH_ENDPOINT="http://localhost:9412/actuator/health"

# ============================================
# Utility Functions
# ============================================
log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }

print_banner() {
    echo -e "${BLUE}"
    echo "============================================"
    echo "  Place Info Service - Deployment"
    echo "============================================"
    echo -e "${NC}"
}

# ============================================
# Prerequisite Checks
# ============================================
check_prerequisites() {
    log_step "Checking prerequisites..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi

    # Check .env file
    if [ ! -f "$SCRIPT_DIR/.env" ]; then
        log_error ".env file not found!"
        log_info "Copy .env.example to .env and configure it:"
        log_info "  cp .env.example .env"
        exit 1
    fi

    log_info "Prerequisites check passed"
}

# ============================================
# Network Setup
# ============================================
ensure_network() {
    log_step "Ensuring infra-network exists..."

    if ! docker network inspect infra-network &> /dev/null; then
        log_info "Creating infra-network..."
        docker network create infra-network
    else
        log_info "infra-network already exists"
    fi
}

# ============================================
# Infrastructure Deployment
# ============================================
deploy_infra() {
    log_step "Deploying shared infrastructure (Kafka, Redis, Zipkin)..."

    cd "$SCRIPT_DIR"

    # Use docker-compose or docker compose based on availability
    if command -v docker-compose &> /dev/null; then
        docker-compose -f $INFRA_COMPOSE_FILE up -d
    else
        docker compose -f $INFRA_COMPOSE_FILE up -d
    fi

    log_info "Waiting for infrastructure to be ready..."
    sleep 15

    # Check Kafka health
    log_info "Checking Kafka cluster health..."
    for i in {1..30}; do
        if docker exec kafka1 kafka-broker-api-versions.sh --bootstrap-server localhost:9091 &> /dev/null; then
            log_info "Kafka cluster is healthy"
            break
        fi
        if [ $i -eq 30 ]; then
            log_warn "Kafka health check timed out, but continuing..."
        fi
        sleep 2
    done

    # Check Zipkin health
    log_info "Checking Zipkin health..."
    for i in {1..15}; do
        if curl -sf http://localhost:9411/health &> /dev/null; then
            log_info "Zipkin is healthy"
            break
        fi
        sleep 2
    done

    log_info "Infrastructure deployment completed"
}

# ============================================
# Application Deployment
# ============================================
deploy_app() {
    log_step "Deploying Place Info Service..."

    cd "$SCRIPT_DIR"

    # Pull latest images
    log_info "Pulling latest images..."
    if command -v docker-compose &> /dev/null; then
        docker-compose -f $COMPOSE_FILE pull
        docker-compose -f $COMPOSE_FILE up -d
    else
        docker compose -f $COMPOSE_FILE pull
        docker compose -f $COMPOSE_FILE up -d
    fi

    log_info "Application deployment initiated"
}

# ============================================
# Health Check
# ============================================
health_check() {
    log_step "Running health checks..."

    local max_attempts=60
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -sf "$HEALTH_ENDPOINT" > /dev/null 2>&1; then
            log_info "Application is healthy!"

            # Show detailed health status
            echo ""
            log_info "Health Status:"
            curl -s "$HEALTH_ENDPOINT" | python3 -m json.tool 2>/dev/null || curl -s "$HEALTH_ENDPOINT"
            echo ""

            return 0
        fi

        echo -ne "\r${YELLOW}[WAIT]${NC} Waiting for application... ($attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done

    echo ""
    log_error "Health check failed after $max_attempts attempts"
    log_info "Checking container logs..."
    docker logs --tail 50 $SERVICE_NAME
    return 1
}

# ============================================
# Status Check
# ============================================
show_status() {
    log_step "Service Status:"
    echo ""

    if command -v docker-compose &> /dev/null; then
        docker-compose -f $COMPOSE_FILE ps
        echo ""
        docker-compose -f $INFRA_COMPOSE_FILE ps
    else
        docker compose -f $COMPOSE_FILE ps
        echo ""
        docker compose -f $INFRA_COMPOSE_FILE ps
    fi

    echo ""
    log_info "Endpoints:"
    echo "  - Application: http://localhost:9412"
    echo "  - Health:      http://localhost:9412/actuator/health"
    echo "  - Swagger:     http://localhost:9412/swagger-ui/index.html"
    echo "  - Zipkin:      http://localhost:9411"
}

# ============================================
# Stop Services
# ============================================
stop_services() {
    log_step "Stopping services..."

    cd "$SCRIPT_DIR"

    if command -v docker-compose &> /dev/null; then
        docker-compose -f $COMPOSE_FILE down
    else
        docker compose -f $COMPOSE_FILE down
    fi

    log_info "Services stopped"
}

# ============================================
# Stop All (including infrastructure)
# ============================================
stop_all() {
    log_step "Stopping all services including infrastructure..."

    cd "$SCRIPT_DIR"

    if command -v docker-compose &> /dev/null; then
        docker-compose -f $COMPOSE_FILE down
        docker-compose -f $INFRA_COMPOSE_FILE down
    else
        docker compose -f $COMPOSE_FILE down
        docker compose -f $INFRA_COMPOSE_FILE down
    fi

    log_info "All services stopped"
}

# ============================================
# View Logs
# ============================================
show_logs() {
    local service=${1:-$SERVICE_NAME}
    log_step "Showing logs for $service..."

    docker logs -f --tail 100 $service
}

# ============================================
# Main
# ============================================
print_banner

case "${1:-deploy}" in
    infra)
        check_prerequisites
        ensure_network
        deploy_infra
        ;;
    app)
        check_prerequisites
        ensure_network
        deploy_app
        health_check
        show_status
        ;;
    deploy)
        check_prerequisites
        ensure_network
        deploy_infra
        deploy_app
        health_check
        show_status
        ;;
    stop)
        stop_services
        ;;
    stop-all)
        stop_all
        ;;
    status)
        show_status
        ;;
    health)
        health_check
        ;;
    logs)
        show_logs "${2:-}"
        ;;
    restart)
        stop_services
        deploy_app
        health_check
        show_status
        ;;
    *)
        echo "Usage: $0 {deploy|infra|app|stop|stop-all|status|health|logs|restart}"
        echo ""
        echo "Commands:"
        echo "  deploy    - Deploy infrastructure + application (default)"
        echo "  infra     - Deploy only infrastructure (Kafka, Redis, Zipkin)"
        echo "  app       - Deploy only application"
        echo "  stop      - Stop application services"
        echo "  stop-all  - Stop all services including infrastructure"
        echo "  status    - Show service status"
        echo "  health    - Run health check"
        echo "  logs      - Show application logs (use: logs [container-name])"
        echo "  restart   - Restart application services"
        exit 1
        ;;
esac

log_info "Done!"
