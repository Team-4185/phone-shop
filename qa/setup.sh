#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_DIR=$(dirname "$SCRIPT_DIR")
ENV_FILE="$SCRIPT_DIR/test.env"

if [ ! -f "$ENV_FILE" ]; then
  ENV_FILE="$PROJECT_DIR/test.env"
fi

if [ ! -f "$ENV_FILE" ]; then
  echo "Missing test.env. Put it into $SCRIPT_DIR or $PROJECT_DIR." >&2
  exit 1
fi

compose() {
  docker compose \
    --project-directory "$PROJECT_DIR" \
    --project-name gadget-room-backend \
    --env-file "$ENV_FILE" \
    "$@"
}

MODE="${1:-up}"

case "$MODE" in
  up)
    echo "Starting up gadget-room..."
    compose up --build
    ;;
  reset)
    echo "Resetting gadget-room containers and volumes..."
    compose down -v --remove-orphans
    echo "Starting up gadget-room..."
    compose up --build
    ;;
  *)
    echo "Usage: $0 [up|reset]" >&2
    exit 1
    ;;
esac
