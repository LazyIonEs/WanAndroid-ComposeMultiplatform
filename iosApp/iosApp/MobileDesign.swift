import SwiftUI

/// Shared visual language, expressed with native, dynamically sized SwiftUI views.
enum ReadingStyle {
    static let canvas = Color(uiColor: .systemGroupedBackground)
    static let card = Color(uiColor: .secondarySystemGroupedBackground)
    static let radius: CGFloat = 24
}

struct FeedIntroduction: View {
    let section: AppSection
    let onExplore: (AppSection) -> Void

    private var eyebrow: String {
        switch section {
        case .home: "给开发者的阅读空间"
        case .plaza: "社区广场"
        case .projects: "开源灵感库"
        case .settings: "阅读偏好"
        }
    }
    private var headline: String {
        switch section {
        case .home: "保持好奇，持续进步。"
        case .plaza: "交流，让灵感发生。"
        case .projects: "好项目，值得被发现。"
        case .settings: "让阅读更合心意"
        }
    }
    private var subtitle: String {
        switch section {
        case .home: "读一篇好文章，让今天的积累成为明天的灵感。"
        case .plaza: "来自开发者的实践、思考与新鲜分享。"
        case .projects: "发现实用工具与开源作品，从好代码中学习。"
        case .settings: "留一点时间给知识与灵感。"
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(eyebrow).font(.subheadline.weight(.medium)).foregroundStyle(Color.accentColor)
            Text(headline)
                .font(.title2.bold())
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityAddTraits(.isHeader)
            Text(subtitle)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .lineSpacing(4)
                .fixedSize(horizontal: false, vertical: true)
            if section == .home {
                HStack(alignment: .top, spacing: 12) {
                    exploreCard(.plaza, title: "社区广场", subtitle: "发现不同的解法", symbol: "bubble.left.and.bubble.right")
                    exploreCard(.projects, title: "开源项目", subtitle: "从灵感到实践", symbol: "curlybraces")
                }
                .padding(.top, 10)
            }
        }
        .padding(.vertical, 10)
    }

    private func exploreCard(_ destination: AppSection, title: String, subtitle: String, symbol: String) -> some View {
        Button { onExplore(destination) } label: {
            VStack(alignment: .leading, spacing: 7) {
                Image(systemName: symbol).font(.title3).foregroundStyle(Color.accentColor).accessibilityHidden(true)
                Text(title).font(.subheadline.weight(.semibold)).foregroundStyle(.primary)
                Text(subtitle).font(.caption).foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(ReadingStyle.card, in: RoundedRectangle(cornerRadius: 20))
        }
        .buttonStyle(.plain)
        .accessibilityHint("切换到\(title)")
    }
}

struct FeedSectionHeading: View {
    let title: String
    let detail: String

    var body: some View {
        ViewThatFits(in: .horizontal) {
            HStack(alignment: .firstTextBaseline) {
                Text(title).font(.headline).accessibilityAddTraits(.isHeader)
                Spacer(minLength: 12)
                Text(detail).font(.caption).foregroundStyle(.secondary)
            }
            VStack(alignment: .leading, spacing: 4) {
                Text(title).font(.headline).accessibilityAddTraits(.isHeader)
                Text(detail).font(.caption).foregroundStyle(.secondary)
            }
        }
        .padding(.top, 10)
        .padding(.bottom, 2)
    }
}

struct ArticleRow: View {
    let article: NativeArticle
    let showImage: Bool
    let featured: Bool
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            ViewThatFits(in: .horizontal) {
                HStack(spacing: 6) { badges }
                VStack(alignment: .leading, spacing: 6) { badges }
            }
            HStack(alignment: .top, spacing: 14) {
                VStack(alignment: .leading, spacing: 9) {
                    Text(article.title)
                        .font(featured ? .title3.bold() : .headline)
                        .foregroundStyle(.primary)
                        .lineSpacing(4)
                        .fixedSize(horizontal: false, vertical: true)
                    if let description = article.desc, !description.isEmpty {
                        Text(description)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .lineSpacing(4)
                            .lineLimit(3)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                if showImage && !dynamicTypeSize.isAccessibilitySize { projectImage }
            }
            Rectangle().fill(Color.primary.opacity(0.07)).frame(height: 1).accessibilityHidden(true)
            HStack(spacing: 9) {
                Text(String(article.displayAuthor.prefix(1)).uppercased())
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(Color.accentColor)
                    .frame(width: 32, height: 32)
                    .background(Color.accentColor.opacity(0.1), in: Circle())
                    .accessibilityHidden(true)
                VStack(alignment: .leading, spacing: 3) {
                    Text(article.displayAuthor).font(.caption.weight(.medium)).foregroundStyle(.primary).lineLimit(1)
                    if !article.displayDate.isEmpty {
                        Text(article.displayDate).font(.caption2).foregroundStyle(.secondary)
                    }
                }
                Spacer(minLength: 8)
                Image(systemName: "arrow.up.right").font(.caption.weight(.semibold)).foregroundStyle(.secondary).accessibilityHidden(true)
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(featured ? Color.accentColor.opacity(0.12) : ReadingStyle.card, in: RoundedRectangle(cornerRadius: ReadingStyle.radius))
        .contentShape(RoundedRectangle(cornerRadius: ReadingStyle.radius))
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private var badges: some View {
        if article.pinned == true {
            Label("站内置顶", systemImage: "pin.fill").badgeStyle(color: .accentColor)
        }
        if article.fresh == true { Text("新").badgeStyle(color: .accentColor) }
        if let category = article.chapterName, !category.isEmpty {
            Text(category).badgeStyle(color: .secondary)
        }
    }

    private var projectImage: some View {
        AsyncImage(url: article.imageURL) { phase in
            if let image = phase.image {
                image.resizable().scaledToFill()
            } else {
                Image(systemName: "curlybraces")
                    .font(.title2.weight(.medium))
                    .foregroundStyle(Color.accentColor)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.accentColor.opacity(0.09))
            }
        }
        .frame(width: 64, height: 76)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .accessibilityHidden(true)
    }
}

private extension View {
    func badgeStyle(color: Color) -> some View {
        font(.caption2.weight(.medium))
            .foregroundStyle(color)
            .padding(.horizontal, 8)
            .padding(.vertical, 5)
            .background(color.opacity(0.08), in: RoundedRectangle(cornerRadius: 7))
    }
}
