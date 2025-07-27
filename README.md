# DaysLeft

An Android application to track days remaining for important events.

## Development

### Release Automation

This project includes comprehensive release automation. See [Release Automation Guide](docs/RELEASE_AUTOMATION.md) for details.

**Quick release:**
```bash
./scripts/release.sh patch  # For bug fixes
./scripts/release.sh minor  # For new features  
./scripts/release.sh major  # For breaking changes
```

### Building

```bash
./gradlew assembleDebug     # Debug APK
./gradlew assembleRelease   # Release APK
./gradlew bundleRelease     # Release AAB
```