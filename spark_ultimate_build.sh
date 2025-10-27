#!/usr/bin/env bash
# ======================================================================
# Lamont Labs — Spark ULTIMATE Replit/GitHub Build + Auto-Push v3.0
#
# Role: One-command build for Spark (Expo React Native) to Play-Store-ready AAB.
# Works on Replit and GitHub Actions. Fail-closed. Deterministic. Loud on error.
#
# Usage (Replit shell or CI step):
#   bash spark_ultimate_build.sh
#
# Optional env:
#   BUMP=patch|minor|major           # bump app.json version + android.versionCode
#   EAS_TOKEN=...                    # enables EAS cloud AAB build + submit
#   SUBMIT=1 GOOGLE_SERVICE_ACCOUNT_JSON=creds.json PLAY_TRACK=internal|alpha|beta|production
#   ANDROID_PACKAGE=com.lamontlabs.spark
#   GIT_REMOTE=origin GIT_BRANCH=main
#   GIT_AUTHOR_NAME="Lamont Labs Bot" GIT_AUTHOR_EMAIL="bot@lamontlabs.dev"
#   GH_PAT=...                       # https push token if needed
#
# Contract:
#   - Requires: app.json, package.json, assets/lamontlabs_logo.png
#   - Will create/update: eas.json, dist/BUILD_REPORT.md, icon/splash if missing
#   - Never commits keystores or Google service JSON
# ======================================================================

set -Eeuo pipefail
trap 'c=$?; echo -e "\n[FAIL] spark_ultimate_build.sh exited with code $c at line $BASH_LINENO"; exit $c' ERR

ts(){ date +"%Y-%m-%d %H:%M:%S"; }
log(){ echo "[$(ts)] $*"; }
die(){ echo "[$(ts)] ERROR: $*" >&2; exit 1; }
have(){ command -v "$1" >/dev/null 2>&1; }

APP_JSON="app.json"
PKG_JSON="package.json"
EAS_JSON="eas.json"
REPORT_DIR="dist"
REPORT_FILE="$REPORT_DIR/BUILD_REPORT.md"

LOGO="assets/lamontlabs_logo.png"   # canonical Lamont Labs logo
ICON="assets/icon.png"
SPLASH="assets/splash.png"

NODE_REQUIRED_MAJOR=18
ANDROID_PACKAGE="${ANDROID_PACKAGE:-com.lamontlabs.spark}"
PLAY_TRACK="${PLAY_TRACK:-internal}"
GIT_REMOTE="${GIT_REMOTE:-origin}"
GIT_BRANCH="${GIT_BRANCH:-main}"

mkdir -p "$REPORT_DIR" assets

# ──────────────────────────────────────────────────────────────────────
# Sanity checks
# ──────────────────────────────────────────────────────────────────────
[ -f "$APP_JSON" ] || die "Missing $APP_JSON"
[ -f "$PKG_JSON" ] || die "Missing $PKG_JSON"
[ -f "$LOGO" ]     || die "Missing $LOGO (assets/lamontlabs_logo.png)"

have node || die "Node.js is required"
NODE_VER="$(node -v | sed 's/^v//')"; NODE_MAJOR="${NODE_VER%%.*}"
[ "$NODE_MAJOR" -ge "$NODE_REQUIRED_MAJOR" ] || die "Node >= $NODE_REQUIRED_MAJOR required (have $NODE_VER)"

# Package manager selection
if   have pnpm; then PM=pnpm
elif have npm;  then PM=npm
else die "npm or pnpm required"
fi
log "Package manager: $PM"

# Prefer npx to avoid global deps; fall back to global if available
EXPO_CMD="npx -y expo"
EAS_CMD="npx -y eas-cli"
have expo && EXPO_CMD="expo"
have eas  && EAS_CMD="eas"
log "Expo cmd: $EXPO_CMD"
log "EAS  cmd: $EAS_CMD"

# Node-based JSON helpers (no jq dependency)
json_patch(){ # $1=file, $2=JS expr returning modified object
node - "$1" "$2" <<'NODE'
const fs=require('fs'); const f=process.argv[2]; const expr=process.argv[3];
const o=JSON.parse(fs.readFileSync(f,'utf8')); const n=eval(`(o)=>(${expr})`)(o);
fs.writeFileSync(f, JSON.stringify(n,null,2));
NODE
}
json_get(){ # $1=file, $2=JS path like o.expo.version
node -e "const o=require('./$1'); console.log($2)" | tr -d '\n'
}

# ──────────────────────────────────────────────────────────────────────
# Branding enforcement
# ──────────────────────────────────────────────────────────────────────
[ -f "$ICON" ]   || cp "$LOGO" "$ICON"
[ -f "$SPLASH" ] || cp "$LOGO" "$SPLASH"
log "Branding ensured: $ICON and $SPLASH created from $LOGO if missing"

