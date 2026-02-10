#!/bin/bash
# Setup script for installing Git hooks in NovaChat repository

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(git rev-parse --show-toplevel)"
HOOKS_DIR="$REPO_ROOT/.github/hooks"
GIT_HOOKS_DIR="$(git rev-parse --git-path hooks)"

echo "🔧 Setting up Git hooks for NovaChat..."
echo ""
echo "Repository root: $REPO_ROOT"
echo "Custom hooks directory: $HOOKS_DIR"
echo "Git hooks directory: $GIT_HOOKS_DIR"
echo ""

# Check if .github/hooks directory exists
if [ ! -d "$HOOKS_DIR" ]; then
    echo "❌ Error: .github/hooks directory not found at $HOOKS_DIR"
    exit 1
fi

# Create hooks directory if it doesn't exist
mkdir -p "$GIT_HOOKS_DIR"

# Make all hook scripts executable
echo "📝 Making hook scripts executable..."
chmod +x "$HOOKS_DIR"/*

# Configure Git to use .github/hooks directory
echo ""
echo "📌 Configuring Git to use .github/hooks directory..."
git config core.hooksPath ".github/hooks"
echo "  ✅ Git configured to use custom hooks directory"

# Create symlinks to Git hooks directory (only if inside repo)
echo ""
echo "🔗 Creating symlinks..."

if [[ "$GIT_HOOKS_DIR" != "$REPO_ROOT"* ]]; then
    echo "  ⚠️  Skipping symlinks (hooks dir outside repo): $GIT_HOOKS_DIR"
    echo ""
    echo "✅ Git hooks setup completed successfully!"
    exit 0
fi

for hook in pre-commit pre-push commit-msg; do
    SOURCE="$HOOKS_DIR/$hook"
    TARGET="$GIT_HOOKS_DIR/$hook"
    
    if [ -f "$SOURCE" ]; then
        # Remove existing hook if it's a symlink
        if [ -L "$TARGET" ]; then
            echo "  Removing existing symlink: $hook"
            rm "$TARGET"
        elif [ -f "$TARGET" ]; then
            echo "  Backing up existing hook: $hook -> $hook.backup"
            mv "$TARGET" "$TARGET.backup"
        fi
        
        # Create symlink
        ln -s "$SOURCE" "$TARGET"
        echo "  ✅ Installed: $hook"
    else
        echo "  ⚠️  Skipped: $hook (file not found)"
    fi
done

echo ""
echo "✅ Git hooks setup completed successfully!"
echo ""
echo "Installed hooks:"
echo "  - pre-commit:  Runs ktlint format on staged Kotlin files"
echo "  - pre-push:    Runs unit tests before pushing"
echo "  - commit-msg:  Enforces conventional commit format"
echo ""
echo "To bypass hooks (not recommended), use:"
echo "  git commit --no-verify"
echo "  git push --no-verify"
echo ""
echo "To disable hooks, run:"
echo "  git config --unset core.hooksPath"
echo ""
