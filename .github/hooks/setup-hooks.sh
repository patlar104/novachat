#!/bin/bash
# Setup script for installing Git hooks in NovaChat repository

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(git rev-parse --show-toplevel)"
HOOKS_DIR="$REPO_ROOT/.github/hooks"
GIT_HOOKS_DIR="$(git rev-parse --git-path hooks)"

echo "Setting up Git hooks for NovaChat"
echo "Repository root: $REPO_ROOT"
echo "Custom hooks directory: $HOOKS_DIR"
echo "Git hooks directory: $GIT_HOOKS_DIR"

if [ ! -d "$HOOKS_DIR" ]; then
  echo "Error: .github/hooks directory not found at $HOOKS_DIR"
  exit 1
fi

mkdir -p "$GIT_HOOKS_DIR"
chmod +x "$HOOKS_DIR"/*

git config core.hooksPath ".github/hooks"
echo "Git configured to use .github/hooks"

if [[ "$GIT_HOOKS_DIR" != "$REPO_ROOT"* ]]; then
  echo "Skipping symlink setup because git hooks dir is outside repo"
  echo "Hook path configuration is complete"
  exit 0
fi

for hook in pre-commit pre-push commit-msg; do
  SOURCE="$HOOKS_DIR/$hook"
  TARGET="$GIT_HOOKS_DIR/$hook"

  if [ -f "$SOURCE" ]; then
    if [ -L "$TARGET" ]; then
      rm "$TARGET"
    elif [ -f "$TARGET" ]; then
      mv "$TARGET" "$TARGET.backup"
    fi
    ln -s "$SOURCE" "$TARGET"
    echo "Installed hook: $hook"
  fi
done

echo "Git hooks setup completed"
echo "Installed hooks:"
echo "  pre-commit: changed-scope checks (Android/functions/format)"
echo "  pre-push: full Android + functions + format checks"
echo "  commit-msg: conventional commit validation"