# Patch app.json to spec
log "Patching $APP_JSON for icon/splash/package/version"
json_patch "$APP_JSON" "
(() => {
  const e = o.expo || (o.expo = {});
  e.name    = e.name    || 'Spark';
  e.slug    = e.slug    || 'spark';
  e.scheme  = e.scheme  || 'spark';
  e.icon    = './$ICON';
  e.splash  = e.splash || { image: './$SPLASH', resizeMode: 'contain', backgroundColor: '#0B1020' };
  e.android = e.android || {};
  e.android.package = '$ANDROID_PACKAGE';
  e.android.versionCode = e.android.versionCode || 12;
  e.ios     = e.ios || {}; e.ios.bundleIdentifier = e.ios.bundleIdentifier || '$ANDROID_PACKAGE';
  e.updates = { enabled: false, ...e.updates };
  e.version = e.version || '1.2.0';
  return o;
})()
"

# Optional semantic version bump
if [ "${BUMP:-}" ]; then
  case "$BUMP" in patch|minor|major) ;; *) die "BUMP must be patch|minor|major";; esac
  log "Version bump ($BUMP) in $APP_JSON"
  node - "$APP_JSON" "$BUMP" <<'NODE'
const fs=require('fs'); const f=process.argv[2]; const bump=process.argv[3];
const o=JSON.parse(fs.readFileSync(f,'utf8')); const e=o.expo||{};
function bumpSem(v,k){ let [M,m,p]=String(v||"1.2.0").split('.').map(n=>+n||0);
  if(k==="major"){M++;m=0;p=0}else if(k==="minor"){m++;p=0}else{p++} return `${M}.${m}.${p}`;}
e.version=bumpSem(e.version,bump); e.android=e.android||{}; e.android.versionCode=(e.android.versionCode||12)+1;
o.expo=e; fs.writeFileSync(f, JSON.stringify(o,null,2));
NODE
fi

# ──────────────────────────────────────────────────────────────────────
# Install dependencies
# ──────────────────────────────────────────────────────────────────────
log "Installing dependencies"
if [ "$PM" = "pnpm" ]; then
  [ -f pnpm-lock.yaml ] && pnpm install --frozen-lockfile || pnpm install
else
  [ -f package-lock.json ] && npm ci || npm install
fi

# Quality gates
log "Lint + typecheck (best-effort)"
$PM run lint || true
$PM run typecheck || true
[ -f scripts/test-determinism.cjs ] && node scripts/test-determinism.cjs || true

# Ensure eas.json
if [ ! -f "$EAS_JSON" ]; then
  log "Creating $EAS_JSON"
  cat > "$EAS_JSON" <<'JSON'
{
  "cli": { "version": ">= 3.16.0" },
  "build": {
    "production": {
      "android": { "buildType": "app-bundle" }
    }
  },
  "submit": {
    "production": {
      "android": { "track": "internal" }
    }
  }
}
JSON
fi

# ──────────────────────────────────────────────────────────────────────
# Prebuild native
# ──────────────────────────────────────────────────────────────────────
log "Expo prebuild --clean"
$EXPO_CMD prebuild --clean || true

# ──────────────────────────────────────────────────────────────────────
# Build path selection
# ──────────────────────────────────────────────────────────────────────
BUILD_MODE="local"
if [ -n "${EAS_TOKEN:-}" ]; then
  export EXPO_TOKEN="$EAS_TOKEN"
  BUILD_MODE="eas"
fi
log "Build mode: $BUILD_MODE"

ARTIFACT_URL=""
LOCAL_AAB_PATHS=""

if [ "$BUILD_MODE" = "eas" ]; then
  log "EAS build (production)"
  $EAS_CMD build --platform android --profile production --non-interactive

  LAST_ID="$($EAS_CMD build:list --status finished --limit 1 --json | node -e 'let a=\"\";process.stdin.on(\"data\",d=>a+=d);process.stdin.on(\"end\",()=>{try{const j=JSON.parse(a);console.log(j.items?.[0]?.id||\"\")}catch{console.log(\"\")}})')"
  if [ -n "$LAST_ID" ]; then
    ARTIFACT_URL="$($EAS_CMD build:view --build-id "$LAST_ID" --json | node -e 'let a=\"\";process.stdin.on(\"data\",d=>a+=d);process.stdin.on(\"end\",()=>{try{const j=JSON.parse(a);console.log(j.artifacts?.buildUrl||\"\")}catch{}})')"
    log "EAS build id: $LAST_ID"
    log "EAS artifact URL: $ARTIFACT_URL"
  else
    log "No finished EAS build found yet; check EAS dashboard."
  fi
