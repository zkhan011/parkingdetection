# Parking Detection

Native Android and native iOS application workspace for privacy-first automatic parked-car detection. The repository contains a pure Kotlin/JVM detection domain, Android app/data modules, Swift domain package, documentation, and CI test workflow.

## Structure

```text
android/app/                    Native Android application module that builds the debug APK
android/data/                   Android library module for Room, DataStore, location, Bluetooth, and detection adapters
android/domain/                 Pure Kotlin/JVM domain module used by the Android app
ios/ParkingDetection/           Swift Package for iOS domain logic
docs/                           Architecture, schema, permissions, background, privacy, store text
.github/workflows/native-tests.yml  CI for Android domain/app and iOS core tests
```

## Requirements

- Java 17 for Android Gradle Plugin 8.8.2 and Kotlin compilation.
- Gradle 8.10.2 available on `PATH`; `./gradlew` is a text-only launcher because this repository cannot store binary wrapper JARs.
- Android SDK with API 35 installed and `ANDROID_HOME` or `ANDROID_SDK_ROOT` configured.
- Network access to `plugins.gradle.org`, Maven Central, and Google Maven the first time Gradle resolves plugins or dependencies.
- Swift 6 or a compatible toolchain for the iOS package tests.

## Build and test

From the repository root, run the Android tests and build the debug APK:

```bash
export JAVA_HOME="$HOME/.local/share/mise/installs/java/17.0.2"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew clean test :android:app:assembleDebug --stacktrace
```

The Android app module path is `:android:app`. The expected debug APK path is:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

Install it on an emulator or device with:

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.zishan.parkingdetection 1
```

Android test reports are generated under:

```text
android/domain/build/reports/tests/
android/data/build/reports/tests/
android/app/build/reports/tests/
```

Run the iOS core tests with Swift Package Manager:

```bash
cd ios/ParkingDetection
swift test
```

## Android app capabilities

The Android app provides manual parking save, a current saved-parking screen, history, detection settings, permission onboarding, local Room persistence, DataStore settings, Hilt injection, background component declarations, foreground-service declaration, geo-intent navigation, and local-first privacy guidance. Automatic detection adapters are structured for Activity Recognition, Bluetooth, location stability, walking-away confirmation, and cooldown-aware confidence scoring.
