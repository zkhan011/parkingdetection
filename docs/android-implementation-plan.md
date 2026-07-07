# Android Implementation Plan

1. Create `:android:app`, `:android:data`, and `:android:platform` modules around the existing `:android:domain` module.
2. Use Hilt for dependency injection and expose use cases to Compose ViewModels.
3. Persist vehicles, parking records, detection sessions, events, feedback, and cached geocodes in Room.
4. Store scoring settings and privacy toggles in DataStore.
5. Use Activity Recognition Transition API for `IN_VEHICLE`, `STILL`, `WALKING`, and `ON_FOOT` transitions.
6. Use Fused Location with adaptive priorities by detection state.
7. Use Bluetooth APIs for known device connection state and Android Auto best-effort signals where supported.
8. Use WorkManager for reverse geocoding retries and cleanup; use geofencing for parked-region awareness.
9. Use a foreground service only for active detection sessions that require reliable background updates, with a persistent notification.
10. Build screens: Home, Map, History, Vehicle Management, Settings, Onboarding, Privacy, Manual Parking.
