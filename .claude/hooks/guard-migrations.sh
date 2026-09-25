#!/usr/bin/env bash
# Blocks edits to Flyway migrations that are already committed to git.
# "Committed" is our proxy for "applied somewhere". New migrations are allowed.
# Exit code 2 blocks the tool call and shows stderr to Claude.
input=$(cat)
file=$(echo "$input" | jq -r '.tool_input.file_path // empty')
[[ -z "$file" ]] && exit 0
cd "$CLAUDE_PROJECT_DIR" || exit 0
if [[ "$file" == *"/db/migration/"* ]] && git ls-files --error-unmatch "$file" >/dev/null 2>&1; then
  echo "BLOCKED: $file is a committed Flyway migration. Never edit applied migrations; create a new V<n>__*.sql instead." >&2
  exit 2
fi
exit 0
