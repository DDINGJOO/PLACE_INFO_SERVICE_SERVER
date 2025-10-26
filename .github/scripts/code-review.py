mport os
import requests
import json
import re
from github import Github
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed

def get_changed_files(pr):
    changed_files = []
    for file in pr.get_files():
        if file.filename.endswith(('.ts')):
            changed_files.append({
                'filename': file.filename,
                'patch': file.patch,
                'status': file.status,
            })
    return changed_files

def get_file_content(repo, file_path, ref):
    return repo.get_contents(file_path, ref=ref).decoded_content.decode('utf-8')

def search_file(repo, file, changed_files, ref):
    if file.type == 'file' and file.name.endswith(('.ts', '.js')):
        content = get_file_content(repo, file.path, ref)
        related = set()
        for changed_file in changed_files:
            changed_name = os.path.splitext(os.path.basename(changed_file['filename']))[0]
            if re.search(r'\b' + re.escape(changed_name) + r'\b', content):
                related.add(changed_file['filename'])
        return file.path, related
    return None, set()

def find_related_files(repo, changed_files, ref):
    related_files = defaultdict(set)
    all_files = repo.get_contents('', ref=ref)
    dirs_to_process = [file for file in all_files if file.type == 'dir']

    with ThreadPoolExecutor(max_workers=10) as executor:
        future_to_file = {executor.submit(search_file, repo, file, changed_files, ref): file for file in all_files if file.type == 'file'}

        while dirs_to_process:
            dir_files = repo.get_contents(dirs_to_process.pop().path, ref=ref)
            dirs_to_process.extend([file for file in dir_files if file.type == 'dir'])
            future_to_file.update({executor.submit(search_file, repo, file, changed_files, ref): file for file in dir_files if file.type == 'file'})

        for future in as_completed(future_to_file):
            file_path, related = future.result()
            if related:
                for changed_file in related:
                    related_files[changed_file].add(file_path)

    return related_files


def call_claude_api(changes, related_files):
    url = "https://api.anthropic.com/v1/messages"
    headers = {
        "Content-Type": "application/json",
        "x-api-key": os.environ['CLAUDE_API_KEY'],
        "anthropic-version": "2023-06-01"
    }

    system_content = (
        "경험 많은 시니어 개발자로서, 다음 변경사항들에 대해 전체적이고 간결한 코드 리뷰를 수행해주세요.\n\n"
        "리뷰 지침:\n"
        "1. 모든 변경사항을 종합적으로 검토하고, 가장 중요한 문제점이나 개선사항에만 집중하세요.\n"
        "2. 파일별로 개별 리뷰를 하지 말고, 전체 변경사항에 대한 통합된 리뷰를 제공하세요.\n"
        "3. 각 주요 이슈에 대해 간단한 설명과 구체적인 개선 제안을 제시하세요.\n"
        "4. 개선 제안에는 실제 코드 예시를 포함하세요. 단, 코드 예시는 제공한 코드와 연관된 코드여야 합니다. \n"
        "5. 사소한 스타일 문제나 개인적 선호도는 무시하세요.\n"
        "6. 심각한 버그, 성능 문제, 또는 보안 취약점이 있는 경우에만 언급하세요.\n"
        "7. 전체 리뷰는 간결하게 유지하세요.\n"
        "8. 변경된 부분만 집중하여 리뷰하고, 이미 개선된 코드를 다시 지적하지 마세요.\n"
        "9. 기존에 이미 개선된 사항(예: 중복 코드 제거를 위한 함수 생성)을 인식하고 이를 긍정적으로 언급하세요.\n"
        "10. 변경된 파일과 관련된 다른 파일들에 미칠 수 있는 영향을 분석하세요.\n\n"
        "리뷰 형식:\n"
        "- 개선된 사항: [이미 개선된 부분에 대한 긍정적 언급]\n"
        "- 주요 이슈 (있는 경우에만):\n"
        "  1. [문제 설명]\n"
        "     - 제안: [개선 방안 설명]\n"
        "     ```typescript\n"
        "     // 수정된 코드 예시\n"
        "     ```\n"
        "  2. ...\n"
        "- 관련 파일에 대한 영향 분석:\n"
        "  [변경된 파일과 관련된 다른 파일들에 미칠 수 있는 잠재적 영향 설명]\n"
        "- 전반적인 의견: [1-2문장으로 요약]\n\n"
        "변경된 파일들:\n"
    )

    for file_info in changes:
        system_content += f"- {file_info['filename']} ({file_info['status']})\n"

    system_content += "\n변경 내용:\n"
    for file_info in changes:
        system_content += f"파일: {file_info['filename']}\n전체 내용:\n{file_info['full_content']}\n\n변경된 부분:\n{file_info['patch']}\n\n"

    system_content += "\n관련된 파일들:\n"
    for changed_file, related in related_files.items():
        system_content += f"- {changed_file}에 영향을 받을 수 있는 파일들:\n"
        for related_file in related:
            system_content += f"  - {related_file}\n"

    payload = {
        "model": "claude-3-5-sonnet-20240620",
        "max_tokens": 2000,
        "system": system_content,
        "messages": [
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": "제공된 모든 변경사항에 대해 통합된, 간결하고 핵심적인 코드 리뷰를 제공해주세요. 가장 중요한 이슈에만 집중하고, 각 개선 제안에는 구체적인 코드 예시를 포함해주세요. 변경된 부분만 집중하여 리뷰하고, 이미 개선된 코드를 다시 지적하지 마세요. 또한, 변경된 파일과 관련된 다른 파일들에 미칠 수 있는 잠재적 영향을 분석해주세요."
                    }
                ]
            }
        ]
    }

    response = requests.post(url, headers=headers, json=payload)
    if response.status_code == 200:
        return response.json()['content'][0]['text']
    else:
        return f"Error: API returned status code {response.status_code}"

def main():
    g = Github(os.environ['GITHUB_TOKEN'])
    repo = g.get_repo(os.environ['GITHUB_REPOSITORY'])
    pr_number = int(os.environ['PR_NUMBER'])
    pr = repo.get_pull(pr_number)

    changed_files = get_changed_files(pr)
    changes = []

    for file_info in changed_files:
        full_content = get_file_content(repo, file_info['filename'], pr.head.sha)
        file_info['full_content'] = full_content
        changes.append(file_info)

    related_files = find_related_files(repo, changed_files, pr.head.sha)
    review = call_claude_api(changes, related_files)

    pr.create_issue_comment(f"Claude의 전체 변경사항 및 관련 파일에 대한 리뷰:\n\n{review}")

if __name__ == "__main__":
    main()
