import SwiftUI

struct ArticleFeedView: View {
    let section: AppSection
    @ObservedObject var store: WanAndroidStore
    let onExplore: (AppSection) -> Void
    let onRequestAuth: () -> Void

    @State private var selectedArticle: NativeArticle?
    @State private var showCoinRank = false
    @State private var showCollect = false

    private var feed: NativeFeedState { store.state.feed(for: section) }
    private var categoryLoading: Bool { section == .projects && store.state.categoriesLoading }
    private var categoryError: String? { section == .projects ? store.state.categoriesError : nil }
    private var error: String? { store.decodingError ?? categoryError ?? feed.error }
    private var initialLoading: Bool {
        feed.articles.isEmpty && (feed.loading || categoryLoading || (!feed.loaded && error == nil))
    }
    private var hasNoCategories: Bool {
        section == .projects && store.state.categories.isEmpty && !categoryLoading
    }

    var body: some View {
        ScrollViewReader { proxy in
            List {
                if section == .home {
                    if !store.state.banners.isEmpty {
                        Section {
                            BannerCarouselView(banners: store.state.banners) { banner in
                                selectedArticle = NativeArticle(
                                    articleID: nil,
                                    originId: nil,
                                    title: banner.title.isEmpty ? "精选推荐" : banner.title,
                                    link: banner.url,
                                    author: nil,
                                    shareUser: nil,
                                    niceDate: nil,
                                    niceShareDate: nil,
                                    chapterName: nil,
                                    superChapterName: nil,
                                    desc: banner.desc,
                                    envelopePic: nil,
                                    fresh: nil,
                                    collect: nil,
                                    pinned: nil,
                                    tags: nil
                                )
                            }
                            .listRowInsets(EdgeInsets())
                        }
                    }

                    Section {
                        HomeQuickActionsView(
                            onOpenCoinRank: { showCoinRank = true },
                            onOpenCollect: { showCollect = true },
                            onOpenProjects: { onExplore(.projects) },
                            onOpenPlaza: { onExplore(.plaza) }
                        )
                        .listRowBackground(Color.clear)
                        .listRowInsets(EdgeInsets())
                    }
                }

                if section == .projects && !store.state.categories.isEmpty {
                    Section("探索分类（\(store.state.categories.count)）") {
                        categoryPicker
                            .listRowBackground(Color.clear)
                            .listRowInsets(EdgeInsets())
                    }
                    .id("category-picker")
                }

                if initialLoading {
                    Section {
                        HStack {
                            Spacer()
                            ProgressView(categoryLoading ? "正在加载项目分类…" : "正在加载…")
                                .padding(.vertical, 32)
                            Spacer()
                        }
                    }
                } else if feed.articles.isEmpty {
                    if feed.loaded && feed.hasMore && error == nil {
                        Section { pagination }
                    } else {
                        Section { emptyState }
                    }
                } else {
                    if let error, feed.failedToLoadMore != true {
                        Section {
                            ErrorRow(message: error) { store.retry(section) }
                        }
                    }

                    Section {
                        ForEach(feed.articles) { article in
                            let collected = store.isCollected(article)
                            Button {
                                selectedArticle = article
                            } label: {
                                ArticleRow(
                                    article: article,
                                    showImage: section == .projects,
                                    featured: section == .home && article.pinned == true,
                                    isCollected: collected,
                                    onToggleCollect: {
                                        handleToggleCollect(article)
                                    }
                                )
                            }
                            .buttonStyle(.plain)
                            .disabled(article.url == nil)
                            .swipeActions(edge: .trailing) {
                                if let targetID = article.collectTargetID {
                                    Button {
                                        handleToggleCollect(article, targetID: targetID)
                                    } label: {
                                        Label(collected ? "取消收藏" : "收藏", systemImage: collected ? "heart.slash" : "heart")
                                    }
                                    .tint(collected ? .gray : ReadingStyle.heartRed)
                                }
                            }
                            .contextMenu {
                                if let targetID = article.collectTargetID {
                                    Button {
                                        handleToggleCollect(article, targetID: targetID)
                                    } label: {
                                        Label(
                                            collected ? "取消收藏" : "收藏文章",
                                            systemImage: collected ? "heart.slash" : "heart"
                                        )
                                    }
                                }
                                if let url = article.url {
                                    ShareLink(item: url) { Label("分享文章", systemImage: "square.and.arrow.up") }
                                }
                            }
                        }
                    } header: {
                        HStack {
                            Text(sectionHeading)
                            Spacer()
                            Text("已载入 \(feed.articles.count) 篇")
                        }
                    }

                    Section { pagination }
                }
            }
            .listStyle(.insetGrouped)
            .refreshable {
                await store.refresh(section)
            }
            .navigationTitle(section == .home ? "WanAndroid" : section.title)
            .navigationBarTitleDisplayMode(.inline)
            .navigationDestination(isPresented: $showCoinRank) {
                CoinRankView(store: store, initialTab: 0, onRequestAuth: onRequestAuth)
            }
            .navigationDestination(isPresented: $showCollect) {
                CollectListView(store: store, onRequestAuth: onRequestAuth)
            }
            .onChange(of: store.state.selectedProjectId) { previous, _ in
                if section == .projects && previous != nil { proxy.scrollTo("category-picker", anchor: .top) }
            }
        }
        .onDisappear { store.cancelRefresh(section) }
        .sheet(item: $selectedArticle) { article in
            if let url = article.url {
                ArticleBrowser(url: url).ignoresSafeArea()
            }
        }
    }

