import Foundation

struct NativeTag: Decodable, Hashable {
    let name: String?
    let url: String?
}

struct NativeArticle: Decodable, Identifiable {
    let articleID: Int?
    let originId: Int?
    let title: String
    let link: String
    let author: String?
    let shareUser: String?
    let niceDate: String?
    let niceShareDate: String?
    let chapterName: String?
    let superChapterName: String?
    let desc: String?
    let envelopePic: String?
    let fresh: Bool?
    let collect: Bool?
    let pinned: Bool?
    let tags: [NativeTag]?

    enum CodingKeys: String, CodingKey {
        case articleID = "id"
        case originId, title, link, author, shareUser, niceDate, niceShareDate
        case chapterName, superChapterName, desc, envelopePic, fresh, collect, pinned, tags
    }

    var id: String { articleID.map(String.init) ?? link }
    var collectTargetID: Int? {
        if let originId, originId > 0 { return originId }
        return articleID
    }
    var displayAuthor: String { author.nonEmpty ?? shareUser.nonEmpty ?? "匿名分享" }
    var displayDate: String { niceDate.nonEmpty ?? niceShareDate.nonEmpty ?? "" }
    var url: URL? { Self.webURL(link) }
    var imageURL: URL? { envelopePic.flatMap(Self.webURL) }

    static func webURL(_ value: String) -> URL? {
        guard let url = URL(string: value),
              ["http", "https"].contains(url.scheme?.lowercased() ?? ""),
              url.host != nil else { return nil }
        return url
    }
}

struct NativeCategory: Decodable, Identifiable, Hashable {
    let id: Int
    let name: String
}

struct NativeBanner: Decodable, Identifiable, Hashable {
    let id: Int
    let title: String
    let desc: String
    let imagePath: String
    let url: String

    var imageURL: URL? { NativeArticle.webURL(imagePath) }
    var targetURL: URL? { NativeArticle.webURL(url) }
}

struct NativeHotKey: Decodable, Identifiable, Hashable {
    let id: Int
    let name: String
}

struct NativeUserInfo: Decodable, Equatable {
    let id: Int
    let username: String
    let nickname: String
    let coinCount: Int
    let collectIds: [Int]

    var displayName: String {
        let trimmed = nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? username : trimmed
    }
}

struct NativeCoinInfo: Decodable, Equatable {
    let coinCount: Int
    let level: Int
    let nickname: String
    let rank: String
    let userId: Int
    let username: String

    var displayName: String {
        let trimmed = nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? username : trimmed
    }
}

struct NativeCoinRecord: Decodable, Identifiable {
    let id: Int64
    let coinCount: Int
    let date: Int64
    let desc: String
    let reason: String
}

struct NativeCoinRankState: Decodable {
    var items: [NativeCoinInfo] = []
    var loading = false
    var refreshing = false
    var loadingMore = false
    var hasMore = true
    var error: String?
    var loaded = false
    var failedToLoadMore = false
    var loadedPage: Int?

    var busy: Bool { loading || refreshing || loadingMore }
}

struct NativeCoinHistoryState: Decodable {
    var items: [NativeCoinRecord] = []
    var loading = false
    var refreshing = false
    var loadingMore = false
    var hasMore = true
    var error: String?
    var loaded = false
    var failedToLoadMore = false
    var loadedPage: Int?

    var busy: Bool { loading || refreshing || loadingMore }
}

struct NativeFeedState: Decodable {
    var articles: [NativeArticle] = []
    var loading = false
    var refreshing = false
    var loadingMore = false
    var hasMore = true
    var error: String?
    var failedToLoadMore: Bool?
    var loaded = false
    var loadedPage: Int?

    var busy: Bool { loading || refreshing || loadingMore }
}

struct NativeAppState: Decodable {
    var home = NativeFeedState()
    var plaza = NativeFeedState()
    var projects = NativeFeedState()
    var categories: [NativeCategory] = []
    var selectedProjectId: Int?
    var categoriesLoading = false
    var categoriesError: String?
    var banners: [NativeBanner] = []
    var hotKeys: [NativeHotKey] = []
    var searchHistory: [String] = []
    var searchQuery = ""
    var search = NativeFeedState(hasMore: false)
    var user: NativeUserInfo?
    var coinInfo: NativeCoinInfo?
    var authLoading = false
    var authError: String?
    var collectedIds: [Int] = []
    var collections = NativeFeedState()
    var coinRank = NativeCoinRankState()
    var coinHistory = NativeCoinHistoryState()
    var actionMessage: String?

    func feed(for section: AppSection) -> NativeFeedState {
        switch section {
        case .home: home
        case .plaza: plaza
        case .projects: projects
        case .search: search
        case .profile: NativeFeedState()
        }
    }
}

private extension Optional where Wrapped == String {
    var nonEmpty: String? {
        guard let value = self?.trimmingCharacters(in: .whitespacesAndNewlines), !value.isEmpty else { return nil }
        return value
    }
}
