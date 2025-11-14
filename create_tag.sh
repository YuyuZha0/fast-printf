#!/bin/bash

# A script to safely create and push a Git tag for a new release.
#
# This script enforces several safety checks:
# 1. You must be on the main branch.
# 2. Your working directory must be clean (no uncommitted changes).
# 3. Your local branch must be perfectly synchronized with the remote.
# 4. The tag must not already exist, either locally or on the remote.

# --- Configuration ---
# Makes the script exit immediately if a command exits with a non-zero status.
set -e
# Treat unset variables as an error when substituting.
set -u
# Pipelines fail on the first command that fails, not the last.
set -o pipefail

# --- Variables ---
MAIN_BRANCH="main"
REMOTE="origin"
TAG_NAME=${1:-} # Use parameter expansion to avoid unbound variable error if $1 is not set.

# --- Color Definitions for Output ---
COLOR_RED='\033[0;31m'
COLOR_GREEN='\033[0;32m'
COLOR_YELLOW='\033[0;33m'
COLOR_BOLD='\033[1m'
COLOR_RESET='\033[0m'

# --- Helper Functions ---
log_error() {
  echo -e "${COLOR_RED}ERROR: $1${COLOR_RESET}"
}

log_success() {
  echo -e "${COLOR_GREEN}$1${COLOR_RESET}"
}

log_info() {
  echo -e "${COLOR_YELLOW}$1${COLOR_RESET}"
}

# --- Pre-flight Checks ---

# 1. Check for tag name argument
if [[ -z "$TAG_NAME" ]]; then
  log_error "No tag name provided."
  echo "Usage: $0 <tag-name> (e.g., v1.2.3)"
  exit 1
fi

# 2. Check if we are in a Git repository
if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    log_error "This is not a Git repository."
    exit 1
fi

# 3. Check if on the correct branch
CURRENT_BRANCH=$(git branch --show-current)
if [[ "$CURRENT_BRANCH" != "$MAIN_BRANCH" ]]; then
  log_error "You must be on the '$MAIN_BRANCH' branch to create a tag. You are currently on '$CURRENT_BRANCH'."
  exit 1
fi

# 4. Check for a clean working directory
if [[ -n $(git status --porcelain) ]]; then
  log_error "Your working directory is not clean. Please commit or stash your changes."
  git status -s
  exit 1
fi

# 5. Fetch the latest from the remote to ensure our checks are up-to-date
log_info "Fetching latest updates from '$REMOTE'..."
git fetch "$REMOTE"

# 6. Check if the local branch is synchronized with the remote
SYNC_STATUS=$(git rev-list --count --left-right "${REMOTE}/${MAIN_BRANCH}...HEAD")
read -r AHEAD BEHIND <<<"${SYNC_STATUS//$'\t'/ }" # Split the tab-separated output

if [[ "$BEHIND" -ne 0 ]]; then
    log_error "Your local '$MAIN_BRANCH' branch is behind '${REMOTE}/${MAIN_BRANCH}'. Please pull the latest changes."
    exit 1
fi
if [[ "$AHEAD" -ne 0 ]]; then
    log_error "Your local '$MAIN_BRANCH' branch has commits that have not been pushed to '${REMOTE}/${MAIN_BRANCH}'. Please push your commits."
    exit 1
fi
log_success "Local branch is synchronized with remote."

# 7. Check if the tag already exists
if git rev-parse --quiet --verify "$TAG_NAME" >/dev/null; then
  log_error "Tag '$TAG_NAME' already exists locally."
  exit 1
fi
if git ls-remote --tags "$REMOTE" | grep -q "refs/tags/$TAG_NAME$"; then
    log_error "Tag '$TAG_NAME' already exists on the remote '$REMOTE'."
    exit 1
fi
log_success "Tag '$TAG_NAME' does not exist."

# --- Confirmation and Execution ---

log_info "\nAll checks passed. You are about to perform the following actions:"
echo "  - Create annotated tag: ${COLOR_BOLD}${TAG_NAME}${COLOR_RESET}"
echo "  - Push tag to remote:   ${COLOR_BOLD}${REMOTE}${COLOR_RESET}"
echo ""

read -p "Are you sure you want to continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_info "Tagging operation cancelled."
    exit 0
fi

log_info "Creating annotated tag '$TAG_NAME'..."
git tag "$TAG_NAME" -a -m "Release version $TAG_NAME"

log_info "Pushing tag '$TAG_NAME' to '$REMOTE'..."
git push "$REMOTE" "$TAG_NAME"

log_success "\n✅ Tag ${COLOR_BOLD}$TAG_NAME${COLOR_RESET} created and pushed successfully."