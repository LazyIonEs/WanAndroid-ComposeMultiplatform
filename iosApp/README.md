# iOS

The iOS app uses SwiftUI for the home, plaza, project and settings screens. Article details use `SFSafariViewController`. It imports the UI-independent `SharedLogic` Kotlin framework for HTTP requests, pagination, project selection and feed state.

## Run

1. Install Xcode and an iOS simulator runtime. The app's deployment target is iOS 18.2; the current Kotlin targets support Apple Silicon simulators and arm64 devices.
2. Set up the repository's Java 21 / Android SDK build prerequisites.
3. Open `iosApp.xcodeproj`, select the `iosApp` scheme and an available simulator, then run. The build phase calls `:sharedLogic:embedAndSignAppleFrameworkForXcode` and links `SharedLogic` automatically.
4. For a physical device, select your development team in the target's Signing & Capabilities settings.

From the repository root, an unsigned simulator build can also be run with:

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -configuration Debug -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath /tmp/wanandroid-ios-build CODE_SIGNING_ALLOWED=NO build
```

## Android Studio builds

The `Compile Kotlin Framework` phase always invokes the incremental
`:sharedLogic:embedAndSignAppleFrameworkForXcode` task, including when Android
Studio sets `OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES`. That flag alone does not
guarantee that the IDE has prepared the new `sharedLogic` module. Skipping the
phase after a clean build can otherwise leave Swift unable to import
`SharedLogic`; reusing an old framework can also hide Kotlin changes.

Gradle checks whether the framework is up to date for the selected SDK and build
configuration. Keep this build phase before Compile Sources, and retain the
`sharedLogic/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)` search path.
No manual framework build or IDE cache deletion is required.

## State and lifecycle

`WanAndroidStore` decodes shared JSON snapshots and publishes presentation state on the main actor. Swift owns tab selection, appearance preferences and article presentation; it does not implement a separate network or pagination stack.

Each native pull-to-refresh action observes the current shared state after dispatching its request. Completion therefore works even when `StateFlow` combines intermediate loading updates. Repeated refreshes replace the previous wait, and cancellation or leaving a feed closes its temporary subscription. The main subscription and client are released with the store.

Feed refresh uses SwiftUI's `List.refreshable` and its system indicator. Reaching the list's last row loads the next shared page through `onAppear`; list-row identity changes after loading so SwiftUI checks visibility again, including when an API page contains no articles but still has a next page. Failed requests stop automatic loading until an explicit retry or a pull to refresh. Existing articles remain visible during refresh and append errors. Loading, empty content, unavailable project categories, errors and the end of a feed all have native SwiftUI states. Feed tabs have no duplicate page heading or refresh toolbar button.

## Verification

The Android Studio override case was reproduced with no `sharedLogic/build`
directory: an unsigned `iphoneos` build with
`OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES` failed to resolve `SharedLogic` before
the build-phase fix, then built and linked successfully with the fix.

The standard Xcode 27 Debug simulator build has passed, including its Gradle framework assembly phase and the framework search paths from `Config.xcconfig`, without overriding either. Swift syntax, project/plist validity and whitespace checks also passed. At implementation time no available simulator runtime was installed, so launching the app and visual interaction checks were not performed. On a simulator or device, check all tabs, category changes, article opening, refresh/pagination failures and cancellation, and theme changes.

## Mobile reading design (2026-09-30)

The native feed now includes a section introduction, working shortcuts from home
to the community and projects, a distinct pinned-article treatment, author and
date details, and clearer project categories. Cards use semantic system colors,
a light/dark accent asset, Dynamic Type, and a 760-point maximum reading width.
Project thumbnails fall back to a code symbol and yield space to text at
accessibility sizes. Long-pressing an article offers the system share sheet.
Appearance choices remain persisted through AppStorage. Refresh and pagination
continue to use the existing native List and shared state subscription.

The updated simulator build passed. Home-feed screenshots were checked at
standard size and with dark appearance plus accessibility-large text on the
iPhone 18 Pro simulator. The simulator's original appearance and text size were
restored afterward. Full tab, category and article interaction testing on iOS
remains unverified in this round.
