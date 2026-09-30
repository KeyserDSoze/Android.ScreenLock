#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
current="$(tr -d '[:space:]' < VERSION)"
IFS=. read -r major minor patch <<< "$current"

case "${1:-patch}" in
  major) major=$((major + 1)); minor=0; patch=0 ;;
  minor) minor=$((minor + 1)); patch=0 ;;
  patch) patch=$((patch + 1)) ;;
  *) echo "Usage: $0 [major|minor|patch]" >&2; exit 1 ;;
esac

next="$major.$minor.$patch"
printf '%s\n' "$next" > VERSION
echo "$current -> $next"
