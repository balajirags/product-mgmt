#!/bin/bash
# Guild setup validation script
# Works in two layouts:
#   (A) Inside the Guild repo itself  → agents/ instructions/ at root
#   (B) Inside a project using Guild  → .guild/agents/ .guild/instructions/ etc.

PASS=0; FAIL=0; WARN=0

pass() { echo "  ✅ $1"; PASS=$((PASS+1)); }
fail() { echo "  ❌ $1 — $2"; FAIL=$((FAIL+1)); }
warn() { echo "  ⚠️  $1 — $2"; WARN=$((WARN+1)); }

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Guild Setup Check"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ── Detect layout ────────────────────────────────────────────────────────────
if [ -d "agents" ] && [ -d "skills" ]; then
  GUILD_DIR="."          # Running inside the Guild repo itself
  LAYOUT="guild-repo"
elif [ -d ".guild/agents" ] && [ -d ".guild/skills" ]; then
  GUILD_DIR=".guild"     # Running in a project with Guild as submodule/copy
  LAYOUT="project"
else
  echo ""
  echo "  ❌ Cannot find Guild files."
  echo "     Expected either:"
  echo "       agents/ skills/ instructions/  (if running inside the Guild repo)"
  echo "       .guild/agents/ .guild/skills/  (if running in a project using Guild)"
  echo ""
  echo "     To install Guild in your project:"
  echo "       git submodule add https://github.com/your-org/guild .guild"
  echo "     Then re-run: bash .guild/scripts/setup-check.sh"
  exit 1
fi

echo ""
if [ "$LAYOUT" = "guild-repo" ]; then
  echo "  Layout: Guild repo (agents/ at root)"
else
  echo "  Layout: Project using Guild (agents/ at .guild/)"
fi

# ── Required CLI tools ───────────────────────────────────────────────────────
echo ""
echo "Required tools:"
command -v git     &>/dev/null && pass "git"     || fail "git"     "Install git from git-scm.com"
command -v jq      &>/dev/null && pass "jq"      || fail "jq"      "brew install jq  OR  apt install jq"
command -v python3 &>/dev/null && pass "python3" || fail "python3" "Install Python 3.9+"
command -v bc      &>/dev/null && pass "bc"      || warn "bc"      "brew install bc  (needed for token cost calculation)"

echo ""
echo "Container runtime (need one):"
if command -v colima &>/dev/null; then
  pass "colima (recommended container runtime)"
  if colima status 2>/dev/null | grep -q "Running"; then
    pass "  colima is running"
  else
    warn "  colima installed but not running" "Run: colima start --cpu 4 --memory 8 --disk 60"
  fi
elif command -v docker &>/dev/null; then
  pass "docker (container runtime)"
  warn "  Docker Desktop detected" "Consider Colima: brew install colima (free, no license)"
else
  warn "No container runtime" "brew install colima && colima start"
fi
command -v docker &>/dev/null && pass "docker CLI" || warn "docker CLI" "Installed with Colima or Docker Desktop"

echo ""
echo "Local Kubernetes (optional — for local deploy testing):"
if command -v kind &>/dev/null; then
  pass "kind (recommended local K8s)"
elif command -v minikube &>/dev/null; then
  pass "minikube (local K8s)"
else
  warn "No local K8s tool" "brew install kind && kind create cluster --name guild-local"
fi
command -v kubectl &>/dev/null && pass "kubectl" || warn "kubectl" "brew install kubectl"
command -v bru     &>/dev/null && pass "bru (Bruno CLI)" || warn "bru" "npm install -g @usebruno/cli"

# ── Hook configuration ───────────────────────────────────────────────────────
echo ""
echo "Guild hook configuration:"
[ -f ".claude/settings.json" ] && pass ".claude/settings.json" \
  || fail ".claude/settings.json" "Copy from ${GUILD_DIR}/.claude/settings.json or create from plan.md"

# ── Hook scripts ─────────────────────────────────────────────────────────────
echo ""
echo "Guild hook scripts (${GUILD_DIR}/hooks/):"
HOOKS=(pre-bash.sh pre-write.sh post-write-scan.sh post-bash-audit.sh track-tokens.sh on-stop.sh pre-commit)
HOOKS_DIR="${GUILD_DIR}/hooks"
# Handle layout difference: guild repo has hooks at .guild/hooks; project has them at .guild/hooks too
if [ "$LAYOUT" = "guild-repo" ]; then
  HOOKS_DIR=".guild/hooks"
else
  HOOKS_DIR=".guild/hooks"
fi

