#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE_TAG="${NOVACHAT_MCP_DOCKER_IMAGE:-novachat-cursor-mcp:2026-02-18}"
DOCKERFILE_PATH="$ROOT_DIR/docker/cursor-mcp.Dockerfile"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker command not found" >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "docker daemon is not reachable" >&2
  exit 1
fi

if ! docker image inspect "$IMAGE_TAG" >/dev/null 2>&1; then
  echo "Building Cursor MCP image: $IMAGE_TAG" >&2
  docker build -f "$DOCKERFILE_PATH" -t "$IMAGE_TAG" "$ROOT_DIR" >/dev/null
fi

echo "$IMAGE_TAG"
