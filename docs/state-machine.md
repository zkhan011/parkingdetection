# Parking Detection State Machine

```mermaid
stateDiagram-v2
  IDLE --> POSSIBLE_VEHICLE_JOURNEY: in-vehicle activity or vehicle speed
  POSSIBLE_VEHICLE_JOURNEY --> IN_VEHICLE: sustained vehicle motion
  IN_VEHICLE --> POSSIBLE_PARKING: speed near zero or vehicle-to-walking transition
  POSSIBLE_PARKING --> PARKING_CONFIRMATION: candidate score below threshold but plausible
  PARKING_CONFIRMATION --> PARKED: score >= threshold and no driving resumed
  PARKING_CONFIRMATION --> DRIVING_RESUMED: vehicle activity or speed returns
  PARKED --> USER_REJECTED: user taps Not parked here
  DRIVING_RESUMED --> IN_VEHICLE: continue journey
  USER_REJECTED --> IDLE: feedback stored
```

The candidate is reset if driving resumes during confirmation. The saved location is selected from the rolling driving buffer rather than the final walking location.
