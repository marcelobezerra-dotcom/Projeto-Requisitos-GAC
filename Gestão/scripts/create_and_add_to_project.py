#!/usr/bin/env python3
"""
Recriação em Python do script PowerShell create_and_add_to_project.ps1
- Usa GITHUB_TOKEN (env) para autenticação
- Cria milestones, labels e issues via REST
- Adiciona issues ao Project v2 via GraphQL

Uso:
Windows PowerShell:
PS> $env:GITHUB_TOKEN='ghp_...'
PS> python .\create_and_add_to_project.py

Instalação de dependência:
pip install requests
"""

import os
import sys
import json
import textwrap
from pathlib import Path

try:
    import requests
except Exception:
    print("Módulo 'requests' não encontrado. Instale com: pip install requests", file=sys.stderr)
    sys.exit(2)

ROOT = Path(__file__).parent
CONFIG_PATH = ROOT / 'config.json'

# Defaults (same as PowerShell original)
REPO_FULL_DEFAULT = 'marcelobezerra-dotcom/Projeto-Requisitos-GAC'

HEADERS_REST = None
HEADERS_GRAPH = None
BASE = 'https://api.github.com'
GRAPHQL = BASE + '/graphql'


def load_config():
    cfg = {}
    if CONFIG_PATH.exists():
        try:
            cfg = json.loads(CONFIG_PATH.read_text(encoding='utf-8'))
        except Exception as e:
            print(f"Falha ao ler {CONFIG_PATH}: {e}", file=sys.stderr)
    return cfg


def init_headers(token):
    global HEADERS_REST, HEADERS_GRAPH
    HEADERS_REST = {
        'Authorization': f'token {token}',
        'Accept': 'application/vnd.github+json',
        'User-Agent': 'Projeto-Requisitos-Script'
    }
    HEADERS_GRAPH = {
        'Authorization': f'bearer {token}',
        'Accept': 'application/vnd.github+json',
        'User-Agent': 'Projeto-Requisitos-Script'
    }


def ensure_milestone(owner, repo, title, due_on=None, description=None):
    url = f"{BASE}/repos/{owner}/{repo}/milestones"
    resp = requests.get(url, headers=HEADERS_REST)
    if resp.ok:
        for m in resp.json():
            if m.get('title') == title:
                print(f"Milestone '{title}' já existe (#{m.get('number')}).")
                return m.get('number')
    payload = {'title': title}
    if due_on:
        payload['due_on'] = due_on
    if description:
        payload['description'] = description
    resp = requests.post(url, headers=HEADERS_REST, json=payload)
    if not resp.ok:
        print(f"Falha ao criar milestone '{title}': {resp.status_code} {resp.text}", file=sys.stderr)
        return None
    m = resp.json()
    print(f"Criado milestone '{title}' (#{m.get('number')}).")
    return m.get('number')


def repo_has_issues(owner, repo):
    url = f"{BASE}/repos/{owner}/{repo}"
    resp = requests.get(url, headers=HEADERS_REST)
    if not resp.ok:
        print(f"Falha ao obter informações do repositório: {resp.status_code} {resp.text}", file=sys.stderr)
        return False
    data = resp.json()
    return data.get('has_issues', False)


def find_issue_by_title(owner, repo, title):
    # Busca issues (abertas e fechadas) e compara pelo título
    url = f"{BASE}/repos/{owner}/{repo}/issues?state=all&per_page=100"
    resp = requests.get(url, headers=HEADERS_REST)
    if not resp.ok:
        return None
    for it in resp.json():
        if it.get('title') == title:
            return it
    return None


def update_issue(owner, repo, number, fields):
    url = f"{BASE}/repos/{owner}/{repo}/issues/{number}"
    resp = requests.patch(url, headers=HEADERS_REST, json=fields)
    if not resp.ok:
        print(f"Falha ao atualizar issue #{number}: {resp.status_code} {resp.text}", file=sys.stderr)
        return None
    return resp.json()


def ensure_label(owner, repo, name, color, description=None):
    # GET label by name
    url_get = f"{BASE}/repos/{owner}/{repo}/labels/{name}"
    resp = requests.get(url_get, headers=HEADERS_REST)
    if resp.ok:
        print(f"Label '{name}' já existe.")
        return True
    url = f"{BASE}/repos/{owner}/{repo}/labels"
    payload = {'name': name, 'color': color}
    if description:
        payload['description'] = description
    resp = requests.post(url, headers=HEADERS_REST, json=payload)
    if not resp.ok:
        print(f"Falha ao criar label '{name}': {resp.status_code} {resp.text}", file=sys.stderr)
        return False
    print(f"Criada label '{name}'.")
    return True


