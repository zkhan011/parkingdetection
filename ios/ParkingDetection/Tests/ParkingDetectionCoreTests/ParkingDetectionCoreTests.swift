import Testing
@testable import ParkingDetectionCore

@Test func confidenceReachesParkedThreshold() { var s = DetectionSignals(); s.wasInVehicle=true; s.maxTripSpeedKmh=50; s.lowSpeedDurationSeconds=120; s.transitionedVehicleToWalking=true; s.knownBluetoothDisconnected=true; s.walkingAwayDistanceMeters=35; let result = ConfidenceScorer().score(s); #expect(result.0 >= 70); #expect(result.1.contains(.combinedSensorFusion)) }
@Test func falseStopScoresLow() { var s = DetectionSignals(); s.wasInVehicle=true; s.maxTripSpeedKmh=40; s.stopDurationSeconds=30; s.drivingResumed=true; s.majorRoadOrJunction=true; let result = ConfidenceScorer().score(s); #expect(result.0 < 30) }
