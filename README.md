# Parking Detection

Native Android and native iOS application workspace for privacy-first automatic parked-car detection. The repository starts with shared behavioural documentation plus compilable platform domain cores that implement the state machine, confidence scoring, location buffering, duplicate prevention, and simulator-friendly tests.

## Structure

```text
android/domain/                 Pure Kotlin/JVM domain module used by the Android app
ios/ParkingDetection/           Swift Package for iOS domain logic
docs/                           Architecture, schema, permissions, background, privacy, store text
.github/workflows/native-tests.yml  CI for Android domain and iOS core tests
```

## Requirements

- Java 21 for Gradle and Kotlin compilation.
- Gradle 8.10.2 available on `PATH`; `./gradlew` is a text-only launcher because this repository cannot store binary wrapper JARs.
- Network access to `plugins.gradle.org`, Maven Central, and Google Maven the first time Gradle resolves plugins or dependencies.
- Swift 6 or a compatible toolchain for the iOS package tests.

## Build and test

From the repository root, run the Android domain tests with the repository Gradle launcher:

```bash
./gradlew :android:domain:test --no-daemon
```

The Android module path is `:android:domain`. Test reports are generated under:

```text
android/domain/build/reports/tests/
android/domain/build/test-results/
```

Run the iOS core tests with Swift Package Manager:

```bash
cd ios/ParkingDetection
swift test
```

Full Android app modules and iOS SwiftUI app targets are planned in phased implementation documents.
