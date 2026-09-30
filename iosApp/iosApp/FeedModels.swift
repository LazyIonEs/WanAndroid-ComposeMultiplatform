import Foundation

struct NativeArticle: Decodable, Identifiable {
    let articleID: Int?
    let title: String
    let link: String
    let author: String?
    let shareUser: String?
    let niceDate: String?
    let niceShareDate: String?
    let chapterName: String?
    let desc: String?
    let envelopePic: String?
    let fresh: Bool?
    let pinned: Bool?

    enum CodingKeys: String, CodingKey {
        case articleID = "id"
        case title, link, author, shareUser, niceDate, niceShareDate
        case chapterName, desc, envelopePic, fresh, pinned
    }

    var id: String { articleID.map(String.init) ?? link }
    var displayAuthor: String { author.nonEmpty ?? shareUser.nonEmpty ?? "匿名分享" }
    var displayDate: String { niceDate.nonEmpty ?? niceShareDate.nonEmpty ?? "" }
    var url: URL? { Self.webURL(link) }
    var imageURL: URL? { envelopePic.flatMap(Self.webURL) }

    private static func webURL(_ value: String) -> URL? {
        guard let url = URL(string: value),
              ["http", "https"].contains(url.scheme?.lowercased() ?? ""),
              url.host != nil else { return nil }
        return url
    }
}

struct NativeCategory: Decodable, Identifiable {
    let id: Int
    let name: String
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

    func feed(for section: AppSection) -> NativeFeedState {
        switch section {
        case .home: home
        case .plaza: plaza
        case .projects: projects
        case .settings: NativeFeedState()
        }
    }
}

private extension Optional where Wrapped == String {
    var nonEmpty: String? {
        guard let value = self?.trimmingCharacters(in: .whitespacesAndNewlines), !value.isEmpty else { return nil }
        return value
    }
}
