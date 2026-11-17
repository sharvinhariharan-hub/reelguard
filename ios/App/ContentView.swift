import SwiftUI
import WebKit

@MainActor
struct ContentView: View {
    @StateObject private var browser = BrowserModel()
    @AppStorage("introAccepted") private var introAccepted = false
    @State private var confirmingClear = false
    private let green = Color(red: 0.09, green: 0.38, blue: 0.31)

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Image(systemName: "shield.lefthalf.filled").foregroundStyle(green)
                Text("ReelGuard").font(.headline)
                Spacer()
                Toggle("Block Reels", isOn: $browser.blockingEnabled).fixedSize()
                    .accessibilityIdentifier("blockingToggle")
            }.padding()
            HStack {
                Label("instagram.com · inside ReelGuard", systemImage: "lock.fill")
                    .font(.caption).foregroundStyle(.secondary)
                Spacer()
                if browser.loading { ProgressView().accessibilityLabel("Loading Instagram") }
            }.padding(.horizontal).padding(.bottom, 8)
            if let error = browser.errorMessage {
                VStack(alignment: .leading, spacing: 8) {
                    Text(error).font(.callout)
                    Button("Dismiss") { browser.errorMessage = nil }
                }.padding().frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.orange.opacity(0.12))
            }
            ZStack {
                InstagramView(webView: browser.webView)
                if browser.showBlocked {
                    VStack(spacing: 20) {
                        Image(systemName: "pause.circle.fill").font(.system(size: 52)).foregroundStyle(green)
                        Text("Reels are blocked").font(.title.bold())
                        Text("Your posts and conversations are still available.")
                            .multilineTextAlignment(.center)
                        Button("Back to posts", action: browser.goHome).buttonStyle(.borderedProminent).tint(green)
                        Button("Open messages", action: browser.openDMs).buttonStyle(.bordered)
                        Button("Turn blocking off") { browser.blockingEnabled = false }
                    }.padding(28).frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color(red: 0.96, green: 0.96, blue: 0.94))
                        .accessibilityIdentifier("blockedScreen")
                }
            }
            Divider()
            HStack {
                Button(action: browser.goHome) { Label("Posts", systemImage: "house") }
                Spacer()
                Button(action: browser.openDMs) { Label("DMs", systemImage: "paperplane") }
                Spacer()
                Menu {
                    Button("Reload", action: browser.reload)
                    Button("About this prototype") { introAccepted = false }
                    Button("Clear saved login", role: .destructive) { confirmingClear = true }
                } label: { Label("More", systemImage: "ellipsis.circle") }
            }.padding().tint(green)
        }
        .background(Color(red: 0.96, green: 0.96, blue: 0.94))
        .onAppear { if introAccepted { browser.start() } }
        .fullScreenCover(isPresented: Binding(get: { !introAccepted }, set: { _ in })) {
            ScrollView {
                VStack(alignment: .leading, spacing: 22) {
                    Image(systemName: "shield.lefthalf.filled").font(.system(size: 52)).foregroundStyle(green)
                    Text("Your attention.\nYour choice.").font(.largeTitle.bold())
                    Text("An Instagram app with a Reels filter.").font(.title2)
                    Text("This app opens Instagram’s website inside its own window. It does not modify or block the official Instagram app. Opening that app bypasses this filter.")
                    Text("The filter blocks known Reels links and pages, including shared Reels in DMs. Instagram updates or unidentified media can escape it. Posts and DMs use Instagram’s web features, which may differ from the official app. All video requires a tap to play.")
                    Text("Sign in on Instagram’s own webpage. This project has no backend or analytics. Instagram receives your normal browsing traffic; saved login data stays in this app’s WebKit store. You can clear it under More.")
                    Text("Prototype: live Instagram login and playback have not been verified. This is not a guarantee that every Reel is inaccessible.").font(.callout).foregroundStyle(.secondary)
                    Button("Continue to Instagram") {
                        introAccepted = true
                        browser.start()
                    }.buttonStyle(.borderedProminent).tint(green)
                        .accessibilityIdentifier("continueButton")
                }.padding(28)
            }.interactiveDismissDisabled()
        }
        .confirmationDialog("Clear Instagram login data from this app?", isPresented: $confirmingClear, titleVisibility: .visible) {
            Button("Clear saved login", role: .destructive, action: browser.clearSavedLogin)
        }
    }
}

private struct InstagramView: UIViewRepresentable {
    let webView: WKWebView
    func makeUIView(context: Context) -> WKWebView { webView }
    func updateUIView(_ uiView: WKWebView, context: Context) {}
}
