# AGENTS.md

## Project Overview
**MinusOne Cloud Music** (减一云音乐) is a lightweight, third-party NetEase Cloud Music (NCM) client for Android. It aims to provide a pure music experience by stripping away non-essential features like ads, podcasts, and social squares.

## Technology Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (Modern), View-based (Legacy/Migration in progress)
- **Architecture**: MVVM, Clean Architecture, Single Activity
- **Navigation**: Navigation 3 (Compose), Legacy Navigation Component
- **DI**: Hilt
- **Data**: Room, DataStore, SharedPreferences
- **Network**: Retrofit, Moshi, OkHttp
- **Media**: Media3 (ExoPlayer)
- **Image**: Coil 3
- **Build**: Gradle (Kotlin DSL), Version Catalogs

## Project Structure
- **`/app`**: Monolithic application module.
- **`src/main/cpp`**: Native C++ code (NDK) for basic cryptography/security.
- **`gradle/libs.versions.toml`**: Dependency management.

## Specific Configuration
- **API**: Direct NCM API integration using undocumented endpoints.
- **Debugging**: **Chucker** is available in debug builds for HTTP inspection.

## Guidelines
1.  **Compose Migration**: Prefer **Jetpack Compose** for new UI. Adopt a gradual migration strategy: migrate containers (Fragments, RecyclerViews) first, then inner content.
2.  **Naming Conventions**: Follow naming conventions of decompiled code or API fields if provided.
3.  **Navigation**: Align new logic with Navigation 3 patterns.
4.  **Media & Images**: Use Media3 for playback and Coil 3 for loading.
5.  **NCM API**: Handle network errors gracefully due to the unofficial nature of the API.
6.  **Large Screen**: Ensure UI adapts reasonably well to tablet/landscape (best-effort).
7.  **NDK**: Avoid modifying `src/main/cpp` unless necessary for security logic.
8.  **Philosophy**: Maintain the "MinusOne" approach—no ads, no bloat.