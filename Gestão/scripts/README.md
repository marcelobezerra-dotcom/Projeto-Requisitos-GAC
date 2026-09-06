Script: automação de milestones / labels / issues (Python)

O script principal agora é a versão em Python; os scripts PowerShell foram removidos deste diretório.

O que o script faz:

- Cria milestones (Sprint 1..5)
- Cria labels úteis para o projeto
- Cria issues iniciais do backlog e associa às milestones
- (Opcional) Adiciona issues ao Project Kanban (Projects v2) via GraphQL

Pré-requisitos:

- Python 3.8+ e `requests`
- Um token GitHub com permissões para o repositório (veja "Gerar token GitHub")

Arquivo principal:

- `create_and_add_to_project.py` — versão em Python (recomendada)

Gerar token GitHub (Personal Access Token)

1. Acesse https://github.com e faça login.
2. Vá em **Settings** → **Developer settings** → **Personal access tokens**.
3. Recomenda-se criar um *Fine-grained token* com acesso apenas ao repositório deste projeto, ou um *classic* com escopo `repo` se necessário.
4. Permissões mínimas recomendadas para automação:
   - `Contents`: Read & write
   - `Issues`: Read & write
   - `Pull requests`: Read & write (opcional)
5. Copie o token (será mostrado apenas uma vez).

Exportar/definir `GITHUB_TOKEN` (exemplos)

- PowerShell (sessão atual):

```powershell
$env:GITHUB_TOKEN = 'ghp_SEU_TOKEN_AQUI'
python .\create_and_add_to_project.py
# ou
.\create_and_add_to_project.ps1
```

- PowerShell (persistente para o usuário):

```powershell
[Environment]::SetEnvironmentVariable('GITHUB_TOKEN','ghp_SEU_TOKEN_AQUI','User')
# Reabra o terminal para aplicar
```

- CMD (sessão atual):

```cmd
set GITHUB_TOKEN=ghp_SEU_TOKEN_AQUI
python Gestão\scripts\create_and_add_to_project.py
```

- Git Bash / WSL / Linux:

```bash
export GITHUB_TOKEN='ghp_SEU_TOKEN_AQUI'
python Gestão/scripts/create_and_add_to_project.py
```

Instalar dependências Python

```bash
pip install requests
```

Como executar

- PowerShell (sem gh):

```powershell
cd Gestão\scripts
$env:GITHUB_TOKEN = 'ghp_...'
.\create_github_items_with_token.ps1
```

- PowerShell (com GraphQL para Projects v2):

```powershell
cd Gestão\scripts
$env:GITHUB_TOKEN = 'ghp_...'
.\create_and_add_to_project.ps1 -ProjectOwner 'seu_usuario_ou_org' -ProjectNumber 9
```

- Python (equivalente, recomendado se preferir Python):

```powershell
cd Gestão\scripts
$env:GITHUB_TOKEN = 'ghp_...'
python .\create_and_add_to_project.py
```

Segurança e boas práticas

- Nunca commit o token em repositórios ou compartilhe publicamente.
- Use expirações curtas e rotacione o token se houver suspeita de vazamento.
- Prefira *Fine-grained tokens* com acesso limitado ao repositório quando possível.

Project Kanban (Projects v2)

- O script `create_and_add_to_project.*` usa a API GraphQL para adicionar issues ao Projects v2. Para isso você precisa do `projectNumber` (número visível na URL do Project) e permissões adequadas no token.

Precisa de ajuda adicional?

- Posso adicionar um passo-a-passo com capturas de tela para criar o token, ou gerar um `requirements.txt` e um `README` mais detalhado. Quer que eu inclua essas opções?
