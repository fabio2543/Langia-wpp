#!/usr/bin/env bash
set -euo pipefail

# =======================
# CONFIG
# =======================
OWNER="fabio2543"
REPO="Langia-wpp"   # <- seu repo atual
PROJECT_TITLE="Sprint 1 – Canal WhatsApp + Cadastro Básico"
SPRINT="Sprint 1"

echo "🚀 Iniciando configuração da Sprint 1 para $OWNER/$REPO"


# =======================
# 1) (Opcional) Remover projeto antigo
# =======================
read -p "Deseja excluir o projeto antigo? (y/n): " CONFIRM
if [[ "$CONFIRM" =~ ^[Yy]$ ]]; then
  echo "Listando projetos existentes..."
  gh project list --owner "$OWNER"
  read -p "Digite o número do projeto a excluir: " OLD_PN
  if [[ -n "${OLD_PN:-}" ]]; then
    echo "➡️  Excluindo Project #$OLD_PN (confirme quando a CLI pedir)..."
    gh project delete "$OLD_PN" --owner "$OWNER" || true
    echo "🗑️ Projeto $OLD_PN removido (se você confirmou)."
  fi
else
  echo "Mantendo projetos existentes."
fi

# =======================
# 2) Criar novo Project
# =======================
echo "📦 Criando novo projeto “$PROJECT_TITLE”..."
gh project create --owner "$OWNER" --title "$PROJECT_TITLE"

# Descobrir número pelo título (tabela: NUMBER  TITLE  ...)
PN=""
for i in {1..10}; do
  PN=$(gh project list --owner "$OWNER" | awk -F'\t' -v t="$PROJECT_TITLE" 'NR>1 && $2==t {print $1; exit}')
  [[ -n "$PN" ]] && break
  sleep 1
done

if [[ -z "$PN" ]]; then
  echo "❌ Não consegui obter o número do projeto. Rode 'gh project list --owner $OWNER' e pegue o NUMBER manualmente."
  exit 1
fi

echo "✅ Projeto criado com NUMBER: $PN"
# Descrição (flag correta é --description / -d)
gh project edit "$PN" --owner "$OWNER" --description "Sprint LangIA: WhatsApp + Cadastro Básico"

# =======================
# 3) Criar campos personalizados (idempotente)
# =======================
echo "🧭 Criando campos personalizados..."
gh project field-create "$PN" --owner "$OWNER" --name "Tipo"   --data-type SINGLE_SELECT --options "Epic,Story"   >/dev/null 2>&1 || true
gh project field-create "$PN" --owner "$OWNER" --name "Área"   --data-type SINGLE_SELECT --options "Backend,Infra,Integração,Bot Logic,Segurança" >/dev/null 2>&1 || true
gh project field-create "$PN" --owner "$OWNER" --name "Sprint" --data-type TEXT >/dev/null 2>&1 || true
echo "✅ Campos prontos."

# =======================
# 4) Função: criar issue e adicionar ao Project
# =======================
create_issue_and_add () {
  local title="$1"; local body="$2"; local tipo="$3"; local area="$4"
  local labels=()
  [[ -n "$tipo" ]] && labels+=(-l "$tipo")
  [[ -n "$area" ]] && labels+=(-l "$area")
  [[ -n "$SPRINT" ]] && labels+=(-l "$SPRINT")

  # Cria issue e captura a última linha com URL
  local issue_url
  issue_url=$(
    gh issue create \
      --repo "$OWNER/$REPO" \
      --title "$title" \
      --body "$body" \
      "${labels[@]}" \
      | awk '/https:\/\/github.com/ {u=$0} END{print u}'
  )

  if [[ -z "$issue_url" ]]; then
    echo "❌ Falha ao criar issue: $title (confira se o repo $OWNER/$REPO existe e você tem permissão)"
    return 1
  fi

  echo "✔ Issue criada: $issue_url"
  # Adicionar ao Project
  if gh project item-add "$PN" --owner "$OWNER" --url "$issue_url" >/dev/null 2>&1; then
    echo "↳ Adicionada ao Project #$PN"
  else
    echo "❌ Não consegui adicionar ao Project #$PN. Tente: gh auth refresh -h github.com -s project -s read:project"
    return 1
  fi
}

# =======================
# 5) Criar todos os itens
# =======================
echo "🧱 Criando épicos e histórias..."

# ÉPICOS
create_issue_and_add "EP-01 – Mensageria WhatsApp"         "Webhook validado, mensagens registradas, resposta “OK” confirmada."                 "Epic"  ""
create_issue_and_add "EP-02 – Gestão Básica de Usuários"   "Professores e alunos cadastrados; aluno recebe mensagem de boas-vindas."           "Epic"  ""
create_issue_and_add "EP-03 – Observabilidade & Segurança" "Mensagens logadas e opt-out “PARAR” funcionando."                                  "Epic"  ""

# EP-01
create_issue_and_add "H1.1 – Configurar Webhook de Recebimento"  "Receber mensagens; validar HMAC (X-Hub-Signature-256); gravar IN/RECEIVED no message_log." "Story" "Integração"
create_issue_and_add "H1.2 – Enviar mensagem “Bem-vindo” (template)" "WhatsAppSender; template welcome_default; log OUT/SENT."                                "Story" "Integração"
create_issue_and_add "H1.3 – Resposta automática “Conexão estabelecida ✅”" "Normalizar texto; detectar 'ok'; responder e logar OUT/SENT."                     "Story" "Bot Logic"

# EP-02
create_issue_and_add "H2.1 – Cadastrar Professor"         "POST /api/v1/teachers; validação; 201 Created; persistência em teacher."            "Story" "Backend"
create_issue_and_add "H2.2 – Cadastrar Aluno + Whitelist" "POST /api/v1/students; validar E.164/unicidade; disparo mensagem de boas-vindas."  "Story" "Backend"

# EP-03
create_issue_and_add "H3.1 – Persistir Logs de Mensagens" "Tabela message_log; payload JSON, direction, status, timestamp; índices e repo."   "Story" "Infra"
create_issue_and_add "H3.2 – LGPD Básico (opt-out “PARAR”)" "Detectar 'PARAR'; set active=false; enviar confirmação; registrar log."           "Story" "Segurança"

# Release
create_issue_and_add "Release – Teste E2E + Deploy Cloud Run" "Fluxo ponta-a-ponta validado; deploy no Cloud Run; logs visíveis; roteiro de testes." "Story" "Infra"

echo ""
echo "✅ Sprint 1 configurada!"
echo "👉 Project: https://github.com/users/$OWNER/projects/$PN"
echo "👉 Issues:  https://github.com/$OWNER/$REPO/issues"