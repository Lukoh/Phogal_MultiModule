# Fix Search List Scroll Jump and Flickering

This plan addresses the issue where searching for photos causes the list to jump to a middle position (previous scroll position) and flicker multiple times before settling.

## User Review Required

> [!IMPORTANT]
> The fix involves changing how `PagingData` is emitted in the ViewModel and how `LazyListState` is managed in the UI. This should result in a smoother search experience, but I need to ensure it doesn't break pagination for other features.

## Proposed Changes

### Core UI Components

#### [MODIFY] [BaseSectionUiState.kt](file:///Users/namlukoh/StudioProjects/Phogal_MultiModule/feature/common/src/main/java/com/goforer/phogal/presentation/stateholder/uistate/home/common/base/BaseSectionUiState.kt)
- Update `resetScrollIfNeeded` to be more robust.
- Ensure `isResettingScroll` can be managed externally if needed.

### Gallery Feature

#### [MODIFY] [GalleryViewModel.kt](file:///Users/namlukoh/StudioProjects/Phogal_MultiModule/feature/gallery/src/main/java/com/goforer/phogal/presentation/stateholder/business/home/gallery/GalleryViewModel.kt)
- Avoid emitting `PagingData.empty()` as an initial value when a search is already active.
- Ensure the flow doesn't produce redundant emissions that cause UI flickering.

#### [MODIFY] [SearchPhotosSection.kt](file:///Users/namlukoh/StudioProjects/Phogal_MultiModule/feature/gallery/src/main/java/com/goforer/phogal/presentation/ui/compose/screen/home/gallery/SearchPhotosSection.kt)
- Pass the search `query` to the section.
- Use the `query` change to trigger an explicit scroll reset to index 0.
- Ensure `isResettingScroll` is correctly synchronized with the `LazyPagingItems` load state.

#### [MODIFY] [SearchPhotosContent.kt](file:///Users/namlukoh/StudioProjects/Phogal_MultiModule/feature/gallery/src/main/java/com/goforer/phogal/presentation/ui/compose/screen/home/gallery/SearchPhotosContent.kt)
- Pass the `currentQuery` down to `SearchPhotosSection`.

## Verification Plan

### Automated Tests
- I'll check if there are any existing tests for `GalleryViewModel` and update them if necessary.
- Run `assembleDebug` to ensure build stability.

### Manual Verification
1. Open the Gallery screen.
2. Search for a keyword (e.g., "dog").
3. Scroll down the list.
4. Search for a new keyword (e.g., "cat").
5. Verify that the list starts at the top (0th item) immediately and doesn't flicker multiple times.
