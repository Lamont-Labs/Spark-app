# Spark™ — Play Store Offline Submission Checklist

## Build Settings
- ✅ `app.json` → `"updates": { "enabled": false }`
- ✅ Config plugin removes `android.permission.INTERNET`
- ✅ Network Security Config set to deny all traffic
- ✅ Runtime guards (`src/lib/no-net.ts`) block `fetch`, XHR, WebSocket in production
- ✅ `eas.json` has `production` profile generating AAB

## Produce AAB
```bash
npm install
npm run build:aab
# Output: .aab available in EAS build page or local artifact (depending on account)