def create_issue_return_node(owner, repo, title, body_text=None, labels=None, milestone_number=None):
    # se já existir uma issue com o mesmo título, atualiza milestone/labels
    existing = find_issue_by_title(owner, repo, title)
    if existing:
        num = existing.get('number')
        fields = {}
        if body_text:
            fields['body'] = body_text
        if labels:
            fields['labels'] = labels
        if milestone_number:
            fields['milestone'] = milestone_number
        if fields:
            updated = update_issue(owner, repo, num, fields)
            if not updated:
                return None
            print(f"Issue existente atualizada: {updated.get('html_url')} (#{num}).")
            return {'number': num, 'node_id': updated.get('node_id'), 'url': updated.get('html_url')}
        else:
            return {'number': num, 'node_id': existing.get('node_id'), 'url': existing.get('html_url')}

    url = f"{BASE}/repos/{owner}/{repo}/issues"
    payload = {'title': title}
    if body_text:
        payload['body'] = body_text
    if labels:
        payload['labels'] = labels
    if milestone_number:
        payload['milestone'] = milestone_number
    resp = requests.post(url, headers=HEADERS_REST, json=payload)
    if not resp.ok:
        print(f"Falha ao criar issue '{title}': {resp.status_code} {resp.text}", file=sys.stderr)
        return None
    r = resp.json()
    print(f"Issue criada: {r.get('html_url')} (#{r.get('number')}).")
    return {'number': r.get('number'), 'node_id': r.get('node_id'), 'url': r.get('html_url')}


def get_project_v2_id(login, number):
    query = 'query($login:String!, $number:Int!){ user(login:$login){ projectV2(number:$number){ id } } }'
    body = {'query': query, 'variables': {'login': login, 'number': number}}
    resp = requests.post(GRAPHQL, headers=HEADERS_GRAPH, json=body)
    if not resp.ok:
        print(f"Falha ao obter project v2 id: {resp.status_code} {resp.text}", file=sys.stderr)
        return None
    data = resp.json()
    try:
        return data['data']['user']['projectV2']['id']
    except Exception as e:
        print(f"Resposta inesperada ao obter project id: {data}", file=sys.stderr)
        return None


def add_item_to_project(project_id, content_id):
    mutation = 'mutation($projectId:ID!, $contentId:ID!){ addProjectV2ItemById(input:{projectId:$projectId, contentId:$contentId}){ item{ id } } }'
    body = {'query': mutation, 'variables': {'projectId': project_id, 'contentId': content_id}}
    resp = requests.post(GRAPHQL, headers=HEADERS_GRAPH, json=body)
    if not resp.ok:
        print(f"Falha ao adicionar item ao project: {resp.status_code} {resp.text}", file=sys.stderr)
        return None
    data = resp.json()
    try:
        return data['data']['addProjectV2ItemById']['item']['id']
    except Exception:
        print(f"Resposta inesperada ao adicionar item: {data}", file=sys.stderr)
        return None


