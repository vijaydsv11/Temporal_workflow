#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

mvn clean package
podman build -t workflow-abstraction-service .
podman build -f batch-service.Dockerfile -t batch-service .
cp -n .env.example .env || true
podman compose -f podman-compose.yml up --build
