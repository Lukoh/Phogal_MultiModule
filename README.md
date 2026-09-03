## Phogal_MultiModule — Multi-Module + SSOT Android Showcase

Project Transition: Phogal_Migrate ➔ Phogal_With_Room ➔ Phogal_MultiModule Applied Principles: Multi-Module Architecture · SSOT (Single Source of Truth) via Room · MVVM + UDF (Unidirectional Data Flow) Navigation: Navigation 3 (Nav3-only, type-safe NavKey routes)
Phogal is an Unsplash-based photo gallery app used as a living showcase of modern Android architecture. This repository is the third evolution of the project: after migrating the data layer to a strict Room-backed SSOT (Phogal_With_Room), the entire codebase has now been restructured from a single app module into 12 Gradle modules — isolating each of the 4 bottom-navigation tabs into its own feature module on top of a shared core layer.

## 1. Multi-Module Architecture

Phogal Multi-Module Architecture

Module Map (12 modules · 202 Kotlin files)

Tier	Module	Role
App	app	Entry point: MainActivity, HomeScreen (bottom tabs), Navigation 3 graph (Routes + PhogalEntryProvider + NavigationState), Hilt aggregation point (RepositoryModule @Binds)
Feature	feature:gallery	Tab 1 — photo search (UI · ViewModel · Repository, Paging 3)
Feature	feature:popularphotos	Tab 2 — popular photos feed (UI · ViewModel · Repository)
Feature	feature:notification	Tab 3 — notifications (UI)
Feature	feature:setting	Tab 4 — settings, bookmarked photos, following users (UI · ViewModel)
Feature	feature:common	Screens & state holders shared across tabs: picture viewer, user photos, WebView, follow; repositories (photo / user / bookmark / follow / download), BaseRepository
Core	core:model	Serializable data models (remote responses + local models)
Core	core:common	Kotlin/Compose extensions, utils, coroutine dispatchers & scopes, UiState/NetworkResult mapping
Core	core:network	Retrofit 3 + OkHttp 5 Unsplash REST API, NetworkModule (Json / interceptors / cookie jar), Coil image sizing interceptor
Core	core:database	Room — the SSOT: 3 Entities, 3 DAOs, PhotoFeedRemoteMediator, @ProvidedTypeConverter, DatabaseModule
Core	core:datastore	Jetpack DataStore preferences (LocalDataSource)
Core	core:ui	Design system components, theme, shared resources (drawable / values / raw)
Dependency Rules (verified: 0 boundary violations, 0 cycles)

graph TD
    app --> FG[feature:gallery] & FP[feature:popularphotos] & FN[feature:notification] & FS[feature:setting]
    FG & FP & FN & FS --> FC[feature:common]
    FC --> CM[core:model] & CC[core:common] & CN[core:network] & CD[core:database] & CS[core:datastore] & CU[core:ui]
    CC --> CM & CN & CU
    CN --> CM
    CD --> CM & CN
    CS --> CM
Features never depend on each other — only on feature:common and core:*. Tab-to-tab navigation is fully callback-based, so no feature knows the nav graph.
The nav graph lives only in app (Routes are NavKey data objects/classes; PhogalEntryProvider maps all 12 routes to feature screens).
core:database may read core:network (the RemoteMediator writes API pages into Room inside one transaction) — the deliberate SSOT direction.
Migration was package-preserving: module boundaries changed, com.goforer.* packages did not — keeping the diff reviewable (file moves + a handful of mechanical edits such as R → core.ui.R and AppModule → NetworkModule).
2. The SSOT Principle (Single Source of Truth)

In earlier versions, API responses were cached in-memory inside ViewModels and rendered directly, which fragmented state — the Feed and Detail screens could disagree about the same photo's "Like" status. The Room migration fixed this, and the multi-module split keeps the rule enforceable at the module boundary:

Network as a Writer Only — API responses are strictly written into the Room database (core:network → core:database).
Database as the Sole Emitter — UI layers subscribe only to observable streams (Flow / PagingSource) emitted by Room.
Reactive Updates — a mutation (e.g., toggling "Like") patches the database row; Room re-emits to every active observer, synchronizing the whole app instantly.
Data Flow

