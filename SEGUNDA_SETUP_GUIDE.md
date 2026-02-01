# Segunda X Report – Setup Guide

## 1. Create `.env` in Segunda folder

Copy `.env.example` to `.env`. Set:

- **DATABASE_URL** – Your Neon connection string. If the host has **-pooler**, add **`&pgbouncer=true`** at the end.
- **X_REPORT_SECRET** – Any long random string (e.g. `openssl rand -base64 32`). You’ll use the same value in Vercel.
- **X_REPORT_API_URL** – Your Vercel URL (e.g. `https://segunda-xxx.vercel.app`), no trailing slash.

## 2. Push to GitHub and deploy on Vercel

- Create a repo (e.g. `segunda`), push this folder.
- In Vercel: Import the repo, deploy.
- In Vercel **Settings → Environment Variables** add:
  - **DATABASE_URL** – Same Neon URL as in `.env` (with `&pgbouncer=true` if pooler).
  - **X_REPORT_SECRET** – Same value as in `.env`.
- Redeploy.

## 3. Create the database table

From the Segunda folder (with `.env` set):

```bash
cd /Users/adamrab/Segunda
npm install
npx prisma db push
```

## 4. Check health

Open in browser: **https://your-vercel-url.vercel.app/api/xreport/health**

You should see `{"ok":true,"message":"Database connected","tableExists":true,"reportCount":0}`. If you see an error, use the `error` and `hint` in the response to fix (e.g. run `prisma db push`, fix DATABASE_URL).

## 5. Run the Python script once

```bash
cd /Users/adamrab/Segunda
pip install -r requirements.txt
python3 scripts/caspit_xreport.py
```

Then open **https://your-vercel-url.vercel.app/api/xreport** – you should see the report JSON.

## 6. Android app

- Open **Segunda/android** in Android Studio.
- In **android/gradle.properties** set **xreportApiUrl** to your Vercel URL (no trailing slash).
- Build → Build APK(s). Install the APK on your phone and tap Refresh.

## 7. Add/remove registers

- **Hide/show** – In the app: gear icon → Registers → toggle each register.
- **Add a register** – Add it to **ACCOUNTS** in `scripts/caspit_xreport.py`, run the script, then Refresh in the app.
