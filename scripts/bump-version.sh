#!/bin/bash

# bump-version.sh - Automated version bumping for Android project
# Usage: ./scripts/bump-version.sh [major|minor|patch]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_FILE="$PROJECT_ROOT/app/build.gradle.kts"

# Default bump type
BUMP_TYPE="${1:-patch}"

# Validate bump type
if [[ ! "$BUMP_TYPE" =~ ^(major|minor|patch)$ ]]; then
    echo "Error: Invalid bump type '$BUMP_TYPE'. Use: major, minor, or patch"
    exit 1
fi

# Extract current version
CURRENT_VERSION=$(grep 'versionName = ' "$BUILD_FILE" | head -1 | sed 's/.*"\(.*\)".*/\1/')
CURRENT_CODE=$(grep 'versionCode = ' "$BUILD_FILE" | head -1 | awk '{print $3}')

if [[ -z "$CURRENT_VERSION" ]]; then
    echo "Error: Could not extract current version from $BUILD_FILE"
    exit 1
fi

echo "Current version: $CURRENT_VERSION (code: $CURRENT_CODE)"

# Parse version components
if [[ $CURRENT_VERSION =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    MAJOR=${BASH_REMATCH[1]}
    MINOR=${BASH_REMATCH[2]}
    PATCH=${BASH_REMATCH[3]}
else
    echo "Error: Version format not recognized. Expected format: x.y.z"
    exit 1
fi

# Calculate new version
case $BUMP_TYPE in
    major)
        NEW_MAJOR=$((MAJOR + 1))
        NEW_MINOR=0
        NEW_PATCH=0
        ;;
    minor)
        NEW_MAJOR=$MAJOR
        NEW_MINOR=$((MINOR + 1))
        NEW_PATCH=0
        ;;
    patch)
        NEW_MAJOR=$MAJOR
        NEW_MINOR=$MINOR
        NEW_PATCH=$((PATCH + 1))
        ;;
esac

NEW_VERSION="$NEW_MAJOR.$NEW_MINOR.$NEW_PATCH"
NEW_CODE=$((CURRENT_CODE + 1))

echo "New version: $NEW_VERSION (code: $NEW_CODE)"

# Update build.gradle.kts
sed -i "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$BUILD_FILE"
sed -i "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" "$BUILD_FILE"

echo "Updated $BUILD_FILE"

# Create git tag if in git repository
if git rev-parse --git-dir > /dev/null 2>&1; then
    echo "Creating git tag v$NEW_VERSION"
    git add "$BUILD_FILE"
    git commit -m "Bump version to $NEW_VERSION"
    git tag "v$NEW_VERSION"
    echo "Git tag v$NEW_VERSION created"
    echo ""
    echo "To push the changes and trigger release:"
    echo "  git push origin main"
    echo "  git push origin v$NEW_VERSION"
else
    echo "Not a git repository, skipping tag creation"
fi

echo "Version bump complete!"