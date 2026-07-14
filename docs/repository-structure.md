# Repository and Module Structure

```text
android/
  app/                 Compose app, navigation, permissions, notifications
  domain/              Pure Kotlin detection engine and tests
  data/                Room entities, DAOs, DataStore settings, encrypted files
  platform/            Google Play services, Bluetooth, WorkManager adapters

ios/ParkingDetection/
  Package.swift
  Sources/ParkingDetectionCore/  Domain models and detection rules
  Tests/ParkingDetectionCoreTests/
ios/ParkingDetectionApp/
  SwiftUI app, MapKit screens, Core Location/Motion/Bluetooth adapters

docs/
  architecture.md
  repository-structure.md
  state-machine.md
  confidence-scoring.md
  android-implementation-plan.md
  ios-implementation-plan.md
  database-schema.md
  permissions.md
  background-execution.md
  limitations.md
  privacy-policy-template.md
  store-permission-justification.md
```
