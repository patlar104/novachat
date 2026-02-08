#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  scripts/setup-worktree.sh --branch <name> [--new] [--from <base>] [--base <dir>] [--path <dir>]

Options:
  --branch <name>  Branch name for the worktree (required).
  --new            Create a new branch before adding the worktree.
  --from <base>    Base branch for --new (default: main).
  --base <dir>     Base directory for worktrees (default: $WORKTREE_BASE or $HOME/dev/projects/novachat.worktrees).
  --path <dir>     Exact path for the worktree (overrides --base).

Examples:
  scripts/setup-worktree.sh --branch feature/chat-ux --new
  scripts/setup-worktree.sh --branch fix/build --path "$HOME/dev/projects/novachat.worktrees/fix-build"
EOF
}

branch=""
from="main"
base="${WORKTREE_BASE:-$HOME/dev/projects/novachat.worktrees}"
path=""
create_new="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --branch)
      branch="$2"
      shift 2
      ;;
    --from)
      from="$2"
      shift 2
      ;;
    --base)
      base="$2"
      shift 2
      ;;
    --path)
      path="$2"
      shift 2
      ;;
    --new)
      create_new="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

if [[ -z "$branch" ]]; then
  echo "--branch is required." >&2
  usage >&2
  exit 1
fi

if [[ -z "$path" ]]; then
  safe_branch="${branch//\//-}"
  path="${base}/${safe_branch}"
fi

repo_root="$(git rev-parse --show-toplevel)"
if [[ -z "$repo_root" ]]; then
  echo "Unable to locate git repo root." >&2
  exit 1
fi

mkdir -p "$(dirname "$path")"

cd "$repo_root"
if [[ "$create_new" == "true" ]]; then
  git worktree add -b "$branch" "$path" "$from"
else
  git worktree add "$path" "$branch"
fi

echo "Worktree created at: $path"
