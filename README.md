# Segunda X Report

X report and monthly totals for your registers: backend (Next.js + Prisma), Python scraper, Android app.

## Quick start

1. **Backend** – Copy `.env.example` to `.env`. Set `DATABASE_URL` (Neon, with `&pgbouncer=true` if using pooler), `X_REPORT_SECRET`. Run `npm install`, `npx prisma db push`, `npm run dev`.
2. **Vercel** – Deploy this repo. In Vercel env: `DATABASE_URL` (same URL with `&pgbouncer=true`), `X_REPORT_SECRET`.
3. **Android** – Open `android/` in Android Studio. Set `xreportApiUrl` in `android/gradle.properties` to your Vercel URL. Build APK.
4. **Script** – In `.env` set `X_REPORT_API_URL` (Vercel URL), `X_REPORT_SECRET` (same as Vercel). Run `python3 scripts/caspit_xreport.py` so the app has data.

- **Health check:** `GET /api/xreport/health` – shows DB connection and table status.
- **Report:** `GET /api/xreport` – latest report (or 404 if none).

See **SEGUNDA_SETUP_GUIDE.md** for full steps (GitHub, Neon, Vercel, APK).
