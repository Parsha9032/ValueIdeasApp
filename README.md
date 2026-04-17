# Value Ideas (v1.3)

**Value Ideas** is a professional Android application designed to help users capture, track, and implement their best ideas. Built with modern Android development practices, it provides a seamless experience for managing personal and professional growth through structured idea tracking.

## 🚀 Key Features

*   **Structured Idea Tracking**: Capture titles, detailed descriptions, and categories.
*   **Dynamic Categories**: Organize ideas into "Business", "Personal", "Tech", "Finance", or "Other".
*   **Progress Management**: Track implementation progress (0-100%) with manual seek-bars or milestone-based auto-calculation.
*   **Milestones**: Break down large ideas into 3 actionable milestones.
*   **Priority System**: Star important ideas to keep them at the top of your list.
*   **Smart Search & Filter**: Search by title/tags and filter by status (Not Started, In Progress, Completed).
*   **Archiving**: Clean up your main list by archiving ideas without deleting them.
*   **Sharing**: Share your ideas and plans with others via standard Android sharing intents.

## 🛠 Technical Stack

*   **Language**: Java
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Database**: Room Persistence Library (Schema Version 2)
*   **Reactive UI**: LiveData and ViewModel for lifecycle-aware data handling.
*   **UI Components**: Material Design 3, ViewBinding, CoordinatorLayout, RecyclerView.
*   **Threading**: ExecutorService for background database operations.

## 📂 Project Structure & Code Flow

### 1. Data Layer (`com.valueadd.app.data`)
*   **`entity/Idea.java`**: The core data model. Includes fields for category, status, progress, milestones, and archive status.
*   **`dao/IdeaDao.java`**: SQL queries for Room. Handles filtering, searching, and archiving.
*   **`repository/IdeaRepository.java`**: The "Single Source of Truth". Orchestrates data flow between the DAO and the ViewModel, handling background thread execution.
*   **`AppDatabase.java`**: Room database configuration. Currently at **Version 2** to support categories and archiving.

### 2. ViewModel Layer (`com.valueadd.app.viewmodel`)
*   **`IdeaViewModel.java`**: Acts as the bridge between UI and Data. It provides LiveData streams to the UI and contains business logic for status calculation and motivational messages.

### 3. UI Layer (`com.valueadd.app.ui`)
*   **`home/MainActivity.java`**: The entry point. Displays the list of ideas with search, filter chips, and navigation to the Archive.
*   **`detail/IdeaDetailActivity.java`**: View/Update progress. Users can toggle milestones, set priorities, share, or archive ideas from here.
*   **`add/AddEditIdeaActivity.java`**: Unified screen for creating new ideas or editing existing ones.
*   **`archive/ArchiveActivity.java`**: Dedicated space for viewing and restoring archived ideas.

## 🔄 Data Flow Example (Saving an Idea)
1.  **UI**: User enters data in `AddEditIdeaActivity` and clicks "Save".
2.  **ViewModel**: `IdeaViewModel.insert()` is called. It sets the `dateCreated`.
3.  **Repository**: `IdeaRepository` executes the insert on a background thread via `ExecutorService`.
4.  **DAO**: `IdeaDao` performs the SQL `@Insert`.
5.  **Reactive Update**: The `LiveData<List<Idea>>` in `MainActivity` automatically detects the change and refreshes the list via the observer.

## 📈 Version History (v1.3)
*   **Database Migration**: Bumped to version 2 to support `category` and `isArchived`.
*   **Stability Fixes**: Resolved infinite loops in detail view and data race conditions during updates.
*   **New UI**: Added Archive screen and Category dropdowns.

---
**Developed by**: Parsharamulu Mangol
**Target SDK**: 35 (Android 15)
