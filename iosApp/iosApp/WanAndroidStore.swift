import Combine
import Foundation
import SharedLogic

/// Swift owns presentation only. Networking, paging and category selection live in SharedLogic.
@MainActor
final class WanAndroidStore: ObservableObject {
    @Published private(set) var state = NativeAppState()
    @Published private(set) var decodingError: String?

    private let client = WanAndroidClient()
    private var subscription: StateSubscription?
    private var activated: Set<AppSection> = []
    private var refreshWaiters: [AppSection: RefreshWaiter] = [:]

    func start() {
        guard subscription == nil else { return }
        subscription = client.watch { [weak self] json in
            // SharedLogic may deliver on a background dispatcher. SwiftUI updates stay on main.
            Task { @MainActor [weak self] in self?.receive(json) }
        }
        activated.insert(.home)
        client.start()
    }

    func activate(_ section: AppSection) {
        guard section != .settings, activated.insert(section).inserted else { return }
        client.refresh(section: section.rawValue)
    }

    func refresh(_ section: AppSection) async {
        guard section != .settings else { return }
        let requestID = UUID()
        await withTaskCancellationHandler {
            await withCheckedContinuation { continuation in
                guard !Task.isCancelled else {
                    continuation.resume()
                    return
                }
                finishRefresh(section)
                refreshWaiters[section] = RefreshWaiter(id: requestID, continuation: continuation)
                client.refresh(section: section.rawValue)
                // Subscribe AFTER dispatch: the immediate snapshot belongs to this refresh.
                // StateFlow may conflate busy and idle, so completion must not require a busy edge.
                refreshWaiters[section]?.subscription = client.watch { [weak self] json in
                    Task { @MainActor [weak self] in
                        self?.receiveRefresh(json, section: section, requestID: requestID)
                    }
                }
            }
        } onCancel: {
            Task { @MainActor [weak self] in
                self?.finishRefresh(section, requestID: requestID)
            }
        }
    }

    func cancelRefresh(_ section: AppSection) {
        finishRefresh(section)
    }

    func retry(_ section: AppSection) {
        if state.feed(for: section).failedToLoadMore == true {
            client.loadMore(section: section.rawValue)
        } else {
            client.refresh(section: section.rawValue)
        }
    }

    func loadMore(_ section: AppSection) {
        client.loadMore(section: section.rawValue)
    }

    func selectCategory(_ id: Int) {
        client.selectProject(categoryId: Int32(id))
    }

    private func receiveRefresh(_ json: String, section: AppSection, requestID: UUID) {
        guard refreshWaiters[section]?.id == requestID else { return }
        guard let snapshot = try? JSONDecoder().decode(NativeAppState.self, from: Data(json.utf8)) else {
            finishRefresh(section, requestID: requestID)
            return
        }
        let feed = snapshot.feed(for: section)
        // Category loading can emit an idle snapshot just before the first project's request.
        let waitingForFirstProject = section == .projects && snapshot.selectedProjectId != nil
            && !feed.loaded && feed.error == nil
        let refreshing = feed.loading || feed.refreshing
            || (section == .projects && snapshot.categoriesLoading) || waitingForFirstProject
        if !refreshing { finishRefresh(section, requestID: requestID) }
    }

    private func receive(_ json: String) {
        do {
            state = try JSONDecoder().decode(NativeAppState.self, from: Data(json.utf8))
            decodingError = nil
        } catch {
            decodingError = "内容暂时无法显示，请重试。"
            finishRefreshes()
        }
    }

    private func finishRefreshes() {
        for section in Array(refreshWaiters.keys) { finishRefresh(section) }
    }

    private func finishRefresh(_ section: AppSection, requestID: UUID? = nil) {
        guard let pending = refreshWaiters[section], requestID == nil || pending.id == requestID else { return }
        refreshWaiters.removeValue(forKey: section)
        pending.subscription?.close()
        pending.continuation.resume()
    }

    deinit {
        subscription?.close()
        client.close()
        refreshWaiters.values.forEach {
            $0.subscription?.close()
            $0.continuation.resume()
        }
    }

    private struct RefreshWaiter {
        let id: UUID
        let continuation: CheckedContinuation<Void, Never>
        var subscription: StateSubscription?
    }
}
