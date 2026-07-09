# Database Schema

Entities: Vehicle, ParkingRecord, LocationSample, DetectionSession, DetectionEvent, DetectionConfiguration, UserFeedback, CachedGeocode.

`ParkingRecord` contains id, vehicleId, latitude, longitude, accuracy, address, placeName, parkedAt, detectedAt, detectionMethod set, confidenceScore, isAutomatic, isConfirmed, floorNumber, parkingSlot, note, photoPath, meterExpiryTime, createdAt, updatedAt.

Sensitive fields such as notes, photos, and exact coordinates should be encrypted when platform storage permits. Index records by parkedAt, vehicleId, and approximate coordinate hash for duplicate detection.
