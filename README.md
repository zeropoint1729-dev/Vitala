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

## Still stubbed out

- `ui/home`, `ui/news`, `ui/library`, `ui/tips` — screens have TODOs where the real layouts (matching the dark-premium mockups) go.
- `data/local` — no Room `AppDatabase` or DAOs yet.
- `data/remote` — no Retrofit `NewsApi` interface yet.
- `assets/diseases.json` — the curated, sourced disease dataset hasn't been added.
- Launcher icon — manifest currently points at a system placeholder icon.
- `NotificationScheduler.schedule(context)` needs to be called once (e.g. from `MainActivity` or a settings screen) to start the daily reminder.
