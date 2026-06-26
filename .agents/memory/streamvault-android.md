---
name: StreamVault Android
description: Full Kotlin + Jetpack Compose Android APK project, Stremio-like premium streaming app
---

# StreamVault Android Project

## Key decisions
- **No local builds** — all compilation happens via GitHub Actions only
- **TMDB_API_KEY** injected as `BuildConfig.TMDB_API_KEY` from env var / gradle property
- **Release signing** reads `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` from env vars in CI
- **gradle-wrapper.jar** must be bootstrapped in CI (curl from gradle/gradle repo or sdkman fallback) — it's a binary that can't be committed as text

## Package structure
- `com.streamvault.app` — root
- Architecture: MVVM + Hilt + Room + Retrofit + Coroutines + Navigation Compose

## Addon priority
- English: Torrentio → KnightCrawler → Jackettio → Comet
- Indian: MediaFusion → Torrentio → Jackettio → KnightCrawler
- Anime: KnightCrawler → Torrentio → Jackettio

## Dependencies (key)
- Kotlin 1.9.22, KSP 1.9.22-1.0.17, Hilt 2.50, Compose BOM 2024.02.00
- Media3/ExoPlayer 1.2.1, Room 2.6.1, Retrofit 2.9.0, Coil 2.5.0
- libtorrent4j-android-arm64:2.1.0-30 (JitPack)
- Min SDK 26, Target SDK 34, Java 17

## GitHub Actions secrets needed
- TMDB_API_KEY (required for any build)
- KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD (release only)

**Why:** User explicitly said no local builds — CI-only workflow is the design intent.