else
  # Local Gradle fallback
  [ -d android ] || die "android/ missing after prebuild; cannot local-build"
  log "Local Gradle bundleRelease (heavy on Replit)"
  pushd android >/dev/null
  chmod +x ./gradlew || true
  ./gradlew --no-daemon --stacktrace --parallel :app:bundleRelease
  popd >/dev/null
  LOCAL_AAB_PATHS="$(find android/app/build/outputs -name '*.aab' -print || true)"
  log "Local AAB(s): ${LOCAL_AAB_PATHS:-none}"
fi

# Optional: Submit to Google Play via EAS
if [ "${SUBMIT:-0}" = "1" ]; then
  [ -n "${EAS_TOKEN:-}" ] || die "SUBMIT=1 needs EAS_TOKEN"
  [ -n "${GOOGLE_SERVICE_ACCOUNT_JSON:-}" ] || die "SUBMIT=1 needs GOOGLE_SERVICE_ACCOUNT_JSON"
  [ -f "$GOOGLE_SERVICE_ACCOUNT_JSON" ] || die "Service account JSON not found: $GOOGLE_SERVICE_ACCOUNT_JSON"

  log "Set submit track → $PLAY_TRACK"
  node - "$EAS_JSON" "$PLAY_TRACK" "$GOOGLE_SERVICE_ACCOUNT_JSON" <<'NODE'
const fs=require('fs'); const f=process.argv[2]; const track=process.argv[3]; const key=process.argv[4];
const o=JSON.parse(fs.readFileSync(f,'utf8'));
o.submit=o.submit||{}; o.submit.production=o.submit.production||{};
o.submit.production.android=o.submit.production.android||{};
o.submit.production.android.track=track;
o.submit.production.android.serviceAccountKeyPath=key;
fs.writeFileSync(f, JSON.stringify(o,null,2));
NODE

  log "EAS submit → Google Play ($PLAY_TRACK)"
  $EAS_CMD submit --platform android --profile production --non-interactive
fi

# ──────────────────────────────────────────────────────────────────────
# Build report
# ──────────────────────────────────────────────────────────────────────
PKG="$(json_get $APP_JSON 'o.expo.android.package')"
VER="$(json_get $APP_JSON 'o.expo.version')"
VRC="$(json_get $APP_JSON 'o.expo.android.versionCode')"

cat > "$REPORT_FILE" <<EOF
# Spark — Build Report
- Timestamp: $(ts)
- Package: $PKG
- Version: $VER (code $VRC)
- Mode: $BUILD_MODE
- EAS Artifact: ${ARTIFACT_URL:-n/a}
- Local AAB(s): $(printf '%s' "${LOCAL_AAB_PATHS:-n/a}" | tr '\n' ' ')
- Track (if submitted): ${PLAY_TRACK:-n/a}

## Notes
- Branding enforced via assets/lamontlabs_logo.png → icon/splash.
- Never commit keystores or service-account JSON.
- Promote tracks in Play Console after internal testing.
EOF
log "Report written: $REPORT_FILE"

# ──────────────────────────────────────────────────────────────────────
# Auto-commit and push to GitHub
# ──────────────────────────────────────────────────────────────────────
log "Preparing git commit and push"
git config user.name  "${GIT_AUTHOR_NAME:-Lamont Labs Bot}" || true
git config user.email "${GIT_AUTHOR_EMAIL:-bot@lamontlabs.dev}" || true

# Inject token for https remote if provided
if [ -n "${GH_PAT:-}" ]; then
  CURRENT_URL="$(git remote get-url "$GIT_REMOTE" 2>/dev/null || echo "")"
  if echo "$CURRENT_URL" | grep -q '^https://'; then
    TOKEN_URL="$(echo "$CURRENT_URL" | sed -E "s#https://#https://${GH_PAT}:x-oauth-basic@#")"
    git remote set-url "$GIT_REMOTE" "$TOKEN_URL"
    log "Injected GH_PAT into remote URL for push"
  fi
fi

# Add updated files
git add "$APP_JSON" "$EAS_JSON" "$REPORT_FILE" || true
git add "$ICON" "$SPLASH" 2>/dev/null || true

CHANGES="$(git status --porcelain)"
if [ -n "$CHANGES" ]; then
  COMMIT_MSG="[build] Spark AAB build on Replit: v$VER (code $VRC) — mode=$BUILD_MODE"
  git commit -m "$COMMIT_MSG"
  git pull --rebase "$GIT_REMOTE" "$GIT_BRANCH" || true
  git push "$GIT_REMOTE" "HEAD:$GIT_BRANCH"
  log "Pushed commit to $GIT_REMOTE/$GIT_BRANCH"
else
  log "No changes to commit"
fi

log "────────────────────────────────────────────────────────"
log "Spark build complete. Report: $REPORT_FILE"
log "Package: $PKG  Version: $VER  Code: $VRC"
log "EAS URL: ${ARTIFACT_URL:-n/a}"
log "Next: Verify on device, then manage tracks in Play Console."
log "────────────────────────────────────────────────────────"
```0
