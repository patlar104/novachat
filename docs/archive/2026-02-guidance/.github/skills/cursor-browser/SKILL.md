# Cursor Browser Skill

Use this skill when the user selects Cursor's built-in browser (cursor-ide-browser MCP) for web content retrieval, verification, or browser automation. If a task requires external docs or web verification, ask which tool to use first and proceed only after the user chooses a tool.

Cursor's built-in browser provides full browser control (navigation, form filling, snapshots, screenshots) that fetch cannot do.

## When to Use Cursor Browser

- **Verify external docs** – AGP release notes, Compose BOM mapping, dependency versions
- **Navigate multi-step flows** – Sites requiring login, form fills, or multiple clicks
- **Extract structured content** – Pages with dynamic content, SPAs, or auth-gated content
- **Take snapshots** – Accessibility tree for page structure (better than raw HTML)
- **Form filling** – `browser_fill`, `browser_fill_form`, `browser_select_option`
- **Complex navigation** – `browser_navigate`, `browser_click`, `browser_hover`, `browser_drag`

## When NOT to Use Cursor Browser

- If the user selected a different tool for web retrieval or verification
- Simple static JSON/XML APIs (use appropriate API tools if available)
- Content that fetch can reliably retrieve (consider browser for reliability)

## Core Tools (Cursor Browser)

| Tool | Purpose |
|------|---------|
| `browser_navigate` | Navigate to a URL |
| `browser_snapshot` | Get accessibility snapshot (preferred over screenshot for structure) |
| `browser_click` | Click element (use ref from snapshot) |
| `browser_fill` | Fill input field |
| `browser_fill_form` | Fill multiple fields at once |
| `browser_type` | Type into editable element |
| `browser_select_option` | Select dropdown option |
| `browser_take_screenshot` | Capture visual when needed |
| `browser_evaluate` | Run JavaScript on page |
| `browser_wait_for` | Wait for text or time |
| `browser_tabs` | List, create, close browser tabs |

## Workflow

1. **Navigate**: `browser_navigate` to target URL
2. **Snapshot**: `browser_snapshot` to get element refs
3. **Interact**: Use refs from snapshot for `browser_click`, `browser_fill`, etc.
4. **Re-snapshot**: After navigation or significant DOM changes
5. **Extract**: Use snapshot content or `browser_evaluate` for data

## Example: Verify Compose BOM Version

Instead of fetch (which may fail on SPAs or auth):

1. `browser_navigate` → `https://developer.android.com/develop/ui/compose/bom/bom-mapping`
2. `browser_snapshot` → Get page structure
3. Extract version info from snapshot or via `browser_evaluate`

## Example: Form Fill / Multi-Step Flow

1. `browser_navigate` to form URL
2. `browser_snapshot` to get refs
3. `browser_fill_form` or `browser_fill` + `browser_click` for submit
4. `browser_snapshot` to verify result

## Important Notes

- **Lock/Unlock workflow**: Use `browser_lock` before interactions, `browser_unlock` when done
- **Snapshot before interact**: Always get a fresh snapshot before using element refs
- **Re-snapshot after navigation**: Refs go stale after page changes
- **Prefer snapshot over screenshot**: Use `browser_snapshot` for structure; `browser_take_screenshot` when visual check is needed
- **Wait strategy**: Use short incremental waits (1-3 seconds) with snapshot checks rather than long waits

## Related MCP Skills

- **GitKraken MCP** – git context (status, log, diff, blame, PR details). See [gitkraken-mcp skill](../gitkraken-mcp/SKILL.md).
- **Pieces MCP** – older edits from other IDEs. See [pieces-mcp skill](../pieces-mcp/SKILL.md).

## Guardrails

- **Snapshot before interact**: Always get a fresh snapshot before using element refs
- **Re-snapshot after navigation**: Refs go stale after page changes
- **Prefer snapshot over screenshot**: Use `browser_snapshot` for structure; `browser_take_screenshot` when visual check is needed
- **Lock browser during operations**: Use `browser_lock` before a sequence of operations, `browser_unlock` when done
