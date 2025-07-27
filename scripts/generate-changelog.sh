#!/bin/bash

# generate-changelog.sh - Generate changelog from git commits
# Usage: ./scripts/generate-changelog.sh [from_tag] [to_tag]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
CHANGELOG_FILE="$PROJECT_ROOT/CHANGELOG.md"

FROM_TAG="${1:-}"
TO_TAG="${2:-HEAD}"

# Function to get commits between two references
get_commits() {
    local from="$1"
    local to="$2"
    
    if [[ -z "$from" ]]; then
        # Get all commits if no starting point specified
        git log --oneline --reverse "$to"
    else
        # Get commits between tags
        git log --oneline --reverse "${from}..${to}"
    fi
}

# Function to categorize commits
categorize_commit() {
    local commit="$1"
    local message=$(echo "$commit" | cut -d' ' -f2-)
    local hash=$(echo "$commit" | cut -d' ' -f1)
    
    # Categorize based on conventional commit format or keywords
    if [[ "$message" =~ ^feat(\(.*\))?:.*$ ]]; then
        echo "### ✨ Features"
        echo "- $message ($hash)"
    elif [[ "$message" =~ ^fix(\(.*\))?:.*$ ]]; then
        echo "### 🐛 Bug Fixes"
        echo "- $message ($hash)"
    elif [[ "$message" =~ ^docs(\(.*\))?:.*$ ]]; then
        echo "### 📚 Documentation"
        echo "- $message ($hash)"
    elif [[ "$message" =~ ^style(\(.*\))?:.*$ ]]; then
        echo "### 💎 Style"
        echo "- $message ($hash)"
    elif [[ "$message" =~ ^refactor(\(.*\))?:.*$ ]]; then
        echo "### ♻️ Refactor"
        echo "- $message ($hash)"
    elif [[ "$message" =~ ^test(\(.*\))?:.*$ ]]; then
        echo "### 🧪 Tests"
        echo "- $message ($hash)"
    elif [[ "$message" =~ ^chore(\(.*\))?:.*$ ]]; then
        echo "### 🔧 Chores"
        echo "- $message ($hash)"
    elif [[ "$message" =~ ^ci(\(.*\))?:.*$ ]]; then
        echo "### 👷 CI/CD"
        echo "- $message ($hash)"
    else
        echo "### 📦 Other Changes"
        echo "- $message ($hash)"
    fi
}

# Get version for changelog header
if [[ -n "$TO_TAG" && "$TO_TAG" != "HEAD" ]]; then
    VERSION="$TO_TAG"
else
    # Extract version from build file
    VERSION=$(grep 'versionName = ' "$PROJECT_ROOT/app/build.gradle.kts" | head -1 | sed 's/.*"\(.*\)".*/\1/')
    if [[ -z "$VERSION" ]]; then
        VERSION="Unreleased"
    fi
fi

# Generate changelog
echo "# Changelog"
echo ""
echo "All notable changes to this project will be documented in this file."
echo ""
echo "The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),"
echo "and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)."
echo ""

# Current version section
echo "## [$VERSION] - $(date +%Y-%m-%d)"
echo ""

# Get commits and categorize them
commits=$(get_commits "$FROM_TAG" "$TO_TAG")

if [[ -z "$commits" ]]; then
    echo "No commits found between $FROM_TAG and $TO_TAG"
    exit 0
fi

# Group commits by category
declare -A categories
while IFS= read -r commit; do
    if [[ -n "$commit" ]]; then
        category=$(categorize_commit "$commit" | head -1)
        item=$(categorize_commit "$commit" | tail -1)
        
        if [[ -n "${categories[$category]}" ]]; then
            categories[$category]="${categories[$category]}"$'\n'"$item"
        else
            categories[$category]="$item"
        fi
    fi
done <<< "$commits"

# Output categories in order
for category in "### ✨ Features" "### 🐛 Bug Fixes" "### 📚 Documentation" "### ♻️ Refactor" "### 💎 Style" "### 🧪 Tests" "### 👷 CI/CD" "### 🔧 Chores" "### 📦 Other Changes"; do
    if [[ -n "${categories[$category]}" ]]; then
        echo "$category"
        echo ""
        echo "${categories[$category]}"
        echo ""
    fi
done

echo "---"
echo ""
echo "Generated on $(date) by generate-changelog.sh"