    private func handleToggleCollect(_ article: NativeArticle, targetID: Int? = nil) {
        guard let id = targetID ?? article.collectTargetID else { return }
        if !store.isLoggedIn {
            onRequestAuth()
        } else {
            store.toggleCollect(articleId: id)
        }
    }

    private var sectionHeading: String {
        switch section {
        case .home: "精选与新知"
        case .plaza: "最新分享"
        case .projects: store.state.categories.first { $0.id == store.state.selectedProjectId }?.name ?? "项目列表"
        case .profile, .search: ""
        }
    }

    private var categoryPicker: some View {
        ScrollViewReader { chipProxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(store.state.categories) { category in
                        let selected = store.state.selectedProjectId == category.id
                        if selected {
                            Button(category.name) {
                                store.selectCategory(category.id)
                            }
                            .buttonStyle(.borderedProminent)
                            .buttonBorderShape(.capsule)
                            .id(category.id)
                        } else {
                            Button(category.name) {
                                store.selectCategory(category.id)
                            }
                            .buttonStyle(.bordered)
                            .buttonBorderShape(.capsule)
                            .id(category.id)
                        }
                    }
                }
                .padding(.vertical, 2)
            }
            .onChange(of: store.state.selectedProjectId) { _, newValue in
                if let newValue {
                    withAnimation(.easeInOut(duration: 0.25)) {
                        chipProxy.scrollTo(newValue, anchor: .center)
                    }
                }
            }
        }
    }

    private var emptyState: some View {
        ContentUnavailableView {
            Label(emptyTitle, systemImage: error == nil ? "tray" : "wifi.exclamationmark")
        } description: {
            Text(error ?? (hasNoCategories ? "项目分类暂时为空，稍后下拉刷新试试。" : "这里还没有分享，下拉刷新试试。"))
        } actions: {
            if error != nil {
                Button("重新加载") { store.retry(section) }
                    .buttonStyle(.borderedProminent)
                    .disabled(feed.busy || categoryLoading)
            }
        }
        .frame(minHeight: 260)
    }

    private var emptyTitle: String {
        if error != nil { return "暂时无法加载" }
        if hasNoCategories { return "暂无项目分类" }
        return section == .projects ? "暂无项目" : "暂无文章"
    }

    @ViewBuilder
    private var pagination: some View {
        ForEach([paginationIdentity], id: \.self) { _ in
            HStack {
                Spacer()
                if feed.loadingMore {
                    ProgressView("正在加载更多…")
                } else if feed.failedToLoadMore == true {
                    VStack(spacing: 8) {
                        Text(feed.error ?? "更多内容暂时无法加载")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                        Button("重试加载") { store.retry(section) }
                            .buttonStyle(.bordered)
                            .disabled(feed.busy || categoryLoading)
                    }
                } else if feed.hasMore {
                    if feed.articles.isEmpty && error == nil {
                        ProgressView("正在加载…")
                    } else {
                        Text(error == nil ? "上拉加载更多" : "下拉刷新后继续浏览")
                            .font(.footnote).foregroundStyle(.secondary)
                    }
                } else {
                    Text("本页已读完，明天也来发现新知")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                Spacer()
            }
            .padding(.vertical, 6)
            .onAppear(perform: loadNextPageIfNeeded)
        }
    }

    private var paginationIdentity: PaginationIdentity {
        PaginationIdentity(
            lastArticleID: feed.articles.last?.id,
            itemCount: feed.articles.count,
            loadedPage: feed.loadedPage,
            busy: feed.busy,
            categoryID: section == .projects ? store.state.selectedProjectId : nil
        )
    }

    private func loadNextPageIfNeeded() {
        guard feed.loaded, feed.hasMore, !feed.busy, !categoryLoading, error == nil else { return }
        store.loadMore(section)
    }

    private struct PaginationIdentity: Hashable {
        let lastArticleID: String?
        let itemCount: Int
        let loadedPage: Int?
        let busy: Bool
        let categoryID: Int?
    }
}

private struct ErrorRow: View {
    let message: String
    let retry: () -> Void

    var body: some View {
        LabeledContent {
            Button("重试", action: retry)
        } label: {
            Label(message, systemImage: "exclamationmark.circle")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
    }
}