flowchart LR
    API[REST API] -- "1. Fetch" --> MED[RemoteMediator / Repo]
    MED -- "2. Write to DB" --> ROOM[(Room DB — SSOT)]
    ROOM -- "3. Flow / PagingSource" --> REPO[Repository]
    REPO -- "4. Mapping" --> VM[ViewModel]
    VM -- "5. StateFlow" --> UI[Compose UI]
    UI -. "6. User action (Like)" .-> REPO
    REPO -. "7. Read-Modify-Write" .-> ROOM
Core Implementation

Paged feeds (Search · Popular · User photos) use the Paging 3 RemoteMediator pattern: the UI collects LazyPagingItems backed exclusively by Room's PagingSource; when data runs out, PhotoFeedRemoteMediator.load() fetches from the REST API and commits the payload in a single Room transaction; Room invalidates the PagingSource and the UI updates seamlessly. Offline, cached records render instantly and network errors surface as LoadState.Error without clearing the screen.

Detail screens use a multi-emission observable query — emit cache first (offline-first), refresh from network writing only to the DB, then delegate all further emissions to Room's reactive stream:

override fun getPictureStream(id: String): Flow<NetworkResult<Picture>> = flow {
    pictureDao.getPicture(id)?.let { emit(NetworkResult.Success(it.picture)) }
    when (val result = safeApiCall { api.getPhoto(id) }) {
        is NetworkResult.Success -> pictureDao.upsert(PictureEntity.of(result.data))
    }
    emitAll(pictureDao.observePicture(id).filterNotNull()
        .map { NetworkResult.Success(it.picture) }.distinctUntilChanged())
}.flowOn(ioDispatcher)
SSOT components (all in core:database):

Category	Files	Role
Database	PhogalDatabase.kt, DatabaseModule.kt	Room instance (3 Entities / 3 DAOs) + Hilt provisions
Entities	PhotoFeedEntity, PictureEntity, RemoteKeyEntity	Feed cache, detail cache, pagination keys
DAOs	PhotoFeedDao, PictureDao, RemoteKeyDao	Observable Flows and PagingSources
Pagination	PhotoFeedRemoteMediator	Transactional network→DB bridging
Converters	PhogalTypeConverters	@ProvidedTypeConverter reusing the app-wide Json
3. Tech Stack

Area	Stack
Language / Build	Kotlin 2.4.10, AGP 9.3.2, KSP 2.3.11, Gradle Version Catalog, JDK 17 toolchain
UI	Jetpack Compose (BOM 2026.08.00), Material 3 (+ window-size / adaptive), Lottie Compose, Coil
Navigation	Navigation 3 (1.1.6) — NavDisplay + NavEntry, type-safe serializable NavKey routes, adaptive list-detail (material3-adaptive-navigation3)
DI	Hilt 2.60.1 (KSP), hilt-navigation-compose
Async / State	Coroutines + Flow, StateFlow + stateIn, sealed UiState, UDF
Data	Room 2.8.4 (SSOT), Paging 3.5.1 (RemoteMediator), Jetpack DataStore, kotlinx.serialization 1.11.0
Network	Retrofit 3.0.0, OkHttp BOM 5.5.0, kotlinx-serialization converter, persistent cookie jar
Etc.	Accompanist (WebView · permissions), Timber, core-library desugaring
SDK	minSdk 28 · targetSdk 36 · compileSdk 37
4. Build & Run

git clone https://github.com/Lukoh/Phogal_MultiModule.git
cd Phogal_MultiModule
# Windows only: if the wrapper script was shipped as gradlew.bat.txt (mail-safe rename), restore it:
#   ren gradlew.bat.txt gradlew.bat
./gradlew assembleProdDebug
Build variants: dev / stg / prod × debug / release.
First build after sync: run Build > Rebuild Project so KSP generates the Room DAOs (PhogalDatabase_Impl) and Hilt components.
Offline smoke test: enable Airplane Mode → Popular tab renders cache → Detail renders cache → "Like" shows an error snackbar while preserving cached data.
5. Project History

Stage	Repository	Focus
1	Phogal / Phogal_Migrate	Compose UI, refactored state holders, type-safe navigation
2	Phogal_With_Room	Room as strict SSOT, RemoteMediator paging, offline-first streams
3	Phogal_MultiModule (this repo)	12-module split: 4 tab features + shared feature layer + 6 core modules, Nav3-only graph, package-preserving migration verified by static analysis (syntax 202/202, boundary violations 0, FQN duplicates 0, DI graph satisfied)
