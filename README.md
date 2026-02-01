# Segunda X Report

X report and monthly totals for your registers: backend (Next.js + Prisma), Python scraper, Android app.

## Quick start

1. **Backend** – Copy `.env.example` to `.env`. Set `DATABASE_URL` (Neon, with `&pgbouncer=true` if using pooler), `X_REPORT_SECRET`. Run `npm install`, `npx prisma db push`, `npm run dev`.
2. **Vercel** – Deploy this repo. In Vercel env: `DATABASE_URL` (same URL with `&pgbouncer=true`), `X_REPORT_SECRET`, `GITHUB_TOKEN` (for triggering scraper).
3. **GitHub Secrets** – In repo Settings → Secrets and variables → Actions → Repository secrets, add: `X_REPORT_API_URL`, `X_REPORT_SECRET`, `GITHUB_TOKEN`.
4. **Android** – Open `android/` in Android Studio. Set `xreportApiUrl` in `android/gradle.properties` to your Vercel URL. Build APK.

## How it works

- **Tap "Fetch Fresh Data"** in the app → triggers GitHub Actions workflow → scrapes Caspit Light → saves to database (~1-2 minutes)
- **Tap refresh icon** (top right) → fetches latest report from database (instant)

- **Health check:** `GET /api/xreport/health` – shows DB connection and table status.
- **Report:** `GET /api/xreport` – latest report (or 404 if none).
- **Trigger scraper:** `POST /api/trigger-scrape` – starts GitHub Actions workflow.

See **SEGUNDA_SETUP_GUIDE.md** for full setup instructions.
