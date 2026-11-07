# AGENTS.md

> Context and constraints for AI agents working on **MinusOne Cloud Music (减一云音乐)**.
> See [ARCHITECTURE.md](ARCHITECTURE.md) for high-level architecture.

## 1. Project Identity & Goal
- **Type**: Third-party NetEase Cloud Music (NCM) Android client.
- **Goal ("MinusOne")**: Pure music experience—strip ads, podcasts, social bloat, and VIP upsells while replicating core NCM UI and behavior 1:1.

## 2. Reference Artifacts (`ref/`)
- **Captured API Payloads (`ref/api/`)**: Raw NCM request/response JSON fixtures.
- **Decompiled NCM Notes (`ref/decompiled/`)**: Contains `SCOUT_RULES.md` and verbatim identifier/path notes for an external scout agent searching a separate decompiled NCM project. Full decompiled source lives externally and is provided in context when needed.

## 3. Coding & Reverse-Engineering Standards
- **Fail-Fast**: Focus on the happy path. No silent defensive fallbacks; let errors surface early during development.
- **Relentless Refactoring**: Actively modernize, rename, and reorganize code. Fix anti-patterns immediately instead of preserving legacy structures.
- **NCM Naming & Comments**: Match decompiled NCM names and API fields. **CRITICAL**: Preserve comments referencing original NCM classes/IDs (e.g., `// NCM: MyFriendActivity`, `// NCM: v4ShadowViewMask`).
- **1:1 UI Replication**: Replicate exact NCM margins, alphas, z-indexes, shadows, and custom Canvas drawings using decompiled code as the source of truth.
- **9-Patch Assets**: Decompiled `.9.png` images often fail AAPT2. Strip `.9` from filenames and render natively or via Compose `ContentScale.FillBounds`.
- **Dependencies**: Strictly use `gradle/libs.versions.toml` as the single source of truth for library versions. Never hardcode versions in `build.gradle.kts`.