# Jira via MCP in VS Code + Cursor — Recommended Setup Cheat Sheet

## What’s the recommended way?

### ✅ If you are on **Jira Cloud (atlassian.net)** → Use **Atlassian Rovo Remote MCP (official)**
**Why**
- Official Atlassian-managed MCP endpoint (less maintenance).
- OAuth-based auth (no long-lived tokens in config by default).
- Uses your existing Jira permissions and admin controls.

**Remote MCP URL**
- `https://mcp.atlassian.com/v1/mcp`

### ✅ If you are on **Jira Data Center / Server (self-hosted)** → Use a **Local Jira MCP server**
**Why**
- Works well behind corporate networks / VPNs.
- Supports PAT/API token flows commonly used in DC/Server.
- Lets you keep traffic local and customize/proxy as needed.

A popular local option: **`mcp-atlassian`** (community).

---

## 1) VS Code (Copilot) — Configure MCP

### Option A (Recommended for Jira Cloud): Atlassian Rovo Remote MCP

Create a workspace file: `.vscode/mcp.json`

```json
{
  "servers": {
    "atlassian-rovo": {
      "type": "http",
      "url": "https://mcp.atlassian.com/v1/mcp"
    }
  }
}
```

**Then**
1. Reload VS Code window.
2. Open Copilot Chat and try:  
   - “List issues assigned to me”  
   - “Create a Jira ticket for …”

> If your org enforces allowlists or needs admin enablement, you may need an admin to allow Rovo MCP in Atlassian settings.

---

### Option B (Local server): `mcp-atlassian` (Cloud or DC/Server)

Create `.vscode/mcp.json` with a secure prompt for the token:

```json
{
  "inputs": [
    {
      "type": "promptString",
      "id": "jiraToken",
      "description": "Jira API token / PAT",
      "password": true
    }
  ],
  "servers": {
    "jira": {
      "type": "stdio",
      "command": "uvx",
      "args": ["mcp-atlassian"],
      "env": {
        "JIRA_URL": "https://your-company.atlassian.net",
        "JIRA_USERNAME": "you@company.com",
        "JIRA_API_TOKEN": "${input:jiraToken}"
      }
    }
  }
}
```

**Notes**
- **Jira Cloud**: `JIRA_USERNAME` is your email; `JIRA_API_TOKEN` is an Atlassian API token.
- **Jira Data Center/Server**: use a PAT if your Jira supports it, and set `JIRA_URL` to your internal Jira base URL.

---

## 2) Cursor — Configure MCP

Cursor reads MCP config from:
- Global: `~/.cursor/mcp.json`
- Project: `.cursor/mcp.json`

### Option A (Recommended for Jira Cloud): Atlassian Rovo Remote MCP

```json
{
  "mcpServers": {
    "Atlassian-Rovo-MCP": {
      "url": "https://mcp.atlassian.com/v1/mcp"
    }
  }
}
```

If your Cursor version expects a command-based remote wrapper, use:

```json
{
  "mcpServers": {
    "Atlassian-Rovo-MCP": {
      "command": "npx",
      "args": ["mcp-remote@latest", "https://mcp.atlassian.com/v1/mcp"]
    }
  }
}
```

Restart Cursor / reload tools after saving.

---

### Option B (Local server): `mcp-atlassian`

```json
{
  "mcpServers": {
    "jira": {
      "command": "uvx",
      "args": ["mcp-atlassian"],
      "env": {
        "JIRA_URL": "https://your-company.atlassian.net",
        "JIRA_USERNAME": "you@company.com",
        "JIRA_API_TOKEN": "YOUR_TOKEN_HERE"
      }
    }
  }
}
```

**Security tip:** Prefer environment variables instead of hardcoding tokens in the file.

---

## 3) Quick “Does it work?” prompts

Try these in your IDE chat:

- “Search Jira for issues in project ABC with status = In Progress”
- “Create a Jira story titled ‘Add retry for payment webhook’ with acceptance criteria”
- “Summarize the last 10 issues I commented on”

---

## 4) Troubleshooting checklist

- **VS Code**: Run `MCP: List Servers` and open MCP output/logs.
- **Auth fails (Cloud/Rovo)**: Complete OAuth in browser; org may need to allow the client.
- **Network**: Corporate proxy/VPN may block external MCP endpoints.
- **Permissions**: MCP can only do what your Jira user can do.

---

## 5) One-line recommendation

- **Jira Cloud** → **Atlassian Rovo Remote MCP** ✅  
- **Jira Data Center/Server** → **Local MCP server** ✅
