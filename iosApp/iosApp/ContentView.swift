import SwiftUI

struct ContentView: View {
    @StateObject private var store = WanAndroidStore()
    @State private var selectedTab: AppSection = .home
    @AppStorage("appearance") private var appearance = AppAppearance.system.rawValue

    var body: some View {
        TabView(selection: $selectedTab) {
            ForEach(AppSection.allCases) { section in
                NavigationStack {
                    if section == .settings {
                        SettingsView()
                    } else {
                        ArticleFeedView(section: section, store: store) { selectedTab = $0 }
                    }
                }
                .tabItem { Label(section.title, systemImage: section.symbol) }
                .tag(section)
            }
        }
        .preferredColorScheme(AppAppearance(rawValue: appearance)?.colorScheme)
        .onChange(of: selectedTab) { _, section in store.activate(section) }
        .task { store.start() }
    }
}

enum AppAppearance: String, CaseIterable, Identifiable {
    case system, light, dark

    var id: String { rawValue }
    var title: String {
        switch self {
        case .system: "跟随系统"
        case .light: "浅色"
        case .dark: "深色"
        }
    }
    var colorScheme: ColorScheme? {
        switch self {
        case .system: nil
        case .light: .light
        case .dark: .dark
        }
    }
}

private struct SettingsView: View {
    @AppStorage("appearance") private var appearance = AppAppearance.system.rawValue

    var body: some View {
        Form {
            Section {
                VStack(alignment: .leading, spacing: 12) {
                    Image(systemName: "book.pages")
                        .font(.largeTitle)
                        .foregroundStyle(Color.accentColor)
                        .accessibilityHidden(true)
                    Text("让阅读更合心意").font(.title2.bold())
                    Text("选择舒服的外观，留一点时间给知识与灵感。")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .lineSpacing(4)
                }
                .padding(.vertical, 12)
            }
            .listRowBackground(Color.accentColor.opacity(0.10))

            Section {
                ForEach(AppAppearance.allCases) { option in
                    Button { appearance = option.rawValue } label: {
                        HStack(spacing: 14) {
                            Image(systemName: option.symbol)
                                .foregroundStyle(Color.accentColor)
                                .frame(width: 26)
                                .accessibilityHidden(true)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(option.title).font(.body.weight(.medium)).foregroundStyle(.primary)
                                Text(option.description).font(.caption).foregroundStyle(.secondary)
                            }
                            Spacer(minLength: 8)
                            Image(systemName: appearance == option.rawValue ? "checkmark.circle.fill" : "circle")
                                .foregroundStyle(appearance == option.rawValue ? Color.accentColor : Color.secondary.opacity(0.4))
                                .accessibilityHidden(true)
                        }
                        .padding(.vertical, 6)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(appearance == option.rawValue ? .isSelected : [])
                }
            } header: {
                Text("外观")
            } footer: {
                Text("自动保存，下次打开继续使用。")
            }

            Section("阅读小贴士") {
                tip("下拉，发现新内容", description: "列表下拉即可刷新，滑到末尾会自动加载更多。", symbol: "arrow.down.circle")
                tip("轻触，开始深入阅读", description: "点击卡片阅读原文，也可以长按卡片分享文章链接。", symbol: "hand.tap")
            }
            Section("关于 WanAndroid") {
                Link(destination: URL(string: "https://wanandroid.com")!) {
                    Label("玩 Android 开放社区", systemImage: "globe")
                }
                LabeledContent("版本", value: Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "1.0")
            }
            Section {
                Text("文章与项目内容来自玩 Android。\n保持好奇，持续进步。")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .lineSpacing(5)
                    .frame(maxWidth: .infinity)
                    .multilineTextAlignment(.center)
                    .listRowBackground(Color.clear)
            }
        }
        .frame(maxWidth: 760)
        .frame(maxWidth: .infinity)
        .background(ReadingStyle.canvas)
        .navigationTitle("设置")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func tip(_ title: String, description: String, symbol: String) -> some View {
        HStack(alignment: .top, spacing: 14) {
            Image(systemName: symbol).foregroundStyle(Color.accentColor).frame(width: 26).accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 5) {
                Text(title).font(.subheadline.weight(.medium))
                Text(description).font(.caption).foregroundStyle(.secondary).lineSpacing(3)
            }
        }
        .padding(.vertical, 6)
    }
}

private extension AppAppearance {
    var symbol: String {
        switch self {
        case .system: "circle.lefthalf.filled"
        case .light: "sun.max"
        case .dark: "moon"
        }
    }
    var description: String {
        switch self {
        case .system: "随设备外观自动切换"
        case .light: "明亮、清晰的阅读空间"
        case .dark: "适合夜间的柔和深色"
        }
    }
}
