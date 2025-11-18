#!/usr/bin/env bash
# Git MCP helper script implementing basic operations described in the tester prompt.
# Usage: ./scripts/git_mcp.sh <command> [args]
# Commands:
#   status
#   add_all
#   commit "message"
#   push [remote]
#   pr [base] "title" "body"

set -euo pipefail

cmd=${1:-}

function usage() {
  echo "Usage: $0 <status|add_all|commit|push|pr> [...args]"
  exit 2
}

case "$cmd" in
  status)
    git status --porcelain=2 --branch
    ;;
  add_all)
    # add all but ignore common build artifact dirs
    git add --all
    echo "Staged changes (filtered by .gitignore)."
    ;;
  commit)
    shift
    if [ $# -lt 1 ]; then
      echo "commit requires a message"
      usage
    fi
    msg="$*"
    # Attempt to include coverage summary if present
  cov_file="codebase/target/site/jacoco/coverage-summary.json"
    if [ -f "$cov_file" ]; then
      cov=$(cat "$cov_file" | python3 -c "import sys,json;print(json.load(sys.stdin).get('line_coverage_percent'))")
      git commit -m "$msg (coverage=${cov}%)" || git commit -m "$msg"
    else
      git commit -m "$msg"
    fi
    ;;
  push)
    shift || true
    remote=${1:-origin}
    current_branch=$(git rev-parse --abbrev-ref HEAD)
    git push -u "$remote" "$current_branch"
    ;;
  pr)
    shift
    base=${1:-main}
    title=${2:-"WIP: automated test updates"}
    body=${3:-"Automated test / coverage updates."}
    if command -v gh >/dev/null 2>&1; then
      # create pr using gh CLI
      gh pr create --base "$base" --title "$title" --body "$body"
    else
      echo "gh CLI not found; please create a PR from branch $(git rev-parse --abbrev-ref HEAD) to $base"
    fi
    ;;
  *)
    usage
    ;;
esac
