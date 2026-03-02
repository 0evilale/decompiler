# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A GUI application for browsing and decompiling Java archives (JAR/WAR/ZIP) using multiple decompiler backends. Built with Java/Kotlin and Swing (FlatLaf).

## Build & Run Commands

```bash
# Build the project
./gradlew build

# Run the application (optionally pass a JAR file path as argument)
./gradlew run

# Create the distributable fat JAR (output: build/libs/decompiler.jar)
./gradlew shadowJar

# Run using the shadow JAR
./gradlew runShadow

# Create distribution archives (tar/zip)
./gradlew assembleDist

# Clean build artifacts
./gradlew clean
```

Tests are currently **disabled** (`test { enabled = false }` in `build.gradle.kts`). There is no real test suite — the test source is a placeholder.

## Architecture

The project follows **MVC** with a **Strategy pattern** for decompilers.

### Entry Point
`src/main/kotlin/moe/sota/decompiler/Main.kt` — initializes FlatLaf theme, fonts, and the main window. Accepts an optional JAR path as a CLI argument.

### Layer Structure

**Controllers** (manage state and user interaction):
- `WindowController` — main window singleton
- `TabsController` — tabbed interface and decompiler selection (singleton)
- `TabController` — individual tab content and async decompilation
- `TreeController` — archive file tree navigation (singleton)

**Models** (tree structure of archive contents):
- `ArchiveModel` → `PackageModel` → `FileModel`, all extending `BaseModel`

**Views** (Swing UI):
- `WindowView` — main JFrame with split pane (tree + tabs)
- `TabsView` — `JTabbedPane` with decompiler selector combo box
- `TabView` — individual tab using `RSyntaxTextArea`
- `TreeView` — `JTree` for archive navigation

**Services** (business logic):
- `LoaderService` — async JAR parsing into model tree; handles drag-and-drop
- `TypeService` — maps file extensions/names to `Type` handlers
- `ProcessService` — Java Preferences API for user settings (e.g. last-used decompiler); spawns new instances
- `LanguageService` — i18n via `.properties` files (8 languages in `resources/langs/`)

**Transformers** (decompiler strategy implementations in `src/main/java/.../transformers/`):
- `Transformer` enum — factory for creating decompiler instances
- `ITransformer` interface — `String transform(FileModel fileModel) throws Exception`
- Implementations: `CFRTransformer`, `JDTransformer`, `ProcyonTransformer`, `VineflowerTransformer`

**Types** (file type handlers in `src/main/java/.../types/`):
- `ClassType` — `.class` files, triggers decompilation
- `ImageType` — renders images with auto-scaling
- `ManifestType` — `MANIFEST.MF` display

### Key Flow: Opening and Decompiling a File

1. User opens a JAR → `LoaderService.loadAsync()` parses ZIP entries into an `ArchiveModel` tree
2. User clicks a file in `TreeView` → `TabsController.addTab(fileModel)` creates a `TabController`
3. `TabController.updateAsync()` calls `TypeService` to get the file type, then either:
   - Runs the selected `ITransformer` for `.class` files
   - Renders image for image files
   - Displays raw text otherwise
4. Result displayed in `RSyntaxTextArea` with Java syntax highlighting

### Agent Submodule

`agent/` is a separate Gradle submodule (Java 11) that builds a Java instrumentation agent. Currently a stub. The main build copies the agent ZIP via `processResources`.

## Key Dependencies

| Purpose | Library |
|---------|---------|
| UI Look & Feel | `com.formdev:flatlaf:3.5.4` |
| Code editor | `com.fifesoft:rsyntaxtextarea:3.5.4` |
| Layout | `com.miglayout:miglayout-swing:11.4.2` |
| Decompiler: CFR | `net.fabricmc:cfr:0.2.2` |
| Decompiler: JD | `com.github.java-decompiler:jd-core:v1.1.3` |
| Decompiler: Procyon | `org.bitbucket.mstrobel:procyon-compilertools:0.6.0` |
| Decompiler: Vineflower | `org.vineflower:vineflower:1.11.1` |
| Boilerplate reduction | Lombok (`io.freefair.lombok` plugin) |

## Lombok + Kotlin Interop

Kotlin cannot see Lombok-generated getters for `private` Java fields during compilation (Kotlin processes Java stubs before Lombok annotation processing). Solutions:
- Declare Java fields as `public` (like `BaseController.view`), OR
- Add explicit public methods in Java instead of relying on `@Getter` for fields that Kotlin needs to access.

## Key Dependencies

The search UI components (`FindToolBar`, `ReplaceToolBar`, `GoToDialog`, `SearchListener`, `SearchEvent`) are in the separate `com.fifesoft:rstaui` artifact under package `org.fife.rsta.ui.*`, NOT in `rsyntaxtextarea`.

## Adding a New Decompiler

1. Create a class in `transformers/` implementing `ITransformer`
2. Add an entry to the `Transformer` enum
3. The combo box in `TabsView` is populated from the enum — no other UI changes needed
