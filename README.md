# Vitala

Native Android health app: health news and journals, a disease reference library, and daily health tips with a scheduled notification. Bilingual (English/Bengali).

## Setup from Termux

```
gh repo create Vitala --public --source=. --push
```

Every push to `main` triggers `.github/workflows/build.yml`, which builds a debug APK and uploads it as a workflow artifact.

```
gh run list --limit 1
gh run download <run-id> -n vitala-debug-apk
```

## Architecture

- **Data**: Room (`data/local`) + a JSON-seeded disease library (`assets/diseases.json`) + live news from WHO's RSS feed and PubMed's E-utilities API (`data/remote`). `data/repository` wraps both behind `Flow`-returning repositories that the UI observes reactively.
- **UI**: Jetpack Compose, Material 3, dark-premium amber-on-near-black theme. No DI framework — `VitalaApplication` holds lazily-built singletons (database, repositories, `PreferencesManager`) that screens reach via `LocalContext.current.applicationContext as VitalaApplication`.
- **Persistence beyond Room**: `data/prefs/PreferencesManager` (DataStore) holds the one-time disclaimer flag and the persisted reminder time.
- **Localization**: all app-chrome strings live in `res/values/strings.xml` (English) and `res/values-bn/strings.xml` (Bengali) — disease/news *content* itself (JSON data, fetched articles) is not translated.

## Done

- Bottom navigation (Home / News / Library / Tips), with `news/{articleId}` and `library/{diseaseId}` detail routes. Highlighting now works correctly on detail screens too.
- Room database seeded on first launch: diseases from `assets/diseases.json` (14 entries); news starts from a small fallback, immediately replaced by a live fetch; tips are a hardcoded seed list.
- `data/remote/RemoteNewsSource` fetches live "news" from WHO's RSS feed and "journal" articles from PubMed's E-utilities API — both free, no API key. `NewsRepository.refresh()` pulls both and atomically replaces the non-bookmarked cache, called on app startup and whenever the News tab opens.
- **Bookmarking**: articles can be saved from the detail screen; the News list has an All/Saved filter. Saved articles survive a refresh (they're excluded from the replace-cache cycle) — see `NewsArticleDao.replaceAll`.
- **Search + filters**: text search on both News and Library; the Library also has scrollable category chips (`DiseaseFilter`, a pure/testable utility).
- **Last-synced visibility**: the News screen shows a manual refresh button, a spinner while refreshing, and "last synced Xm ago" / an error line otherwise (`NewsRepository.isRefreshing` / `lastSyncedAt` / `lastError`).
- **Disclaimer screen**: shown once before the main app, gated on a DataStore flag (`PreferencesManager.hasAcceptedDisclaimer`).
- **Notification deep-link**: tapping the daily-tip notification opens the Tips screen directly (`MainActivity.EXTRA_DEEP_LINK_ROUTE`, `onNewIntent`, `launchMode="singleTop"`). This also fixed a real bug — the notification was previously showing hardcoded placeholder text instead of the actual day's tip.
- **Persisted reminder time**: survives app restart via DataStore (the WorkManager schedule itself already persisted; this persists the *displayed* selection).
- **Home screen widget**: `widget/DailyTipWidgetProvider` — classic `RemoteViews`/`AppWidgetProvider` (not Glance, to keep the dependency surface small), shows today's tip, updates roughly every 30 min (the OS-enforced minimum), tapping deep-links into the Tips screen.
- **App icon**: real adaptive icon (amber heart on near-black), replacing the system placeholder.
- **ProGuard readiness**: `app/proguard-rules.pro` keeps the Gson-deserialized model classes (`data/model/**`, PubMed response classes) so minification won't silently null out their fields. `isMinifyEnabled` is still `false` — flip it on only after testing a release build, since this was never compiled in this environment.
- **Unit tests**: `DiseaseFilterTest` (search/category logic) and `TipRepositoryTest` (day-seeded tip selection, using a fake DAO — no Android runtime needed). Run with `./gradlew test`.

- **In-app language switcher**: a language card on the Tips screen (English / বাংলা pills) calling `AppCompatDelegate.setApplicationLocales()` — the official per-app language API. `MainActivity` extends `AppCompatActivity` (not plain `ComponentActivity`) specifically so this actually re-applies on Android 12 and below — without it, AppCompat stores the preference but never re-wraps resources on recreation, so the UI silently stays in the old language. `Theme.Vitala` had to move to an AppCompat-lineage parent (`Theme.AppCompat.NoActionBar`) to match, since `AppCompatActivity` throws at startup on a non-AppCompat theme. Android 13+ also has this covered natively regardless (`LocaleManager`), and the choice shows up in Settings → Apps → Vitala → Language there too.

## Known simplifications / follow-ups

- **Debug signing is now stable**: `app/debug.keystore` is committed and `app/build.gradle.kts` points the `debug` signing config at it explicitly. Before this, GitHub Actions' ephemeral runners each auto-generated a *different* random debug key per build, so installing a newer APK over an older one always failed with "App not installed" (signature mismatch). This is fixed going forward — but you'll need one clean uninstall of whatever's currently on your phone, since that copy was signed with one of the old throwaway keys. After that, every future build from this repo will install cleanly over the last one.
- **Room migration**: the `isBookmarked` column bump uses `fallbackToDestructiveMigration()` — fine pre-release (wipes local data on schema change), but replace with a real `Migration` before this ships to real users with data worth keeping.
- **No swipe-to-refresh gesture**: Material3's `PullToRefreshBox` needs a newer Compose BOM than this project pins, and I didn't want to bump it blind (already had one real build break from an unverified version). The manual refresh button covers the same need; add the gesture later if you bump the BOM.
- **Time labels aren't localized**: the three reminder time pills ("7:00 am" etc.) are plain literals, not locale-aware formatted times.
- **Disease dataset**: 14 entries, written from general medical knowledge — not medically reviewed. Have it checked before real users see it.
- **No real device/CI verification**: everything here was written and statically checked (import/reference cross-checks, brace-balance checks) but never compiled — there's no Android SDK in this environment. Push and watch the Actions run; report back anything that fails and I'll fix it.
