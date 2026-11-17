import Foundation

public enum NavigationDecision: Equatable {
    case allow, blockReel, unsupported
}

public enum ReelPolicy {
    public static func isInstagram(_ url: URL) -> Bool {
        guard url.scheme?.lowercased() == "https", let host = url.host?.lowercased() else { return false }
        return host == "instagram.com" || host == "www.instagram.com"
    }

    public static func isReel(_ url: URL) -> Bool {
        guard isInstagram(url) else { return false }
        let path = url.path.removingPercentEncoding ?? url.path
        let first = path.split(separator: "/").first?.lowercased()
        return first == "reel" || first == "reels"
    }

    public static func decide(_ url: URL?, blocking: Bool) -> NavigationDecision {
        guard let url else { return .unsupported }
        if url.absoluteString == "about:blank" { return .allow }
        guard isInstagram(url) else { return .unsupported }
        return blocking && isReel(url) ? .blockReel : .allow
    }
}
