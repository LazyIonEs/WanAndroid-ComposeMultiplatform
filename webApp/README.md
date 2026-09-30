# WanAndroid Web

Web UI uses React, semantic HTML and CSS. `sharedLogic` is a Kotlin/JS library,
as in the reference KotlinProject. No Compose canvas or Wasm UI is used.
The Kotlin module owns networking, article parsing, feed state, project
categories and pagination; React only renders its exported JSON view state.

## Local development

Requires JDK 21 and Node.js 22.12+ (or a newer supported Node release).
From the repository root:

```sh
./gradlew :sharedLogic:jsBrowserProductionLibraryDistribution
npm install
npm start
```

Open `http://127.0.0.1:8080`. Rebuild the shared Kotlin library after changing
`sharedLogic`; restart Vite if its dependency cache does not pick up the change.
The root npm workspace resolves the generated Kotlin package directly and
installs its transitive dependencies. Install dependencies from the repository
root, not inside `webApp` or the generated Kotlin package.

```sh
npm run build --workspace webApp
npm run preview
```

Production preview runs at `http://127.0.0.1:8081`. Both Vite modes proxy `/api/`
to `https://www.wanandroid.com/`, verify the upstream TLS certificate and
rewrite upstream cookie domains/paths for the local origin.

## Deployment

Serve `dist/` from a web server and configure a same-origin `/api/` reverse
proxy. A static file host alone cannot bypass the upstream API's CORS policy.
`deploy/nginx.conf` provides an example for nginx with upstream TLS verification
enabled. Adjust its public hostname, document root, HTTPS termination and CA
bundle path to your deployment. Do not proxy arbitrary user-provided hosts.

## UI behavior

- Home, plaza and project feeds leave vertical overscroll enabled for the
  browser's native pull-to-refresh. Supporting mobile browsers reload the page;
  availability depends on the browser and its settings. Desktop users can use
  the browser's reload command. There are no custom touch/drag gesture handlers.
- Native `IntersectionObserver` loads the next page when the list bottom comes
  into view. Loading stops after any request error until an explicit retry;
  older browsers without this API receive a load-more button.
- Loading, empty feeds, empty project categories, initial errors, append errors
  and end-of-list have distinct feedback. Existing articles remain visible when
  a refresh or append fails. Switching tabs retains loaded content; full browser
  reloads fetch fresh content. Page-title banners and toolbar refresh buttons
  have been removed.
- Articles open only HTTP(S) destinations in a new tab with `noopener noreferrer`.
  API strings render as text; the client does not inject upstream HTML.
- URL hashes support browser back/forward navigation and direct tab links.
- Theme follows the OS by default; users can choose light or dark in Settings.
  This presentation preference is stored locally when browser storage is available.
- Semantic navigation, labeled category controls, keyboard focus, loading
  announcements and reduced-motion preferences are supported.

The generated Kotlin TypeScript declarations are consumed directly. Run
`npm run typecheck --workspace webApp` after shared bridge changes to verify the
integration contract.
