#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_FILE="$ROOT_DIR/app/google-services.json"
REPO_NAME="$(basename "$ROOT_DIR")"

is_valid_google_services_file() {
  local file_path="$1"
  [ -f "$file_path" ] && grep -q '"project_info"' "$file_path"
}

add_candidate_if_valid() {
  local file_path="$1"
  if is_valid_google_services_file "$file_path"; then
    printf "%s\n" "$file_path"
  fi
}

discover_candidates() {
  local candidates=()

  if [ -n "${NOVACHAT_GOOGLE_SERVICES_JSON:-}" ]; then
    candidates+=("$NOVACHAT_GOOGLE_SERVICES_JSON")
  fi
  if [ -n "${GOOGLE_SERVICES_JSON_PATH:-}" ]; then
    candidates+=("$GOOGLE_SERVICES_JSON_PATH")
  fi

  candidates+=(
    "$HOME/dev/$REPO_NAME/app/google-services.json"
    "$HOME/Development/$REPO_NAME/app/google-services.json"
    "$HOME/projects/$REPO_NAME/app/google-services.json"
    "$HOME/Downloads/google-services.json"
  )

  # Codex worktree fallback: check sibling worktrees for the same repo name.
  local maybe_worktrees_dir
  maybe_worktrees_dir="$(dirname "$(dirname "$ROOT_DIR")")"
  if [ "$(basename "$maybe_worktrees_dir")" = "worktrees" ] && [ -d "$maybe_worktrees_dir" ]; then
    local wt_file=""
    while IFS= read -r wt_file; do
      [ "$wt_file" = "$TARGET_FILE" ] && continue
      candidates+=("$wt_file")
    done < <(find "$maybe_worktrees_dir" -maxdepth 3 -path "*/$REPO_NAME/app/google-services.json" -type f 2>/dev/null || true)
  fi

  local candidate=""
  for candidate in "${candidates[@]}"; do
    add_candidate_if_valid "$candidate"
  done
}

copy_google_services_if_needed() {
  if is_valid_google_services_file "$TARGET_FILE"; then
    echo "google-services.json already present: $TARGET_FILE"
    return 0
  fi

  local source_file=""
  source_file="$(discover_candidates | head -n 1 || true)"

  if [ -z "$source_file" ]; then
    echo "google-services.json not found in known locations." >&2
    echo "Set NOVACHAT_GOOGLE_SERVICES_JSON or place file at app/google-services.json." >&2
    return 1
  fi

  cp "$source_file" "$TARGET_FILE"
  echo "Copied google-services.json from: $source_file"
  echo "Updated: $TARGET_FILE"
}

copy_google_services_if_needed
