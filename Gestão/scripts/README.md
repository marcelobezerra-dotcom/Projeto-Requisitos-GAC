Script: create_github_items.ps1

O que faz:
- Cria milestones (Sprint 1..5)
- Cria labels úteis para o projeto
- Cria issues iniciais do backlog e associa às milestones

Pré-requisitos:
- PowerShell (Windows)
- Um token do GitHub com permissões para o repositório (consulte a seção "Gerar token GitHub" abaixo)
-- Permissões de escrita no repositório `marcelobezerra-dotcom/Projeto-Requisitos-GAC`

Como usar:
1. Abra PowerShell na pasta `Gestão/scripts`.

2. Para usar sem `gh` (recomendado neste repositório), gere um token GitHub e exporte para a variável `GITHUB_TOKEN` (exemplos abaixo). Em seguida execute:

```powershell
cd Gestão\scripts
./create_github_items_with_token.ps1
```

3. Alternativa usando `gh` (se preferir):

```powershell
cd Gestão\scripts
./create_github_items.ps1
```

Gerar token GitHub (Personal Access Token)
1. Acesse https://github.com e faça login com sua conta.
2. Abra **Settings** (configurações) -> **Developer settings** -> **Personal access tokens**.
	- Recomendado: crie um *Fine-grained token* (Tokens de escopo fino) e conceda acesso somente ao repositório `marcelobezerra-dotcom/Projeto-Requisitos-GAC`.
	- Alternativa: crie um *Token (classic)* e selecione o escopo `repo` (concede acesso aos repositórios). Use com cautela.
3. Permissões recomendadas para o token (Fine-grained):
	- Repository permissions: `Contents` = Read & write
	- Repository permissions: `Issues` = Read & write
	- Repository permissions: `Pull requests` = Read & write (opcional)
	- Defina uma data de expiração curta (ex.: 30–90 dias) e crie o token.
4. Copie o token gerado — ele será mostrado apenas uma vez.

Como armazenar o token no Windows (PowerShell):
- Temporário (apenas na sessão atual):

```powershell
$env:GITHUB_TOKEN = 'ghp_xxx...'
```

- Persistente (usuário, requer reiniciar a sessão do PowerShell para aplicar):

```powershell
setx GITHUB_TOKEN "ghp_xxx..."
```

Segurança:
- Nunca commit o token em repositórios.
- Use expiração curta e rotacione o token se houver suspeita de vazamento.
- Prefira `gh auth login` ou armazenadores de segredos quando possível.

Observação sobre Project Kanban:
- O script cria issues no repositório. Para vinculá-las automaticamente ao Project Kanban (Projects v2) pode ser necessário usar a API GraphQL dos Projects v2 e o ID do projeto. Se quiser, eu posso gerar um script extra que adiciona issues ao Project quando você fornecer o Project ID e permissões adequadas.

---
Se precisar, eu atualizo o README com instruções específicas para criar um token Fine-grained passo-a-passo com capturas de tela.

Script para adicionar issues ao Project Kanban automaticamente:

- `create_and_add_to_project.ps1` — cria milestones, labels, issues e adiciona as issues ao Project v2 informado (usa GraphQL). Uso:

```powershell
$env:GITHUB_TOKEN = 'ghp_...'
./create_and_add_to_project.ps1 -ProjectOwner 'marcelobezerra-dotcom' -ProjectNumber 9
```

Observação: o `ProjectNumber` é o número exibido na URL do Project (ex.: `/projects/9`).