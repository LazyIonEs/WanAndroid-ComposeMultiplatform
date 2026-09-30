import SafariServices
import SwiftUI

struct ArticleFeedView: View {
    let section: AppSection
    @ObservedObject var store: WanAndroidStore
    let onExplore: (AppSection) -> Void
    @State private var selectedArticle: NativeArticle?
    @State private var refreshingFromGesture = false

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
                Group {
                    FeedIntroduction(section: section, onExplore: onExplore)
                        .id("feed-top")
                    if section == .projects && !store.state.categories.isEmpty {
                        categoryPicker
                            .listRowBackground(Color.clear)
                            .listRowSeparator(.hidden)
                            .id("category-picker")
                    }

                    if initialLoading {
                        loadingState
                    } else if feed.articles.isEmpty {
                        if feed.loaded && feed.hasMore && error == nil {
                            pagination
                        } else {
                            emptyState
                        }
                    } else {
                        if let error, feed.failedToLoadMore != true {
                            ErrorRow(message: error) { store.retry(section) }
                        }
                        FeedSectionHeading(title: sectionHeading, detail: "已载入 \(feed.articles.count) 篇")
                        ForEach(feed.articles) { article in
                            Button {
                                selectedArticle = article
                            } label: {
                                ArticleRow(article: article, showImage: section == .projects, featured: section == .home && article.pinned == true)
                            }
                            .buttonStyle(.plain)
                            .disabled(article.url == nil)
                            .accessibilityHint(article.url == nil ? "链接不可用" : "打开文章详情")
                            .contextMenu {
                                if let url = article.url {
                                    ShareLink(item: url) { Label("分享文章", systemImage: "square.and.arrow.up") }
                                }
                            }
                        }
                        pagination
                    }
                }
                .listRowInsets(EdgeInsets(top: 6, leading: 20, bottom: 6, trailing: 20))
                .listRowSeparator(.hidden)
                .listRowBackground(Color.clear)
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
            .background(ReadingStyle.canvas)
            .frame(maxWidth: 760)
            .frame(maxWidth: .infinity)
            .background(ReadingStyle.canvas)
            .refreshable {
                refreshingFromGesture = true
                defer { refreshingFromGesture = false }
                await store.refresh(section)
            }
            .navigationTitle(section == .home ? "WanAndroid" : section.title)
            .navigationBarTitleDisplayMode(.inline)
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

    private var sectionHeading: String {
        switch section {
        case .home: "精选与新知"
        case .plaza: "最新分享"
        case .projects: store.state.categories.first { $0.id == store.state.selectedProjectId }?.name ?? "项目列表"
        case .settings: ""
        }
    }

    private var categoryPicker: some View {
        VStack(alignment: .leading, spacing: 8) {
            FeedSectionHeading(title: "探索分类", detail: "\(store.state.categories.count) 个方向")
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(store.state.categories) { category in
                        let selected = store.state.selectedProjectId == category.id
                        Button { store.selectCategory(category.id) } label: {
                            Text(category.name)
                                .font(.subheadline.weight(selected ? .semibold : .regular))
                                .foregroundStyle(selected ? Color(uiColor: .systemBackground) : Color.primary)
                                .padding(.horizontal, 16)
                                .frame(minHeight: 44)
                                .background(selected ? Color.accentColor : ReadingStyle.card, in: Capsule())
                        }
                        .buttonStyle(.plain)
                        .accessibilityAddTraits(selected ? .isSelected : [])
                    }
                }
            }
        }
    }

    private var loadingState: some View {
        HStack {
            Spacer()
            if refreshingFromGesture {
                Text(categoryLoading ? "正在加载项目分类…" : "正在加载…")
                    .font(.subheadline).foregroundStyle(.secondary)
            } else {
                ProgressView(categoryLoading ? "正在加载项目分类…" : "正在加载…")
            }
            Spacer()
        }
        .frame(minHeight: 300)
        .listRowSeparator(.hidden)
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
        .frame(minHeight: 300)
        .listRowSeparator(.hidden)
    }

    private var emptyTitle: String {
        if error != nil { return "暂时无法加载" }
        if hasNoCategories { return "暂无项目分类" }
        return section == .projects ? "暂无项目" : "暂无文章"
    }

    @ViewBuilder
    private var pagination: some View {
        // A fresh row identity lets List decide visibility after each page is applied.
        // It also rechecks the end after an empty page that still has a next page.
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
                    Text("本页已读完，明天也来发现新知").font(.footnote).foregroundStyle(.secondary)
                }
                Spacer()
            }
            .frame(minHeight: feed.articles.isEmpty ? 300 : 0)
            .padding(.vertical, 20)
            .listRowSeparator(.hidden)
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
        VStack(alignment: .leading, spacing: 8) {
            Label(message, systemImage: "exclamationmark.circle")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            Button("重试", action: retry)
        }
        .padding(.vertical, 8)
    }
}

/// System browser supplies article rendering, navigation, sharing and Safari hand-off.
private struct ArticleBrowser: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        let configuration = SFSafariViewController.Configuration()
        configuration.entersReaderIfAvailable = false
        return SFSafariViewController(url: url, configuration: configuration)
    }

    func updateUIViewController(_ controller: SFSafariViewController, context: Context) {}
}