for hook in "${HOOKS[@]}"; do
  if [ -f "${HOOKS_DIR}/${hook}" ]; then
    chmod +x "${HOOKS_DIR}/${hook}"
    pass "${HOOKS_DIR}/${hook} (made executable)"
  else
    fail "${HOOKS_DIR}/${hook}" "Missing"
  fi
done

# ── Instructions ─────────────────────────────────────────────────────────────
echo ""
echo "Guild instructions (${GUILD_DIR}/instructions/):"
INST_DIR="${GUILD_DIR}/instructions"
[ -f "${INST_DIR}/guild-guardrails.instructions.md" ] && pass "guild-guardrails.instructions.md" \
  || fail "guild-guardrails.instructions.md" "Missing from ${INST_DIR}/"
[ -f "${INST_DIR}/twelve-factor.instructions.md" ] && pass "twelve-factor.instructions.md" \
  || fail "twelve-factor.instructions.md" "Missing from ${INST_DIR}/"

# ── Agents ───────────────────────────────────────────────────────────────────
echo ""
echo "Guild agent files (${GUILD_DIR}/agents/):"
AGENTS_DIR="${GUILD_DIR}/agents"
REQUIRED_AGENTS=(guild-orchestrator.agent.md developer.agent.md reviewer.agent.md ba.agent.md test.agent.md)
for agent in "${REQUIRED_AGENTS[@]}"; do
  [ -f "${AGENTS_DIR}/${agent}" ] && pass "${agent}" || fail "${AGENTS_DIR}/${agent}" "Missing"
done

# ── CLAUDE.md reference check ────────────────────────────────────────────────
echo ""
echo "CLAUDE.md Guild references:"
if [ -f "CLAUDE.md" ]; then
  if grep -q "Guild" CLAUDE.md && grep -q "${GUILD_DIR}/agents" CLAUDE.md 2>/dev/null || \
     grep -q "agents" CLAUDE.md 2>/dev/null; then
    pass "CLAUDE.md references Guild paths"
  else
    warn "CLAUDE.md may be missing Guild paths" "Add: Agents: ${GUILD_DIR}/agents/ | Skills: ${GUILD_DIR}/skills/ | Instructions: ${GUILD_DIR}/instructions/"
  fi
else
  warn "CLAUDE.md not found" "Create it and add Guild path references (see README.md)"
fi

# ── guild-input.md ───────────────────────────────────────────────────────────
echo ""
echo "Project configuration:"
EXAMPLES_DIR="${GUILD_DIR}/examples"

if [ -f "guild-input.md" ]; then
  pass "guild-input.md found"
  for field in "name:" "mode:" "language:" "framework:"; do
    grep -q "^$field" guild-input.md && pass "  field: $field" \
      || warn "  field: $field" "Required field may be missing"
  done
elif [ -d "$EXAMPLES_DIR" ]; then
  warn "guild-input.md" "Not found — create from template:"
  warn "  Quickstart" "cp ${EXAMPLES_DIR}/guild-input.template.md guild-input.md && edit it"
else
  warn "guild-input.md" "Not found — say 'guild init' to Claude Code to generate one"
fi

# ── Runtime directories ──────────────────────────────────────────────────────
echo ""
echo "Runtime setup:"
mkdir -p .guild memories/session
touch .guild/audit.log .guild/token-log.jsonl
echo "" > .guild/.current-agent
pass ".guild/ runtime directory ready"
pass "memories/session/ ready"

# ── Git hook installation ────────────────────────────────────────────────────
echo ""
echo "Git hook installation:"
if [ -d ".git" ]; then
  if [ -f ".guild/hooks/pre-commit" ]; then
    cp .guild/hooks/pre-commit .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    pass "pre-commit hook installed to .git/hooks/"
  else
    fail "pre-commit hook" ".guild/hooks/pre-commit not found"
  fi
else
  warn "Git hooks" "Not a git repository — run 'git init' first"
fi

# ── Summary ──────────────────────────────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Result: $PASS passed  |  $WARN warnings  |  $FAIL failed"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $FAIL -eq 0 ]; then
  echo ""
  echo " ✅ Guild is ready!"
  echo ""
  echo " Next steps:"
  if [ ! -f "guild-input.md" ]; then
    echo "   1. cp ${EXAMPLES_DIR}/guild-input.template.md guild-input.md"
    echo "   2. Fill in your project details"
    echo "   3. Say: 'guild init' to Claude Code"
  else
    echo "   Say: 'guild init'   — start a new pipeline"
    echo "   Say: 'guild status' — check pipeline state"
  fi
else
  echo ""
  echo " ❌ Fix the $FAIL failed checks, then re-run."
fi
echo ""
