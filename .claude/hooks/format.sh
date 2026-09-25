#!/usr/bin/env bash
# Formats Java after edits, if the build is set up with Spotless. Silent no-op otherwise.
input=$(cat)
file=$(echo "$input" | jq -r '.tool_input.file_path // empty')
[[ "$file" != *.java ]] && exit 0
cd "$CLAUDE_PROJECT_DIR" || exit 0
if [[ -x ./gradlew ]] && grep -qs spotless build.gradle build.gradle.kts; then
  ./gradlew -q spotlessApply >/dev/null 2>&1 || true
elif [[ -x ./mvnw ]] && grep -qs spotless pom.xml; then
  ./mvnw -q spotless:apply >/dev/null 2>&1 || true
fi
exit 0
