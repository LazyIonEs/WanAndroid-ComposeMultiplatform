import Foundation

enum AppSection: String, CaseIterable, Identifiable {
    case home, plaza, projects, settings

    var id: String { rawValue }
    var title: String {
        switch self {
        case .home: "首页"
        case .plaza: "广场"
        case .projects: "项目"
        case .settings: "设置"
        }
    }
    var symbol: String {
        switch self {
        case .home: "house"
        case .plaza: "person.2"
        case .projects: "square.grid.2x2"
        case .settings: "gearshape"
        }
    }
}
