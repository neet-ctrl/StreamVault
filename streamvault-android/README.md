# StreamVault 🎬

> A premium Android streaming app — Stremio-inspired, AMOLED glassmorphism, multi-addon stream resolver.

## Features

- **Multi-Addon Stream Resolver** — Torrentio, MediaFusion, KnightCrawler, Jackettio, Comet, Cinemeta
- **TMDB Metadata** — Full movie/TV search, details, cast, trailers, recommendations
- **Advanced Player** — ExoPlayer/Media3 with gestures, PiP, subtitles, audio tracks, sleep timer, screen lock
- **AMOLED Glassmorphism UI** — Pure black (#000000) AMOLED with glass-effect cards, accent red (#E50914)
- **11 Screens** — Splash, Home, Search, Movie Details, Stream Sources, Player, Downloads, Library, History, Favorites, Settings
- **Downloads** — Torrent-based download queue with progress tracking via libtorrent4j
- **MVVM + Hilt + Room** — Clean architecture with Kotlin Coroutines + Flow

## Architecture

```
app/
├── data/
│   ├── remote/      # Retrofit APIs (TMDB + Stremio addons)
│   │   ├── api/     # TmdbApi, StremioApi
│   │   └── dto/     # Response DTOs + mappers
│   ├── local/       # Room database (history, favorites, downloads)
│   │   ├── dao/
│   │   └── entities/
│   └── repository/  # Repository implementations
├── domain/
│   ├── model/       # Pure Kotlin domain models
│   ├── repository/  # Repository interfaces
│   └── usecase/     # (extend as needed)
├── presentation/
│   ├── ui/
│   │   ├── screens/ # 11 Compose screens
│   │   ├── components/ # Reusable glassmorphic UI
│   │   ├── navigation/ # AppNavigation (single-activity)
│   │   └── theme/   # AMOLED dark Material3 theme
│   └── viewmodel/   # MVVM ViewModels (Hilt-injected)
├── di/              # Hilt modules (Network, Database, Repository)
├── worker/          # DownloadService (foreground)
└── util/            # Constants, Extensions
```

## Addon Priority

| Content | Priority |
|---------|---------|
| English | Torrentio → KnightCrawler → Jackettio → Comet |
| Indian | MediaFusion → Torrentio → Jackettio → KnightCrawler |
| Anime | KnightCrawler → Torrentio → Jackettio |

## Build via GitHub Actions (no local builds needed)

### Secrets to configure in your GitHub repo:

| Secret | Description |
|--------|-------------|
| `TMDB_API_KEY` | Your TMDB v3 API key from themoviedb.org |
| `KEYSTORE_BASE64` | Base64-encoded release keystore (for signed APK) |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias in keystore |
| `KEY_PASSWORD` | Key password |

### Trigger a build:

1. **Push to `main`** → builds a debug APK automatically
2. **Create a tag** `v1.0.0` → builds a signed release APK + GitHub Release
3. **Manual trigger** → Go to Actions → "Build StreamVault APK" → Run workflow → choose debug/release

### Download your APK:

After a workflow run completes, go to the Actions tab → click the run → scroll to **Artifacts** → download the APK.

## Requirements

- Min SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)
- Java: 17
- Kotlin: 1.9.22
- Gradle: 8.4

## Get a TMDB API Key

1. Go to [themoviedb.org](https://www.themoviedb.org/)
2. Create a free account
3. Go to Settings → API → Request API key
4. Add the key as `TMDB_API_KEY` in GitHub Secrets

## Generate a Release Keystore (for signed APK)

```bash
keytool -genkeypair -v \
  -keystore streamvault.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias streamvault \
  -storepass YOUR_STORE_PASS \
  -keypass YOUR_KEY_PASS \
  -dname "CN=StreamVault, O=StreamVault, C=US"

# Encode to base64 for GitHub secret
base64 -i streamvault.jks | tr -d '\n'
```

## Tech Stack

| Component | Library |
|-----------|---------|
| UI | Jetpack Compose + Material3 |
| DI | Hilt 2.50 |
| Database | Room 2.6.1 |
| Network | Retrofit 2.9 + OkHttp 4.12 |
| Player | Media3/ExoPlayer 1.2.1 |
| Images | Coil 2.5 |
| Coroutines | Kotlin Coroutines 1.7.3 |
| Torrent | libtorrent4j-android-arm64 2.1.0 |
| Navigation | Navigation Compose 2.7.6 |