def main():
    token = os.environ.get('GITHUB_TOKEN')
    if not token:
        print('Defina a variável de ambiente GITHUB_TOKEN com um token válido.', file=sys.stderr)
        sys.exit(1)
    init_headers(token)

    cfg = load_config()
    repo_full = cfg.get('repoFull', REPO_FULL_DEFAULT)
    owner, repo = repo_full.split('/')
    project_owner = cfg.get('projectOwner', 'marcelobezerra-dotcom')
    project_number = int(cfg.get('projectNumber', 9))

    milestones = cfg.get('milestones') or [
        {'title': 'Sprint 1', 'due_on': '2026-09-19T23:59:00Z', 'description': 'Kickoff e pesquisas iniciais'},
        {'title': 'Sprint 2', 'due_on': '2026-10-03T23:59:00Z', 'description': 'PoC individuais'},
        {'title': 'Sprint 3', 'due_on': '2026-10-17T23:59:00Z', 'description': 'Revisão técnica e início do SDD'},
        {'title': 'Sprint 4', 'due_on': '2026-10-31T23:59:00Z', 'description': 'Design detalhado (SDD) e integração'},
        {'title': 'Sprint 5', 'due_on': '2026-11-13T23:59:00Z', 'description': 'Finalização e apresentação'}
    ]

    labels = cfg.get('labels') or [
        {'name': 'rfid', 'color': '0e8a16', 'description': 'Tarefas relacionadas a RFID'},
        {'name': 'qrcode', 'color': '1d76db', 'description': 'QR code e código de barras'},
        {'name': 'fingerprint', 'color': '5319e7', 'description': 'Impressão digital'},
        {'name': 'sdd', 'color': '0052cc', 'description': 'Arquitetura e SDD'},
        {'name': 'docs', 'color': 'b60205', 'description': 'Documentação'},
        {'name': 'demo', 'color': 'd4c5f9', 'description': 'Itens para demo/apresentação'},
        {'name': 'high-priority', 'color': 'b60205', 'description': 'Alta prioridade'},
        {'name': 'blocker', 'color': '000000', 'description': 'Bloqueador'}
    ]

    issues = cfg.get('issues') or [
        {'title': 'Pesquisa: RFID', 'body': 'Resumo (1 pág): conceitos, padrões, hardware, APIs e 3 referências.', 'labels': ['rfid', 'docs'], 'milestone': 'Sprint 1'},
        {'title': 'PoC: RFID (leitura básica)', 'body': 'Implementar PoC de leitura RFID. Incluir README e screenshots.', 'labels': ['rfid', 'demo'], 'milestone': 'Sprint 2'},
        {'title': 'Pesquisa: QR code e Código de Barras', 'body': 'Resumo (1 pág): formatos, bibliotecas, geração e leitura. 3 referências.', 'labels': ['qrcode', 'docs'], 'milestone': 'Sprint 1'},
        {'title': 'PoC: QR/Barcode (geração e leitura)', 'body': 'PoC que gera e lê códigos; incluir README.', 'labels': ['qrcode', 'demo'], 'milestone': 'Sprint 2'},
        {'title': 'Pesquisa: Impressão Digital', 'body': 'Resumo (1 pág): sensores, algoritmos de matching, privacidade e 3 referências.', 'labels': ['fingerprint', 'docs'], 'milestone': 'Sprint 1'},
        {'title': 'PoC: Impressão Digital (simulação ou integração)', 'body': 'PoC de matching ou simulação com dados de exemplo.', 'labels': ['fingerprint', 'demo'], 'milestone': 'Sprint 2'},
        {'title': 'Estudo SDD e Documento de Visão', 'body': 'Sessões conjuntas para aplicar SDD; produzir Documento de Visão (rascunho).', 'labels': ['sdd', 'docs'], 'milestone': 'Sprint 3'},
        {'title': 'Desenho de Arquitetura (SDD)', 'body': 'Criar diagrama de componentes e especificar APIs.', 'labels': ['sdd'], 'milestone': 'Sprint 4'},
        {'title': 'Integração dos PoC e testes', 'body': 'Integrar módulos disponíveis; documentar testes manuais.', 'labels': ['demo', 'sdd'], 'milestone': 'Sprint 4'},
        {'title': 'Preparar apresentação (slides + roteiro)', 'body': 'Montar slides e roteiro de demo para 13/11.', 'labels': ['demo', 'docs'], 'milestone': 'Sprint 5'}
    ]

    # criar milestones
    milestone_map = {}
    for m in milestones:
        num = ensure_milestone(owner, repo, m.get('title'), due_on=m.get('due_on'), description=m.get('description'))
        if num:
            milestone_map[m.get('title')] = num

    # criar labels
    for l in labels:
        ensure_label(owner, repo, l.get('name'), l.get('color'), l.get('description'))

    # obter project id
    print(f"Obtendo Project v2 id para {project_owner} / #{project_number} ...")
    project_id = get_project_v2_id(project_owner, project_number)
    if not project_id:
        print('Não foi possível obter o Project v2 id. Verifique o owner/number e permissões.', file=sys.stderr)
    else:
        print(f"Project v2 id: {project_id}")

    # verificar se Issues estão habilitadas no repositório
    if not repo_has_issues(owner, repo):
        print('Issues estão desabilitadas neste repositório. Pulando criação/atualização de issues.')
        return

    # criar issues e adicionar ao project
    created_urls = []
    for it in issues:
        m_num = None
        if it.get('milestone') and it.get('milestone') in milestone_map:
            m_num = milestone_map[it.get('milestone')]
        base_body = it.get('body') or ''
        tasks = it.get('tasks')
        if tasks and isinstance(tasks, list) and len(tasks) > 0:
            tasks_md = '\n\n**Atividades:**\n' + '\n'.join([f'- [ ] {t}' for t in tasks])
            body_text = base_body + tasks_md
        else:
            body_text = base_body

        res = create_issue_return_node(owner, repo, it.get('title'), body_text=body_text, labels=it.get('labels'), milestone_number=m_num)
        if res and res.get('node_id'):
            if project_id:
                print(f"Adicionando issue (#{res.get('number')}) ao project...")
                item_id = add_item_to_project(project_id, res.get('node_id'))
                if item_id:
                    print(f"Item adicionado ao project (item id: {item_id})")
                else:
                    print('Falha ao adicionar issue ao project.', file=sys.stderr)
            created_urls.append(res.get('url'))

    print('\nConcluído. Issues criadas: {}'.format(len(created_urls)))
    for u in created_urls:
        print(u)

    print('\nFim do script.')


if __name__ == '__main__':
    main()
