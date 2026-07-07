# Parking Detection

Native Android and native iOS application workspace for privacy-first automatic parked-car detection. The repository starts with shared behavioural documentation plus compilable platform domain cores that implement the state machine, confidence scoring, location buffering, duplicate prevention, and simulator-friendly tests.

## Structure

```text
android/domain/                 Kotlin domain module used by the Android app
ios/ParkingDetection/           Swift Package for iOS domain logic
docs/                           Architecture, schema, permissions, background, privacy, store text
```

## Build and test

```bash
gradle :android:domain:test
cd ios/ParkingDetection && swift test
```

Full Android app modules and iOS SwiftUI app targets are planned in phased implementation documents.
