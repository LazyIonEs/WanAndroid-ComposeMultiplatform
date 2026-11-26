import SafariServices
import SwiftUI

enum ReadingStyle {
    static let gold = Color(red: 0.85, green: 0.61, blue: 0.15)
    static let silver = Color(red: 0.56, green: 0.61, blue: 0.66)
    static let bronze = Color(red: 0.75, green: 0.48, blue: 0.29)
    static let heartRed = Color(red: 0.90, green: 0.28, blue: 0.30)
    static let hotOrange = Color(red: 0.92, green: 0.35, blue: 0.05)
}

struct HomeQuickActionsView: View {
    let onOpenCoinRank: () -> Void
    let onOpenCollect: () -> Void
    let onOpenProjects: () -> Void
    let onOpenPlaza: () -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                Button(action: onOpenCoinRank) {
                    Label("积分排行", systemImage: "trophy.fill")
                }
                Button(action: onOpenCollect) {
                    Label("我的收藏", systemImage: "heart.fill")
                }
                Button(action: onOpenProjects) {
                    Label("开源项目", systemImage: "curlybraces")
                }
                Button(action: onOpenPlaza) {
                    Label("社区广场", systemImage: "bubble.left.and.bubble.right.fill")
                }
            }
            .buttonStyle(.bordered)
            .buttonBorderShape(.capsule)
            .controlSize(.regular)
            .padding(.vertical, 2)
        }
    }
}

struct ArticleRow: View {
    let article: NativeArticle
    let showImage: Bool
    let featured: Bool
    let isCollected: Bool
    var highlightQuery: String = ""
    var onToggleCollect: (() -> Void)?

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if hasMetadata {
                HStack(spacing: 6) {
                    if article.pinned == true {
                        Label("置顶", systemImage: "pin.fill")
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(Color.accentColor)
                    }
                    if article.fresh == true {
                        Text("新")
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(ReadingStyle.hotOrange)
                    }
                    if let meta = metadataText {
                        Text(meta)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .lineLimit(1)
                    }
                }
            }

            HStack(alignment: .top, spacing: 12) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(highlightedTitle)
                        .font(featured ? .headline.bold() : .headline)
                        .foregroundStyle(.primary)
                        .lineSpacing(3)
                        .fixedSize(horizontal: false, vertical: true)
                    if let description = article.desc, !description.isEmpty {
                        Text(description)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .lineLimit(3)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                if showImage && !dynamicTypeSize.isAccessibilitySize { projectImage }
            }

            HStack(spacing: 8) {
                Label(article.displayAuthor, systemImage: "person.circle")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)

                if !article.displayDate.isEmpty {
                    Text("·")
                        .font(.caption)
                        .foregroundStyle(.tertiary)
                    Text(article.displayDate)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }

                Spacer(minLength: 8)

                if let onToggleCollect, article.collectTargetID != nil {
                    Button(action: onToggleCollect) {
                        Image(systemName: isCollected ? "heart.fill" : "heart")
                            .foregroundStyle(isCollected ? ReadingStyle.heartRed : .secondary)
                            .symbolEffect(.bounce, value: isCollected)
                    }
                    .buttonStyle(.borderless)
                    .sensoryFeedback(.impact(flexibility: .soft), trigger: isCollected)
                    .accessibilityLabel(isCollected ? "取消收藏" : "收藏")
                }
            }
            .padding(.top, 2)
        }
        .padding(.vertical, 4)
    }

    private var hasMetadata: Bool {
        article.pinned == true || article.fresh == true || metadataText != nil
    }

    private var metadataText: String? {
        var parts: [String] = []
        let chapters = [article.superChapterName, article.chapterName]
            .compactMap { $0?.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
        if let first = chapters.first {
            parts.append(chapters.count > 1 && chapters[1] != first ? "\(first) · \(chapters[1])" : first)
        }
        if let tags = article.tags?.prefix(2) {
            for tag in tags {
                if let name = tag.name?.trimmingCharacters(in: .whitespacesAndNewlines), !name.isEmpty {
                    parts.append(name)
                }
            }
        }
        return parts.isEmpty ? nil : parts.joined(separator: " · ")
    }

    private var highlightedTitle: AttributedString {
        var result = AttributedString(article.title)
        let tokens = highlightQuery.split(whereSeparator: { $0.isWhitespace }).map(String.init)
        guard !tokens.isEmpty else { return result }
        for token in tokens where !token.isEmpty {
            var searchRange = result.startIndex..<result.endIndex
            while let range = result[searchRange].range(of: token, options: [.caseInsensitive]) {
                result[range].foregroundColor = .accentColor
                result[range].font = .headline.bold()
                searchRange = range.upperBound..<result.endIndex
            }
        }
        return result
    }

    private var projectImage: some View {
        AsyncImage(url: article.imageURL) { phase in
            if let image = phase.image {
                image.resizable().scaledToFill()
            } else {
                Image(systemName: "curlybraces")
                    .font(.title3)
                    .foregroundStyle(Color.accentColor)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.accentColor.opacity(0.09))
            }
        }
        .frame(width: 64, height: 86)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .accessibilityHidden(true)
    }
}

/// System browser supplies article rendering, navigation, sharing and Safari hand-off.
struct ArticleBrowser: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        let configuration = SFSafariViewController.Configuration()
        configuration.entersReaderIfAvailable = false
        return SFSafariViewController(url: url, configuration: configuration)
    }

    func updateUIViewController(_ controller: SFSafariViewController, context: Context) {}
}
