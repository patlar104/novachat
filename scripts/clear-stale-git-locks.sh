#!/usr/bin/env bash

set -euo pipefail

MIN_LOCK_AGE_SECONDS="${MIN_LOCK_AGE_SECONDS:-120}"

if ! git rev-parse --git-dir >/dev/null 2>&1; then
  echo "Not inside a git repository."
  exit 0
fi

GIT_DIR="$(git rev-parse --git-dir)"
GIT_COMMON_DIR="$(git rev-parse --git-common-dir)"

get_mtime() {
  local file_path="$1"
  if stat -f "%m" "$file_path" >/dev/null 2>&1; then
    stat -f "%m" "$file_path"
    return 0
  fi
  stat -c "%Y" "$file_path"
}

is_lock_in_use() {
  local file_path="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof "$file_path" >/dev/null 2>&1
    return $?
  fi
  return 1
}

now_epoch="$(date +%s)"
removed=0

while IFS= read -r lock_path; do
  [ -f "$lock_path" ] || continue

  if is_lock_in_use "$lock_path"; then
    continue
  fi

  lock_mtime="$(get_mtime "$lock_path")"
  lock_age=$((now_epoch - lock_mtime))

  if [ "$lock_age" -lt "$MIN_LOCK_AGE_SECONDS" ]; then
    continue
  fi

  rm -f "$lock_path"
  echo "Removed stale git lock: $lock_path"
  removed=$((removed + 1))
done < <(find "$GIT_DIR" "$GIT_COMMON_DIR" -type f -name "*.lock" 2>/dev/null | sort -u)

if [ "$removed" -eq 0 ]; then
  echo "No stale git lock files found."
fi
