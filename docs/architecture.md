# Architecture

## Goals
- Separate native UI/platform integrations from portable domain rules.
- Keep location history local by default and avoid continuous tracking when detection is disabled.
- Make scoring tunable through configuration objects.

## Layers
1. Presentation: Jetpack Compose on Android and SwiftUI on iOS, immutable view state, MVVM.
2. Application: use cases for manual parking, vehicle management, detection session orchestration, address resolution, notification actions, and privacy deletion.
3. Domain: state machine, confidence scorer, location buffer, duplicate guard, data models, simulator events.
4. Data: Room/DataStore on Android; SwiftData/Core Data and Codable settings on iOS.
5. Platform adapters: Activity Recognition, Fused Location, Geofencing, Bluetooth, WorkManager, Core Location, Core Motion, Core Bluetooth, Background Tasks, MapKit.

## Dependency rule
Presentation depends on application interfaces; application depends on domain; data and platform adapters implement interfaces and are injected with Hilt on Android and app composition containers on iOS.

## Key interfaces
- LocationProvider
- ActivityProvider
- BluetoothVehicleProvider
- ParkingRepository
- VehicleRepository
- NotificationScheduler
- GeocoderService
- RouteService
- DetectionSettingsStore
