package com.teambind.placeinfoserver.place.domain.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

/**
 * 거리 Value Object
 * 거리를 미터 단위로 관리하며 다양한 단위로 변환 가능
 *
 * 불변 객체로 설계되어 안전하게 사용 가능
 */
@Getter
@EqualsAndHashCode
public class Distance implements Comparable<Distance> {

	private final int meters;

	/**
	 * Private 생성자
	 */
	private Distance(int meters) {
		if (meters < 0) {
			throw new IllegalArgumentException("거리는 음수일 수 없습니다");
		}
		this.meters = meters;
	}

	/**
	 * 미터 단위로 거리 생성
	 */
	public static Distance ofMeters(int meters) {
		return new Distance(meters);
	}

	/**
	 * 킬로미터 단위로 거리 생성
	 */
	public static Distance ofKilometers(double kilometers) {
		if (kilometers < 0) {
			throw new IllegalArgumentException("거리는 음수일 수 없습니다");
		}
		return new Distance((int) (kilometers * 1000));
	}

	/**
	 * 마일 단위로 거리 생성
	 */
	public static Distance ofMiles(double miles) {
		if (miles < 0) {
			throw new IllegalArgumentException("거리는 음수일 수 없습니다");
		}
		return new Distance((int) (miles * 1609.34));
	}

	/**
	 * 킬로미터로 변환
	 */
	public double toKilometers() {
		return meters / 1000.0;
	}

	/**
	 * 마일로 변환
	 */
	public double toMiles() {
		return meters / 1609.34;
	}

	/**
	 * 검색 반경으로 유효한지 확인
	 * 최소 1m, 최대 50km
	 */
	public boolean isValidSearchRadius() {
		return this.meters >= 1 && this.meters <= 50000;
	}

	/**
	 * 최소 거리 (1미터) 반환
	 */
	public static Distance min() {
		return Distance.ofMeters(1);
	}

	/**
	 * 최대 검색 반경 (50km) 반환
	 */
	public static Distance maxSearchRadius() {
		return Distance.ofKilometers(50);
	}

	/**
	 * 기본 검색 반경 (5km) 반환
	 */
	public static Distance defaultSearchRadius() {
		return Distance.ofKilometers(5);
	}

	/**
	 * 거리 더하기
	 */
	public Distance plus(Distance other) {
		Objects.requireNonNull(other, "다른 거리는 null일 수 없습니다");
		return new Distance(this.meters + other.meters);
	}

	/**
	 * 거리 빼기
	 */
	public Distance minus(Distance other) {
		Objects.requireNonNull(other, "다른 거리는 null일 수 없습니다");
		int result = this.meters - other.meters;
		if (result < 0) {
			throw new IllegalArgumentException("결과 거리는 음수일 수 없습니다");
		}
		return new Distance(result);
	}

	/**
	 * 거리 비교
	 */
	@Override
	public int compareTo(Distance other) {
		Objects.requireNonNull(other, "비교 대상은 null일 수 없습니다");
		return Integer.compare(this.meters, other.meters);
	}

	/**
	 * 다른 거리보다 큰지 확인
	 */
	public boolean isGreaterThan(Distance other) {
		return this.compareTo(other) > 0;
	}

	/**
	 * 다른 거리보다 작은지 확인
	 */
	public boolean isLessThan(Distance other) {
		return this.compareTo(other) < 0;
	}

	/**
	 * 사람이 읽기 쉬운 형태로 변환
	 */
	public String toHumanReadable() {
		if (meters < 1000) {
			return meters + "m";
		} else {
			double km = meters / 1000.0;
			return String.format("%.1fkm", km);
		}
	}

	@Override
	public String toString() {
		return toHumanReadable();
	}
}
