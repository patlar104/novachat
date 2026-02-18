#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

pick_editor_cli() {
  if [ -n "${EDITOR_CLI:-}" ]; then
    printf "%s" "$EDITOR_CLI"
    return 0
  fi

  if command -v cursor >/dev/null 2>&1; then
    printf "cursor"
    return 0
  fi

  if command -v code >/dev/null 2>&1; then
    printf "code"
    return 0
  fi

  echo "Neither 'cursor' nor 'code' CLI is available." >&2
  return 1
}

read_recommendations() {
  node - "$ROOT_DIR" <<'NODE'
const fs = require("fs");
const path = require("path");

const root = process.argv[2];
const seen = new Set();

function addIds(ids) {
  if (!Array.isArray(ids)) return;
  for (const id of ids) {
    if (typeof id === "string" && id.includes(".")) {
      seen.add(id.trim());
    }
  }
}

const workspacePath = path.join(root, "novachat.code-workspace");
if (fs.existsSync(workspacePath)) {
  const workspace = JSON.parse(fs.readFileSync(workspacePath, "utf8"));
  addIds(workspace?.extensions?.recommendations);
}

const vscodeExtensionsPath = path.join(root, ".vscode", "extensions.json");
if (fs.existsSync(vscodeExtensionsPath)) {
  const ext = JSON.parse(fs.readFileSync(vscodeExtensionsPath, "utf8"));
  addIds(ext?.recommendations);
}

process.stdout.write(Array.from(seen).sort().join("\n"));
NODE
}

main() {
  local cli=""
  cli="$(pick_editor_cli)"

  local recommendations=""
  recommendations="$(read_recommendations)"
  if [ -z "$recommendations" ]; then
    echo "No extension recommendations found in workspace files."
    exit 0
  fi

  local installed=""
  installed="$("$cli" --list-extensions 2>/dev/null || true)"

  local missing=()
  local extension_id=""
  while IFS= read -r extension_id; do
    [ -n "$extension_id" ] || continue
    if ! printf "%s\n" "$installed" | grep -qx "$extension_id"; then
      missing+=("$extension_id")
    fi
  done <<<"$recommendations"

  if [ "${#missing[@]}" -eq 0 ]; then
    echo "All recommended extensions are already installed."
    exit 0
  fi

  echo "Installing missing extensions with '$cli':"
  local failed=0
  for extension_id in "${missing[@]}"; do
    echo "- $extension_id"
    if ! "$cli" --install-extension "$extension_id"; then
      echo "  Failed to install $extension_id" >&2
      failed=1
    fi
  done

  if [ "$failed" -ne 0 ]; then
    echo "Some extensions could not be installed. See output above." >&2
    exit 1
  fi
}

main "$@"
