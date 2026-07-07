// swift-tools-version: 6.0
import PackageDescription
let package = Package(name: "ParkingDetection", platforms: [.iOS(.v17)], products: [.library(name: "ParkingDetectionCore", targets: ["ParkingDetectionCore"])], targets: [.target(name: "ParkingDetectionCore"), .testTarget(name: "ParkingDetectionCoreTests", dependencies: ["ParkingDetectionCore"])])
