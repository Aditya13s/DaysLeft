# Release Automation Guide

This document describes the automated release process for the DaysLeft Android application.

## Overview

The release automation system provides:
- 🔄 Automatic version bumping based on commit messages
- 📝 Automated changelog generation
- 🏷️ Git tag creation
- 📦 Automated APK and AAB builds
- 🚀 GitHub releases with proper release notes

## Quick Start

### Manual Release (Recommended)

To create a new release manually:

```bash
# For a patch release (1.0.0 → 1.0.1)
./scripts/release.sh patch

# For a minor release (1.0.0 → 1.1.0)
./scripts/release.sh minor

# For a major release (1.0.0 → 2.0.0)
./scripts/release.sh major

# Push the release
git push origin main
git push origin v1.0.1  # Replace with actual version
```

### Automatic Release (On Push to Main)

Releases are automatically created when code is pushed to the `main` branch. The version bump type is determined by commit message conventions:

- `feat:` commits trigger **minor** version bumps
- `feat!:` or commits with `BREAKING CHANGE` trigger **major** version bumps
- Other commits trigger **patch** version bumps

### Manual Trigger

You can manually trigger a version bump from the GitHub Actions tab:
1. Go to Actions → Auto Version Bump
2. Click "Run workflow"
3. Select the bump type (patch/minor/major)
4. Click "Run workflow"

## Scripts

### `scripts/bump-version.sh`

Bumps the version in `app/build.gradle.kts` and creates a git tag.

**Usage:**
```bash
./scripts/bump-version.sh [major|minor|patch]
```

**What it does:**
- Extracts current version from `app/build.gradle.kts`
- Calculates new version based on bump type
- Updates `versionName` and `versionCode` in build file
- Creates a git commit and tag

### `scripts/generate-changelog.sh`

Generates a changelog from git commit history.

**Usage:**
```bash
./scripts/generate-changelog.sh [from_tag] [to_tag]
```

**What it does:**
- Analyzes git commit messages
- Categorizes commits by type (features, fixes, etc.)
- Generates formatted markdown changelog
- Supports conventional commit format

### `scripts/release.sh`

Complete release automation combining version bump and changelog generation.

**Usage:**
```bash
./scripts/release.sh [major|minor|patch]
```

**What it does:**
- Generates changelog from git history
- Bumps version using bump-version.sh
- Updates changelog with new version
- Creates release commit and tag
- Provides instructions for pushing

## Commit Message Conventions

For automatic version bumping to work correctly, use conventional commit messages:

### Patch Release (1.0.0 → 1.0.1)
```
fix: resolve app crash on startup
docs: update installation instructions
style: fix code formatting
refactor: improve code structure
test: add unit tests
chore: update dependencies
ci: fix build pipeline
```

### Minor Release (1.0.0 → 1.1.0)
```
feat: add dark mode support
feature: implement user authentication
```

### Major Release (1.0.0 → 2.0.0)
```
feat!: redesign user interface
feat: add new feature

BREAKING CHANGE: API endpoints have changed
```

## GitHub Actions Workflows

### `android-auto-release.yml`

Triggered when a version tag is pushed (e.g., `v1.0.1`).

**What it does:**
- Sets up Android build environment
- Builds release APK and AAB
- Extracts version information
- Generates release notes from changelog
- Creates GitHub release with artifacts

### `auto-version-bump.yml`

Triggered on pushes to `main` branch or manual dispatch.

**What it does:**
- Analyzes commit messages to determine bump type
- Runs the release script
- Pushes new version and tag
- Triggers the release workflow

## File Structure

```
project/
├── scripts/
│   ├── bump-version.sh       # Version bumping
│   ├── generate-changelog.sh # Changelog generation
│   └── release.sh           # Complete release process
├── .github/workflows/
│   ├── android-auto-release.yml  # Release builds
│   └── auto-version-bump.yml     # Version automation
├── CHANGELOG.md             # Generated changelog
└── app/build.gradle.kts     # Version configuration
```

## Version Configuration

Versions are managed in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 1        # Incremented automatically
    versionName = "1.0.0"  # Semantic version
}
```

## Troubleshooting

### Common Issues

**Build fails after version bump:**
- Ensure all dependencies are compatible
- Check Android Gradle Plugin version
- Verify Java/Kotlin versions

**Workflow doesn't trigger:**
- Check if commit message contains "Bump version to" (skipped to avoid loops)
- Verify branch protection rules
- Check GitHub Actions permissions

**Version extraction fails:**
- Ensure `app/build.gradle.kts` uses the exact format shown above
- Check for syntax errors in build file

### Manual Recovery

If automation fails, you can manually:

1. Reset to previous commit: `git reset --hard HEAD~1`
2. Delete problematic tag: `git tag -d v1.0.1`
3. Fix issues and re-run release script

## Best Practices

1. **Use conventional commits** for automatic categorization
2. **Test releases** in a feature branch first
3. **Review generated changelog** before pushing
4. **Keep build files clean** to ensure version extraction works
5. **Monitor GitHub Actions** for any workflow failures

## Security Notes

- Scripts use `set -e` to fail fast on errors
- No sensitive data is exposed in logs
- Git operations use repository token with appropriate permissions
- Version tags are signed automatically by GitHub Actions

---

For more information, see individual script files or GitHub Actions workflow files.