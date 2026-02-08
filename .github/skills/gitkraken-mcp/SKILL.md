# GitKraken MCP Skill

Use **GitKraken MCP** tools for git context, history, and workflow operations. Prefer these over raw terminal git commands when working with Cursor Agent.

## When to Use GitKraken MCP

- **Check repo state** – `git_status` before starting work or after changes
- **View history** – `git_log_or_diff` for commit history, diffs, or changes in a range
- **Code review** – `git_blame` for authorship, `git_log_or_diff` with diff for changes
- **Branch management** – `git_branch` (list/create), `git_checkout` to switch
- **PR/issue context** – `pull_request_get_detail`, `issues_get_detail`, `issues_assigned_to_me`
- **Staging/committing** – `git_add_or_commit` for staging or committing (when agent has approval)
- **Push/stash** – `git_push`, `git_stash` when needed

## Core Tools

| Tool | Purpose |
|------|---------|
| `git_status` | Working tree status |
| `git_log_or_diff` | Commit logs or diff between revisions |
| `git_blame` | Revision and author per line |
| `git_branch` | List or create branches |
| `git_checkout` | Switch branches |
| `git_add_or_commit` | Stage files or commit |
| `git_push` | Push to remote |
| `git_stash` | Stash changes |
| `git_worktree` | List or add worktrees |
| `pull_request_get_detail` | PR details |
| `pull_request_get_comments` | PR comments |
| `pull_request_create_review` | Create PR review |
| `issues_assigned_to_me` | Assigned issues |
| `issues_get_detail` | Issue details |
| `issues_add_comment` | Add issue comment |
| `repository_get_file_content` | File content from repo (branch/ref) |

## Agent-Specific Usage

### Planner Agent
- `git_status` – Current state before planning
- `git_log_or_diff` – Recent commits to understand recent work
- `git_branch` – Active branches

### Reviewer Agent
- `git_status` – Uncommitted changes
- `git_log_or_diff` (diff) – Changes in PR or range
- `git_blame` – Who last modified code
- `pull_request_get_detail` – PR context
- `pull_request_get_comments` – Existing review comments

### Build Agent
- `git_status` – What files changed
- `git_log_or_diff` – Recent build-related commits

### Implementation Agents (UI, Backend, Testing, Preview)
- `git_status` – Before/after work
- `git_log_or_diff` – Context for related changes

## Related: Pieces MCP

For older edits from other IDEs or past sessions, use **Pieces MCP** (`ask_pieces_ltm`). See [pieces-mcp skill](../pieces-mcp/SKILL.md).

## Guardrails

- **Read-only by default**: Use `git_status`, `git_log_or_diff`, `git_blame` for context; only use `git_add_or_commit`, `git_push`, `git_stash` when explicitly requested or approved
- **Reviewer**: Never commits; uses git tools for read-only context
- **Planner**: Never modifies files; uses git for discovery only
