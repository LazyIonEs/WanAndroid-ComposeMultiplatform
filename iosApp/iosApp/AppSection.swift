import Foundation

enum AppSection: String, CaseIterable, Identifiable, Hashable {
    case home, plaza, projects, profile, search

    static let mainTabs: [AppSection] = [.home, .plaza, .projects, .profile]

    var id: String { rawValue }

    var title: String {
        switch self {
        case .home: "首页"
        case .plaza: "广场"
        case .projects: "项目"
        case .profile: "我的"
        case .search: "搜索"
        }
    }

    var symbol: String {
        switch self {
        case .home: "house"
        case .plaza: "person.2"
        case .projects: "square.grid.2x2"
        case .profile: "person.crop.circle"
        case .search: "magnifyingglass"
        }
    }
}
