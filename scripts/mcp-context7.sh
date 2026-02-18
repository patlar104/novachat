#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

run_local() {
  exec npx -y @upstash/context7-mcp@1.0.31
}

if [ "${NOVACHAT_MCP_DISABLE_DOCKER:-0}" = "1" ]; then
  run_local
fi

if IMAGE_TAG="$(bash "$ROOT_DIR/scripts/ensure-cursor-mcp-image.sh" 2>/dev/null)"; then
  exec docker run --rm -i --init -e CONTEXT7_API_KEY="${CONTEXT7_API_KEY:-}" "$IMAGE_TAG" context7-mcp
fi

run_local
