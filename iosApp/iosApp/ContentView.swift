import SwiftUI

struct ContentView: View {
    @StateObject private var store = WanAndroidStore()
    @State private var selectedTab: AppSection = .home
    @State private var searchText = ""
    @State private var showAuth = false
    @State private var showSearchSheet = false
    @AppStorage("appearance") private var appearance = AppAppearance.system.rawValue

    var body: some View {
        ZStack(alignment: .top) {
            tabRoot

            if let message = store.state.actionMessage, !message.isEmpty {
                toastBanner(message)
                    .padding(.top, 12)
                    .transition(.move(edge: .top).combined(with: .opacity))
                    .zIndex(10)
                    .task(id: message) {
                        try? await Task.sleep(for: .seconds(2.2))
                        withAnimation(.easeOut(duration: 0.2)) {
                            store.clearActionMessage()
                        }
                    }
            }
        }
        .animation(.spring(response: 0.35, dampingFraction: 0.85), value: store.state.actionMessage)
        .preferredColorScheme(AppAppearance(rawValue: appearance)?.colorScheme)
        .onChange(of: selectedTab) { _, section in store.activate(section) }
        .sheet(isPresented: $showAuth) {
            AuthSheetView(store: store)
        }
        .task { store.start() }
    }

    @ViewBuilder
    private func toastBanner(_ message: String) -> some View {
        let label = Label(message, systemImage: "checkmark.circle.fill")
            .font(.subheadline.weight(.medium))
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
        if #available(iOS 26.0, *) {
            label.glassEffect(.regular, in: .capsule)
        } else {
            label
                .background(.regularMaterial, in: Capsule())
                .shadow(color: .black.opacity(0.12), radius: 10, y: 4)
        }
    }

    @ViewBuilder
    private var tabRoot: some View {
        if #available(iOS 26.0, *) {
            TabView(selection: $selectedTab) {
                Tab(AppSection.home.title, systemImage: AppSection.home.symbol, value: AppSection.home) {
                    NavigationStack {
                        ArticleFeedView(
                            section: .home,
                            store: store,
                            onExplore: { selectedTab = $0 },
                            onRequestAuth: { showAuth = true }
                        )
                    }
                }
                Tab(AppSection.plaza.title, systemImage: AppSection.plaza.symbol, value: AppSection.plaza) {
                    NavigationStack {
                        ArticleFeedView(
                            section: .plaza,
                            store: store,
                            onExplore: { selectedTab = $0 },
                            onRequestAuth: { showAuth = true }
                        )
                    }
                }
                Tab(AppSection.projects.title, systemImage: AppSection.projects.symbol, value: AppSection.projects) {
                    NavigationStack {
                        ArticleFeedView(
                            section: .projects,
                            store: store,
                            onExplore: { selectedTab = $0 },
                            onRequestAuth: { showAuth = true }
                        )
                    }
                }
                Tab(AppSection.profile.title, systemImage: AppSection.profile.symbol, value: AppSection.profile) {
                    NavigationStack {
                        ProfileView(store: store, onRequestAuth: { showAuth = true })
                    }
                }
                Tab(value: AppSection.search, role: .search) {
                    NavigationStack {
                        SearchView(store: store, queryText: $searchText, onRequestAuth: { showAuth = true })
                    }
                    .searchable(text: $searchText, prompt: "搜索文章、作者或关键字")
                    .onSubmit(of: .search) {
                        let trimmed = searchText.trimmingCharacters(in: .whitespacesAndNewlines)
                        if !trimmed.isEmpty { store.search(trimmed) }
                    }
                }
            }
            .tabBarMinimizeBehavior(.onScrollDown)
            .tabViewSearchActivation(.searchTabSelection)
        } else {
            ZStack(alignment: .bottomTrailing) {
                TabView(selection: $selectedTab) {
                    ForEach(AppSection.mainTabs) { section in
                        NavigationStack {
                            if section == .profile {
                                ProfileView(store: store, onRequestAuth: { showAuth = true })
                            } else {
                                ArticleFeedView(
                                    section: section,
                                    store: store,
                                    onExplore: { selectedTab = $0 },
                                    onRequestAuth: { showAuth = true }
                                )
                            }
                        }
                        .tabItem { Label(section.title, systemImage: section.symbol) }
                        .tag(section)
                    }
                }

                Button {
                    store.activate(.search)
                    showSearchSheet = true
                } label: {
                    Image(systemName: "magnifyingglass")
                        .font(.title3.weight(.semibold))
                        .foregroundStyle(Color.accentColor)
                        .frame(width: 52, height: 52)
                        .background(.ultraThinMaterial, in: Circle())
                        .shadow(color: .black.opacity(0.16), radius: 10, y: 4)
                }
                .padding(.trailing, 20)
                .padding(.bottom, 64)
                .accessibilityLabel("搜索")
            }
            .sheet(isPresented: $showSearchSheet) {
                NavigationStack {
                    SearchView(store: store, queryText: $searchText, onRequestAuth: { showAuth = true })
                        .toolbar {
                            ToolbarItem(placement: .cancellationAction) {
                                Button("关闭") { showSearchSheet = false }
                            }
                        }
                }
                .searchable(text: $searchText, prompt: "搜索文章、作者或关键字")
                .onSubmit(of: .search) {
                    let trimmed = searchText.trimmingCharacters(in: .whitespacesAndNewlines)
                    if !trimmed.isEmpty { store.search(trimmed) }
                }
            }
        }
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
