# Vitala

Native Android health app: health news and journals, a disease reference library, and daily health tips with a scheduled notification.

## Setup from Termux

```
gh repo create Vitala --public --source=. --push
```

Every push to `main` triggers `.github/workflows/build.yml`, which builds a debug APK and uploads it as a workflow artifact.

```
gh run list --limit 1
gh run download <run-id> -n vitala-debug-apk
```

## Done

- Bottom navigation (Home / News / Library / Tips) wired in `MainActivity`, with `news/{articleId}` and `library/{diseaseId}` detail routes.
- Room database (`AppDatabase`, DAOs, `Converters`) seeded on first launch: diseases load from `app/src/main/assets/diseases.json` (14 entries); news starts from a small hardcoded fallback and is immediately replaced by a live fetch; tips are still a hardcoded seed list.
- Repository layer (`DiseaseRepository`, `NewsRepository`, `TipRepository`) exposing `Flow`s.
- `HomeScreen`, `NewsListScreen`, `ArticleDetailScreen`, `DiseaseListScreen`, `DiseaseDetailScreen`, and `TipsHistoryScreen` are all live off Room — no more placeholder screens.
- `TipsHistoryScreen` has a working reminder toggle: pick a time, tap "Enable daily reminder", it requests `POST_NOTIFICATIONS` on Android 13+ and calls `NotificationScheduler.schedule(...)`.
- `data/remote` — `RemoteNewsSource` fetches live "news" from WHO's RSS feed (`who.int/rss-feeds/news-english.xml`, parsed with Android's built-in `XmlPullParser`, no library needed) and "journal" articles from PubMed's E-utilities (`esearch`/`esummary`, no API key required). `NewsRepository.refresh()` pulls both and atomically replaces the Room cache via `NewsArticleDao.replaceAll` — called once on app startup (`VitalaApplication.onCreate`) and again each time the News tab opens. If both sources fail (e.g. offline), the existing cached/seed data is left alone rather than wiped.

## Still stubbed out / not wired

- `assets/diseases.json` has 14 entries covering common chronic conditions plus a few especially relevant in Bangladesh (dengue, typhoid, tuberculosis, gastroenteritis). It's a good starter set, not a medically reviewed dataset — have it checked before shipping, and it's the only file that needs editing to add more diseases.
- Launcher icon — manifest currently points at a system placeholder icon.
- Reminder time selection isn't persisted (e.g. via DataStore) — it resets to 8:00 am default on app restart, though the underlying WorkManager schedule itself does persist.
- Bottom nav doesn't highlight "News"/"Library" while viewing a detail screen (`news/{id}`, `library/{id}` don't match the tab's exact route).
