// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "ReelGuardCore",
    platforms: [.iOS(.v16), .macOS(.v13)],
    products: [.library(name: "ReelGuardCore", targets: ["ReelGuardCore"])],
    targets: [
        .target(name: "ReelGuardCore"),
        .executableTarget(name: "ReelGuardPolicyChecks", dependencies: ["ReelGuardCore"], path: "Checks")
    ]
)
