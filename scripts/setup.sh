#!/bin/bash

# setup.sh - Set up development environment for release automation

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🔧 Setting up DaysLeft development environment..."

# Make all scripts executable
echo "📋 Making scripts executable..."
chmod +x "$SCRIPT_DIR"/*.sh

# Install git hooks
if [ -d "$PROJECT_ROOT/.git" ]; then
    echo "🪝 Installing git hooks..."
    cp "$SCRIPT_DIR/commit-msg" "$PROJECT_ROOT/.git/hooks/"
    chmod +x "$PROJECT_ROOT/.git/hooks/commit-msg"
    echo "✅ Commit message validation hook installed"
else
    echo "⚠️  Not a git repository, skipping git hooks installation"
fi

# Generate initial changelog if it doesn't exist
if [ ! -f "$PROJECT_ROOT/CHANGELOG.md" ]; then
    echo "📝 Generating initial changelog..."
    "$SCRIPT_DIR/generate-changelog.sh" > "$PROJECT_ROOT/CHANGELOG.md"
    echo "✅ Changelog generated"
fi

echo ""
echo "🎉 Setup complete!"
echo ""
echo "📋 Available commands:"
echo "  ./scripts/release.sh [patch|minor|major]  - Create a new release"
echo "  ./scripts/bump-version.sh [patch|minor|major]  - Bump version only"
echo "  ./scripts/generate-changelog.sh  - Generate changelog"
echo ""
echo "📚 Documentation:"
echo "  docs/RELEASE_AUTOMATION.md  - Complete guide"
echo ""
echo "🚀 Quick start:"
echo "  ./scripts/release.sh patch"