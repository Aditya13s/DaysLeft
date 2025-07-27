#!/bin/bash

# release.sh - Complete release automation script
# Usage: ./scripts/release.sh [major|minor|patch]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Default bump type
BUMP_TYPE="${1:-patch}"

echo "🚀 Starting release process with $BUMP_TYPE version bump..."

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo "❌ Error: Not in a git repository"
    exit 1
fi

# Check if working directory is clean
if ! git diff-index --quiet HEAD --; then
    echo "❌ Error: Working directory is not clean. Please commit or stash changes first."
    exit 1
fi

# Get current version and tag for changelog
CURRENT_VERSION=$(grep 'versionName = ' "$PROJECT_ROOT/app/build.gradle.kts" | head -1 | sed 's/.*"\(.*\)".*/\1/')
CURRENT_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")

echo "📋 Current version: $CURRENT_VERSION"
if [[ -n "$CURRENT_TAG" ]]; then
    echo "📋 Current tag: $CURRENT_TAG"
fi

# Generate changelog for the release
echo "📝 Generating changelog..."
CHANGELOG_CONTENT=$("$SCRIPT_DIR/generate-changelog.sh" "$CURRENT_TAG")

# Write changelog to file
echo "$CHANGELOG_CONTENT" > "$PROJECT_ROOT/CHANGELOG.md"
echo "✅ Changelog generated: CHANGELOG.md"

# Bump version
echo "⬆️  Bumping version ($BUMP_TYPE)..."
"$SCRIPT_DIR/bump-version.sh" "$BUMP_TYPE"

# Get new version
NEW_VERSION=$(grep 'versionName = ' "$PROJECT_ROOT/app/build.gradle.kts" | head -1 | sed 's/.*"\(.*\)".*/\1/')
echo "✅ Version bumped to: $NEW_VERSION"

# Update changelog with new version
echo "📝 Updating changelog with new version..."
sed -i "s/## \[$CURRENT_VERSION\]/## [$NEW_VERSION]/" "$PROJECT_ROOT/CHANGELOG.md"

# Add changelog to the version bump commit
git add "$PROJECT_ROOT/CHANGELOG.md"
git commit --amend --no-edit

# Update the tag to include changelog
git tag -d "v$NEW_VERSION"
git tag "v$NEW_VERSION"

echo ""
echo "🎉 Release v$NEW_VERSION is ready!"
echo ""
echo "📋 Summary:"
echo "  - Version: $CURRENT_VERSION → $NEW_VERSION"
echo "  - Changelog updated: CHANGELOG.md"
echo "  - Git tag created: v$NEW_VERSION"
echo ""
echo "🚀 To complete the release, run:"
echo "  git push origin main"
echo "  git push origin v$NEW_VERSION"
echo ""
echo "This will trigger the automated build and release on GitHub Actions."