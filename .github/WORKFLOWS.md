# GitHub Actions Workflows

This repository has two GitHub Actions workflows:

## 1. Continuous Integration (CI)

**File:** `.github/workflows/ci.yml`  
**Triggers:** 
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches

**What it does:**
- Builds debug APK
- Runs unit tests
- Runs Android instrumented tests
- Uploads debug APK as artifact

## 2. Release Build

**File:** `.github/workflows/android-auto-release.yml`  
**Triggers:** 
- Push of version tags (format: `v*`, e.g., `v1.0.0`, `v1.2.3`)

**What it does:**
- Builds release APK and AAB
- Creates GitHub release with download links
- Attaches APK and AAB files to the release

## How to Trigger Workflows

### Trigger CI Workflow
Simply push to main/develop branch or create a pull request:
```bash
git push origin main
```

### Trigger Release Workflow
Create and push a version tag:
```bash
# Create a new version tag
git tag v1.0.0

# Push the tag to trigger release
git push origin v1.0.0
```

### Version Tag Format
Use semantic versioning format:
- `v1.0.0` - Major release
- `v1.1.0` - Minor release  
- `v1.1.1` - Patch release

## Troubleshooting

If workflows don't trigger:
1. Check that you have the correct branch names (`main`, `develop`)
2. Ensure version tags start with `v` (e.g., `v1.0.0`, not `1.0.0`)
3. Verify you have push permissions to the repository
4. Check the Actions tab in GitHub to see if workflows ran but failed