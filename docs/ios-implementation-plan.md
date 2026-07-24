# iOS Implementation Plan

1. Reuse `ParkingDetectionCore` for state and scoring rules.
2. Create a SwiftUI app target with MVVM view models and an app composition container.
3. Persist app data with SwiftData, or Core Data if broader deployment requires it.
4. Use Core Location significant-location-change monitoring, region monitoring, and temporary high-accuracy requests during candidate confirmation.
5. Use Core Motion activity updates for automotive and walking transitions.
6. Use Core Bluetooth for known vehicle device state; use CarPlay connection signals only through supported public APIs.
7. Use Background Tasks for deferred geocoding and maintenance without prohibited continuous background execution.
8. Use MapKit for parked marker, user location, accuracy radius, and walking route.
9. Restore monitoring after relaunch and document that iOS may not deliver every background event.
10. Build parity screens with SwiftUI and local-first privacy controls.
