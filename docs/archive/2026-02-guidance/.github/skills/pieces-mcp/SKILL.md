# Pieces MCP Skill

Use **Pieces MCP** (`ask_pieces_ltm`) to find older edits, changes, and context that may exist in your Long-Term Memory but are not in the current repo or active session. Pieces captures workflow data from Cursor, other IDEs, browser, and apps—all stored locally.

## When to Use Pieces MCP

- **Forgotten edits** – Edits made in Android Studio, VS Code, or elsewhere that weren’t committed or synced
- **Previous implementations** – "What was my last implementation of X?"
- **Past decisions** – "What decisions did we make about Y?"
- **Cross-IDE context** – Work done in another IDE that might be relevant here
- **Time-based recall** – "What was I working on yesterday?" or "Changes from last week"
- **Before reimplementing** – Check if something similar already exists in LTM

## Core Tool

| Tool | Purpose |
|------|---------|
| `ask_pieces_ltm` | Query Long-Term Memory with natural language |

## Effective Querying

### Use specific parameters

- **Time ranges**: "yesterday", "last week", "April 2nd through April 6th"
- **Application sources**: "edits in Android Studio", "changes in VS Code"
- **Topics**: "NovaChat authentication", "Compose BOM version", "ViewModel changes"
- **File/feature hints**: "build.gradle.kts changes", "ChatScreen edits"

### Example queries

- "What edits did I make to NovaChat in Android Studio that I might have forgotten to commit?"
- "What was my last implementation of API error handling?"
- "Show recent Kotlin/Compose changes from other IDEs"
- "What dependency version updates did I discuss or change recently?"
- "Find notes or decisions about the NovaChat project from this week"

## Agent-Specific Usage

### Planner Agent
- Query before planning: "What NovaChat work was done recently in other IDEs?"
- Avoid duplicating work that exists in LTM

### Reviewer Agent
- "What previous feedback or decisions exist about this code?"
- "Show similar code review comments from past sessions"

### Build Agent
- "What dependency or Gradle changes did I make elsewhere?"
- "Find build.gradle.kts edits from Android Studio"

### Implementation Agents (UI, Backend, Testing, Preview)
- Before implementing: "What similar implementations exist in my history?"
- "What ViewModel/Composable changes did I make in other sessions?"

## Related MCP Skills

- **GitKraken MCP** – git context (status, log, diff). See [gitkraken-mcp skill](../gitkraken-mcp/SKILL.md).
- **Cursor Browser** – web content verification. See [cursor-browser skill](../cursor-browser/SKILL.md).

## Guardrails

- **PiecesOS required**: Pieces MCP needs PiecesOS running and LTM enabled
- **Privacy**: All data is stored locally on your device
- **Supplement, don’t replace**: Use LTM to complement repo context, not as the sole source
