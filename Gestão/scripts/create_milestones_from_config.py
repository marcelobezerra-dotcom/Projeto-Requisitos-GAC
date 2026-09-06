#!/usr/bin/env python3
"""
Cria/atualiza milestones no GitHub a partir de Gestão/scripts/config.json
Uso:
  pip install requests
  set GITHUB_TOKEN=ghp_...
  python create_milestones_from_config.py
"""
import os, sys, json
from pathlib import Path
import requests

ROOT = Path(__file__).parent
CONFIG_PATH = ROOT / 'config.json'
BASE = 'https://api.github.com'


def load_config():
    if not CONFIG_PATH.exists():
        print('config.json não encontrado em', CONFIG_PATH)
        sys.exit(2)
    return json.loads(CONFIG_PATH.read_text(encoding='utf-8'))


def init_headers(token):
    return {
        'Authorization': f'token {token}',
        'Accept': 'application/vnd.github+json',
        'User-Agent': 'Projeto-Requisitos-Script'
    }


def get_existing_milestones(owner, repo, headers):
    url = f"{BASE}/repos/{owner}/{repo}/milestones?state=all&per_page=100"
    r = requests.get(url, headers=headers)
    if not r.ok:
        print('Falha ao obter milestones existentes:', r.status_code, r.text)
        return []
    return r.json()


def create_milestone(owner, repo, headers, title, due_on=None, description=None):
    url = f"{BASE}/repos/{owner}/{repo}/milestones"
    payload = {'title': title}
    if due_on: payload['due_on'] = due_on
    if description: payload['description'] = description
    r = requests.post(url, headers=headers, json=payload)
    if not r.ok:
        print(f"Erro criando milestone '{title}': {r.status_code} {r.text}")
        return None
    print(f"Criado milestone '{title}' (#{r.json().get('number')})")
    return r.json()


def update_milestone(owner, repo, headers, number, title=None, due_on=None, description=None):
    url = f"{BASE}/repos/{owner}/{repo}/milestones/{number}"
    payload = {}
    if title is not None: payload['title'] = title
    if due_on is not None: payload['due_on'] = due_on
    if description is not None: payload['description'] = description
    if not payload:
        return None
    r = requests.patch(url, headers=headers, json=payload)
    if not r.ok:
        print(f"Erro atualizando milestone #{number}: {r.status_code} {r.text}")
        return None
    print(f"Atualizado milestone '{title}' (#{number})")
    return r.json()


def main():
    token = os.environ.get('GITHUB_TOKEN')
    if not token:
        print('Defina GITHUB_TOKEN com um token válido (perm. repo).', file=sys.stderr)
        sys.exit(1)
    cfg = load_config()
    repo_full = cfg.get('repoFull')
    if not repo_full:
        print('repoFull não definido em config.json', file=sys.stderr)
        sys.exit(2)
    owner, repo = repo_full.split('/')
    headers = init_headers(token)
    desired = cfg.get('milestones', [])
    existing = get_existing_milestones(owner, repo, headers)
    existing_map = {m['title']: m for m in existing}

    for m in desired:
        title = m.get('title')
        due_on = m.get('due_on')
        desc = m.get('description')
        if title in existing_map:
            em = existing_map[title]
            # compare and update if different
            need_update = False
            if (em.get('due_on') or '') != (due_on or ''):
                need_update = True
            if (em.get('description') or '') != (desc or ''):
                need_update = True
            if need_update:
                update_milestone(owner, repo, headers, em['number'], title=title, due_on=due_on, description=desc)
            else:
                print(f"Milestone '{title}' já existe e está atualizada (#{em['number']}).")
        else:
            create_milestone(owner, repo, headers, title, due_on=due_on, description=desc)

    print('Concluído.')

if __name__ == '__main__':
    main()
