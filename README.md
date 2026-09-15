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

- Bottom navigation (Home / News / Library / Tips) wired in `MainActivity`.
- Room database (`AppDatabase`, DAOs, `Converters`) seeded with 2 diseases, 2 articles, and 5 tips on first launch.
- Repository layer (`DiseaseRepository`, `NewsRepository`, `TipRepository`) exposing `Flow`s.
- `HomeScreen` is fully live: today's tip (date-seeded pick), a 2-article news preview, and a 2-item disease preview, all from Room via `HomeViewModel`.

## Still stubbed out

- `ui/news`, `ui/library`, `ui/tips` — still just titles; need the same treatment as `HomeScreen`, plus the detail screens (`ArticleDetailScreen`, `DiseaseDetailScreen`) wired into the nav graph with an `{id}` argument.
- `data/remote` — no Retrofit `NewsApi` interface yet; news is Room-only seed data for now.
- `assets/diseases.json` — only 2 hand-written entries exist in `AppDatabase.kt`; the real curated, sourced dataset hasn't been added.
- Launcher icon — manifest currently points at a system placeholder icon.
- `NotificationScheduler.schedule(context)` is defined but not called anywhere yet — needs a call site (e.g. `MainActivity.onCreate` or a settings screen) to actually start the daily reminder.
