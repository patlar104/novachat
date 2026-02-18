#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

run_local() {
  exec npx -y @playwright/mcp@0.0.68
}

if [ "${NOVACHAT_MCP_DISABLE_DOCKER:-0}" = "1" ]; then
  run_local
fi

if IMAGE_TAG="$(bash "$ROOT_DIR/scripts/ensure-cursor-mcp-image.sh" 2>/dev/null)"; then
  exec docker run --rm -i --init "$IMAGE_TAG" playwright-mcp
fi

run_local
