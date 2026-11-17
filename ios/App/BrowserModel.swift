import Foundation
import SwiftUI
import WebKit

@MainActor
final class BrowserModel: NSObject, ObservableObject, WKNavigationDelegate, WKUIDelegate, WKScriptMessageHandler {
    @Published var blockingEnabled: Bool {
        didSet {
            UserDefaults.standard.set(blockingEnabled, forKey: "blockingEnabled")
            installFilter();
            webView.evaluateJavaScript("window.ReelGuardFilter?.setEnabled(\(blockingEnabled ? "true" : "false"));", completionHandler: nil)
            if !blockingEnabled { showBlocked = false }
        }
    }
    @Published var showBlocked = false
    @Published var errorMessage: String?
    @Published var loading = false
    let webView: WKWebView
    private var started = false
    private let filterSource: String

    override init() {
        blockingEnabled = UserDefaults.standard.object(forKey: "blockingEnabled") as? Bool ?? true
        let configuration = WKWebViewConfiguration()
        configuration.websiteDataStore = .default()
        configuration.allowsInlineMediaPlayback = true
        // All web video requires a deliberate tap in this prototype.
        configuration.mediaTypesRequiringUserActionForPlayback = .all
        webView = WKWebView(frame: .zero, configuration: configuration)
        if let url = Bundle.main.url(forResource: "reel-filter", withExtension: "js"),
           let source = try? String(contentsOf: url, encoding: .utf8) {
            filterSource = source
        } else {
            filterSource = ""
        }
        super.init()
        webView.navigationDelegate = self
        webView.uiDelegate = self
        webView.allowsBackForwardNavigationGestures = true
        webView.configuration.userContentController.add(WeakScriptHandler(self), name: "reelguard")
        installFilter()
    }

    private func installFilter() {
        let controller = webView.configuration.userContentController
        controller.removeAllUserScripts()
        let prefix = "window.__REELGUARD_INITIAL_ENABLED__ = \(blockingEnabled ? "true" : "false");\n"
        controller.addUserScript(WKUserScript(source: prefix + filterSource, injectionTime: .atDocumentStart, forMainFrameOnly: false))
    }

    func start() {
        guard !started else { return }
        started = true
        goHome()
    }
    func goHome() { open("https://www.instagram.com/") }
    func openDMs() { open("https://www.instagram.com/direct/inbox/") }
    func reload() { errorMessage = nil; webView.reload() }
    private func open(_ address: String) {
        guard !filterSource.isEmpty else {
            errorMessage = "The Reels filter is missing from this build. Instagram has not been opened."
            return
        }
        showBlocked = false
        errorMessage = nil
        if let url = URL(string: address) { webView.load(URLRequest(url: url)) }
    }
    func clearSavedLogin() {
        let types = WKWebsiteDataStore.allWebsiteDataTypes()
        webView.stopLoading()
        webView.configuration.websiteDataStore.removeData(ofTypes: types, modifiedSince: .distantPast) { [weak self] in
            DispatchQueue.main.async { self?.goHome() }
        }
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard message.name == "reelguard", blockingEnabled,
              let body = message.body as? [String: String], body["type"] == "blocked" else { return }
        showBlocked = true
    }

    func webView(_ webView: WKWebView, decidePolicyFor action: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        // Embedded HTTPS frames may be needed for Instagram authentication.
        if action.targetFrame?.isMainFrame == false, action.request.url?.scheme == "https",
           let url = action.request.url, !ReelPolicy.isReel(url) {
            decisionHandler(.allow)
            return
        }
        switch ReelPolicy.decide(action.request.url, blocking: blockingEnabled) {
        case .allow: decisionHandler(.allow)
        case .blockReel:
            showBlocked = true
            decisionHandler(.cancel)
        case .unsupported:
            errorMessage = "This prototype keeps navigation inside instagram.com. External links and opening the official Instagram app are disabled here."
            decisionHandler(.cancel)
        }
    }

    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for action: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        if action.targetFrame == nil, let url = action.request.url {
            switch ReelPolicy.decide(url, blocking: blockingEnabled) {
            case .allow: webView.load(action.request)
            case .blockReel: showBlocked = true
            case .unsupported: errorMessage = "External windows are unavailable in this prototype."
            }
        }
        return nil
    }
    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) { loading = true; errorMessage = nil }
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) { loading = false }
    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) { report(error) }
    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) { report(error) }
    private func report(_ error: Error) {
        loading = false
        if (error as NSError).code != NSURLErrorCancelled { errorMessage = error.localizedDescription }
    }
}

private final class WeakScriptHandler: NSObject, WKScriptMessageHandler {
    weak var target: WKScriptMessageHandler?
    init(_ target: WKScriptMessageHandler) { self.target = target }
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        target?.userContentController(userContentController, didReceive: message)
    }
}
