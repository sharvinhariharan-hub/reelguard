import Foundation
import ReelGuardCore

var checks = 0
func expect(_ value: String?, blocking: Bool = true, _ expected: NavigationDecision) {
    let actual = ReelPolicy.decide(value.flatMap(URL.init(string:)), blocking: blocking)
    guard actual == expected else { fatalError("Policy failed for \(value ?? "nil"): \(actual), expected \(expected)") }
    checks += 1
}
for path in ["/reels", "/reels/", "/reel/abc/?utm_source=dm", "/REEL/abc", "/%72eel/abc"] {
    expect("https://www.instagram.com" + path, .blockReel)
}
for path in ["/", "/direct/inbox/", "/direct/t/123/", "/p/photo/", "/stories/person/123/", "/accounts/login/", "/person/", "/reelguard/"] {
    expect("https://www.instagram.com" + path, .allow)
}
expect("https://instagram.com/reel/abc/", blocking: false, .allow)
for value in ["https://instagram.com.evil.example/reel/a", "https://evilinstagram.com/", "instagram://reel/a", "http://instagram.com/", "https://www.instagram.com@evil.example/", "javascript:alert(1)"] {
    expect(value, blocking: false, .unsupported)
}
expect("about:blank", .allow)
expect(nil, .unsupported)
print("PASS: \(checks) native navigation policy checks")
