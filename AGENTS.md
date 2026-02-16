# AGENTS.md

> **System Instruction**: This file defines the context and constraints for AI agents working on **MinusOne Cloud Music**.

## 1. Project Identity
- **Name**: MinusOne Cloud Music (减一云音乐)
- **Type**: Third-party NetEase Cloud Music (NCM) client.
- **Goal**: "MinusOne" — Remove ads, podcasts, social bloat. Pure music experience.

## 2. Tech Stack
| Category      | Technology         | Notes                      |
|:--------------|:-------------------|:---------------------------|
| **Language**  | Kotlin             |                            |
| **UI**        | Jetpack Compose    | Material 3.                |
| **Legacy UI** | Android Views      | XML/Fragments (Migrating). |
| **Arch**      | MVVM + Clean       | Single Activity.           |
| **DI**        | Hilt               |                            |
| **Async**     | Coroutines + Flow  |                            |
| **Network**   | Retrofit + OkHttp  | Moshi.                     |
| **Media**     | Media3 (ExoPlayer) |                            |
| **Image**     | Coil 3             |                            |

## 3. Architecture & Guidelines

### UI Layer
- **Compose First**: New UI must be Jetpack Compose.
- **State**: Use `StateFlow<UiState>` in ViewModels.

### Data Layer
- **API**: Undocumented NCM APIs. Handle errors gracefully (no crashes).
- **Persistence**: Cache responses only if critical for offline playback.

### Native (NDK)
- **Path**: `app/src/main/cpp`
- **Rule**: Security constants storage.

## 4. Coding Standards

### Reverse Engineering
- **Naming**: Match NCM decompiled names/API fields.
- **Comments**: **CRITICAL**: Preserve comments referencing original NCM classes/IDs (e.g., `// NCM: MyFriendActivity`).

### Dependencies
- **Catalog**: Use `gradle/libs.versions.toml`. No hardcoded versions.
- **Versions**: **CRITICAL**: AI training data is outdated.
    - **Existing**: STRICTLY use versions from `gradle/libs.versions.toml`.
    - **New**: Check for latest stable versions only when adding new libraries.

## 5. Workflow
- **Debugging**: Use **Chucker** (debug builds) for HTTP inspection.

## 6. Structure
- `app/`: Monolithic module.
- `app/src/main/java/`: Kotlin source.
- `app/src/main/cpp/`: Native code (Security constants).
- `.../minusonecloudmusic/`: Root package.
    - `ui/`: Compose screens & ViewModels.
    - `data/`: Repositories & DataSources.
    - `domain/`: UseCases.
    - `model/`: Data models.
    - `service/`: Media3 PlaybackService.
    - `player/`: Player logic & utilities.

---
**Note**: Consistency with existing code > New patterns.
