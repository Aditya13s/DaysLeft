# DaysLeft

An Android application to track important events and count down the days remaining until they occur.

## 📱 About

DaysLeft helps you keep track of important dates and events in your life by showing you exactly how many days are left until each event. Whether it's birthdays, anniversaries, deadlines, or any special occasions, DaysLeft ensures you never miss what matters most.

## ✨ Features

- **Event Tracking**: Add events with titles and dates
- **Countdown Display**: See exactly how many days remain for each event
- **Event Management**: Edit or delete events as needed
- **Clean Interface**: Modern Material Design with intuitive navigation
- **Local Storage**: All your events are stored securely on your device

## 🏗️ Architecture

Built using modern Android development practices:
- **MVVM Architecture** for separation of concerns
- **Room Database** for local data persistence
- **Data Binding** for reactive UI updates
- **Repository Pattern** for data management
- **Use Cases** for business logic

## 🚀 Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK with minimum API level 24

### Build Instructions

```bash
# Clone the repository
git clone https://github.com/Aditya13s/DaysLeft.git
cd DaysLeft

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build release AAB (for Play Store)
./gradlew bundleRelease
```

### Output Files
- **Debug APK**: `app/build/outputs/apk/debug/`
- **Release APK**: `app/build/outputs/apk/release/`
- **Release AAB**: `app/build/outputs/bundle/release/`

## 📦 Installation

### From Release
1. Download the latest APK from the [Releases](https://github.com/Aditya13s/DaysLeft/releases) page
2. Enable "Install from unknown sources" in your Android settings
3. Install the downloaded APK

### From Source
1. Open the project in Android Studio
2. Connect your Android device or start an emulator
3. Click "Run" or press Ctrl+R

## 🎯 Usage

1. **Add an Event**: Tap the "+" button and enter event details
2. **View Countdown**: Events are displayed with days remaining
3. **Edit Event**: Tap on any event to modify its details
4. **Delete Event**: Use the delete option to remove events

## 🔧 Development

The app follows clean architecture principles with clear separation between:
- **Presentation Layer**: Activities, ViewModels, and UI components
- **Domain Layer**: Use cases and business logic
- **Data Layer**: Repository implementation and database entities

### Key Components
- `MainActivity`: Main screen displaying event list
- `EventViewModel`: Manages UI state and business logic
- `EventRepository`: Handles data operations
- `AppDatabase`: Room database configuration

## 📋 Releases

Releases are automatically built and published when version tags are pushed to the repository. Each release includes both APK and AAB files for easy distribution.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is open source. See the repository for license details.