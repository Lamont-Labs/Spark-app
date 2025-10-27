#!/usr/bin/env bash
# ======================================================================
# Lamont Labs — Spark "Auto Release" Replit Pipeline v4.0
#
# Objective:
#   Push-button Play Store pipeline (Replit → .AAB → Play Internal Track)
#   Handles branding, build, submission, GitHub sync, and tagging.
# ======================================================================

set -Eeuo pipefail
trap 'code=$?; echo -e "\n[❌] Build exited with code $code at line $BASH_LINENO"; exit $code' ERR

# ──────────────────────────────────────────────────────────────────────
#  SECTION 0 — UTILITIES
# ──────────────────────────────────────────────────────────────────────
ts(){ date +"%Y-%m-%d %H:%M:%S"; }
log(){ echo -e "\033[1;36m[$(ts)]\033[0m $*"; }
warn(){ echo -e "\033[1;33m[$(ts)] WARN:\033[0m $*"; }
die(){ echo -e "\033[1;31m[$(ts)] ERROR:\033[0m $*" >&2; exit 1; }
have(){ command -v "$1" >/dev/null 2>&1; }

APP_JSON="app.json"
PKG_JSON="package.json"
EAS_JSON="eas.json"
REPORT_DIR="dist"
REPORT_FILE="$REPORT_DIR/BUILD_REPORT.md"
LOG_FILE="$REPORT_DIR/build.log"

LOGO="assets/lamontlabs_logo.png"
ICON="assets/icon.png"
SPLASH="assets/splash.png"

NODE_REQUIRED_MAJOR=18
ANDROID_PACKAGE="${ANDROID_PACKAGE:-com.lamontlabs.spark}"
PLAY_TRACK="${PLAY_TRACK:-internal}"
KEYSTORE_PATH="android/app/release.keystore"

mkdir -p "$REPORT_DIR" assets
exec > >(tee -a "$LOG_FILE") 2>&1

# ──────────────────────────────────────────────────────────────────────
#  SECTION 1 — ENVIRONMENT VALIDATION
# ──────────────────────────────────────────────────────────────────────
log "== Environment check"
[ -f "$APP_JSON" ] || die "Missing $APP_JSON"
[ -f "$PKG_JSON" ] || die "Missing $PKG_JSON"
[ -f "$LOGO" ]     || die "Missing $LOGO"

have node || die "Node.js missing"
NODE_VER="$(node -v | sed 's/^v//')"; NODE_MAJOR="${NODE_VER%%.*}"
[ "$NODE_MAJOR" -ge "$NODE_REQUIRED_MAJOR" ] || die "Node >= $NODE_REQUIRED_MAJOR required (have $NODE_VER)"

if   have pnpm; then PM=pnpm
elif have npm;  then PM=npm
else die "npm or pnpm required"
fi
log "Package manager: $PM"

EXPO_CMD="npx -y expo"
EAS_CMD="npx -y eas-cli"
have expo && EXPO_CMD="expo"
have eas  && EAS_CMD="eas"
log "Expo: $EXPO_CMD"
log "EAS : $EAS_CMD"

# ──────────────────────────────────────────────────────────────────────
#  SECTION 2 — JSON HELPERS
# ──────────────────────────────────────────────────────────────────────
json_patch(){ node - "$1" "$2" <<'NODE'
const fs=require('fs'); const f=process.argv[2]; const expr=process.argv[3];
const o=JSON.parse(fs.readFileSync(f,'utf8')); const n=eval(`(o)=>(${expr})`)(o);
fs.writeFileSync(f, JSON.stringify(n,null,2));
NODE
}
json_get(){ node -e "const o=require('./$1'); console.log($2)" | tr -d '\n'; }

# ──────────────────────────────────────────────────────────────────────
#  SECTION 3 — BRAND + CONFIG VALIDATION
# ──────────────────────────────────────────────────────────────────────
[ -f "$ICON" ]   || cp "$LOGO" "$ICON"
[ -f "$SPLASH" ] || cp "$LOGO" "$SPLASH"
log "Branding confirmed"

json_patch "$APP_JSON" "
(() => {
  const e=o.expo||(o.expo={});
  e.name='Spark'; e.slug='spark'; e.scheme='spark';
  e.icon='./$ICON'; e.splash={ image:'./$SPLASH', resizeMode:'contain', backgroundColor:'#0B1020' };
  e.android=e.android||{}; e.android.package='$ANDROID_PACKAGE';
  e.android.versionCode=e.android.versionCode||12;
  e.ios=e.ios||{}; e.ios.bundleIdentifier='$ANDROID_PACKAGE';
  e.updates={ enabled:false, ...e.updates };
  e.version=e.version||'1.2.0'; return o;
})()
"

# ──────────────────────────────────────────────────────────────────────
#  SECTION 4 — VERSION BUMP (optional)
# ──────────────────────────────────────────────────────────────────────
if [ "${BUMP:-}" ]; then
  case "$BUMP" in patch|minor|major) ;; *) die "BUMP must be patch|minor|major";; esac
  log "Bumping version ($BUMP)"
  node - "$APP_JSON" "$BUMP" <<'NODE'
const fs=require('fs'); const f=process.argv[2]; const bump=process.argv[3];
const o=JSON.parse(fs.readFileSync(f,'utf8')); const e=o.expo||{};
function bumpv(v,k){let [M,m,p]=String(v||'1.2.0').split('.').map(n=>+n||0);
if(k==='major'){M++;m=0;p=0}else if(k==='minor'){m++;p=0}else{p++}return `${M}.${m}.${p}`;}
e.version=bumpv(e.version,bump); e.android.versionCode=(e.android.versionCode||12)+1;
o.expo=e; fs.writeFileSync(f, JSON.stringify(o,null,2));
NODE
fi

# ──────────────────────────────────────────────────────────────────────
#  SECTION 5 — DEPENDENCY + QUALITY
# ──────────────────────────────────────────────────────────────────────
log "Installing dependencies"
if [ "$PM" = "pnpm" ]; then
  [ -f pnpm-lock.yaml ] && pnpm install --frozen-lockfile || pnpm install
else
  [ -f package-lock.json ] && npm ci || npm install
fi
log "Running lint + typecheck"
$PM run lint || true
$PM run typecheck || true
[ -f scripts/test-determinism.cjs ] && node scripts/test-determinism.cjs || true

# ──────────────────────────────────────────────────────────────────────
#  SECTION 6 — EAS CONFIG + PREBUILD
# ──────────────────────────────────────────────────────────────────────
if [ ! -f "$EAS_JSON" ]; then
  cat > "$EAS_JSON" <<'JSON'
{
  "cli": { "version": ">= 3.16.0" },
  "build": { "production": { "android": { "buildType": "app-bundle" } } },
  "submit": { "production": { "android": { "track": "internal" } } }
}
JSON
fi
log "Running expo prebuild --clean"
$EXPO_CMD prebuild --clean || true

# ──────────────────────────────────────────────────────────────────────
#  SECTION 7 — KEYSTORE GENERATION (local fallback)
# ──────────────────────────────────────────────────────────────────────
if [ ! -f "$KEYSTORE_PATH" ]; then
  log "Generating local keystore for debug signing"
  mkdir -p android/app
  keytool -genkeypair -v -storetype PKCS12 -keystore "$KEYSTORE_PATH" \
    -alias sparkkey -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass sparkpass -keypass sparkpass \
    -dname "CN=Lamont Labs, OU=Spark, O=Lamont Labs, L=Replit, S=Internet, C=US" || true
fi

# ──────────────────────────────────────────────────────────────────────
#  SECTION 8 — BUILD PHASE
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
  log "Cloud build started"
  retry=0
  until $EAS_CMD build --platform android --profile production --non-interactive; do
    ((retry++)) || true
    [ $retry -gt 3 ] && die "EAS build failed after 3 retries"
    warn "EAS build retry $retry"
    sleep 15
  done

  LAST_ID="$($EAS_CMD build:list --status finished --limit 1 --json | node -e 'let a=\"\";process.stdin.on(\"data\",d=>a+=d);process.stdin.on(\"end\",()=>{try{const j=JSON.parse(a);console.log(j.items?.[0]?.id||\"\")}catch{}})')"
  ARTIFACT_URL="$($EAS_CMD build:view --build-id "$LAST_ID" --json | node -e 'let a=\"\";process.stdin.on(\"data\",d=>a+=d);process.stdin.on(\"end\",()=>{try{const j=JSON.parse(a);console.log(j.artifacts?.buildUrl||\"\")}catch{}})')"
  log "EAS build complete: $ARTIFACT_URL"
else
  log "Running local Gradle bundleRelease"
  pushd android >/dev/null
  chmod +x ./gradlew || true
  ./gradlew --no-daemon --stacktrace --parallel :app:bundleRelease
  popd >/dev/null
  LOCAL_AAB_PATHS="$(find android/app/build/outputs -name '*.aab' -print || true)"
  log "Local AAB(s): ${LOCAL_AAB_PATHS:-none}"
fi

# ──────────────────────────────────────────────────────────────────────
#  SECTION 9 — SUBMIT (optional)
# ──────────────────────────────────────────────────────────────────────
if [ "${SUBMIT:-0}" = "1" ]; then
  [ -n "${EAS_TOKEN:-}" ] || die "SUBMIT=1 needs EAS_TOKEN"
  [ -n "${GOOGLE_SERVICE_ACCOUNT_JSON:-}" ] || die "SUBMIT=1 needs GOOGLE_SERVICE_ACCOUNT_JSON"
  [ -f "$GOOGLE_SERVICE_ACCOUNT_JSON" ] || die "Service account JSON not found: $GOOGLE_SERVICE_ACCOUNT_JSON"
  node - "$EAS_JSON" "$PLAY_TRACK" "$GOOGLE_SERVICE_ACCOUNT_JSON" <<'NODE'
const fs=require('fs'); const f=process.argv[2]; const track=process.argv[3]; const key=process.argv[4];
const o=JSON.parse(fs.readFileSync(f,'utf8'));
o.submit=o.submit||{}; o.submit.production=o.submit.production||{};
o.submit.production.android=o.submit.production.android||{};
o.submit.production.android.track=track;
o.submit.production.android.serviceAccountKeyPath=key;
fs.writeFileSync(f, JSON.stringify(o,null,2));
NODE
  log "Submitting to Play Console ($PLAY_TRACK)"
  $EAS_CMD submit --platform android --profile production --non-interactive
fi

# ──────────────────────────────────────────────────────────────────────
#  SECTION 10 — REPORT + GIT TAG + PUSH
# ──────────────────────────────────────────────────────────────────────
PKG="$(json_get $APP_JSON 'o.expo.android.package')"
VER="$(json_get $APP_JSON 'o.expo.version')"
VRC="$(json_get $APP_JSON 'o.expo.android.versionCode')"
GIT_REMOTE="${GIT_REMOTE:-origin}"
GIT_BRANCH="${GIT_BRANCH:-main}"

cat > "$REPORT_FILE" <<EOF
# Spark Build Report
- Timestamp: $(ts)
- Package: $PKG
- Version: $VER (code $VRC)
- Build Mode: $BUILD_MODE
- EAS Artifact: ${ARTIFACT_URL:-n/a}
- Local AABs: ${LOCAL_AAB_PATHS:-n/a}
- Track: ${PLAY_TRACK:-n/a}
EOF

log "Report written to $REPORT_FILE"
git config user.name  "${GIT_AUTHOR_NAME:-Lamont Labs Bot}"
git config user.email "${GIT_AUTHOR_EMAIL:-bot@lamontlabs.dev}"
git add "$APP_JSON" "$EAS_JSON" "$REPORT_FILE" "$ICON" "$SPLASH" 2>/dev/null || true

CHANGES="$(git status --porcelain)"
if [ -n "$CHANGES" ]; then
  COMMIT_MSG="[auto-build] Spark v$VER (code $VRC)"
  git commit -m "$COMMIT_MSG"
  git pull --rebase "$GIT_REMOTE" "$GIT_BRANCH" || true
  git push "$GIT_REMOTE" "HEAD:$GIT_BRANCH"
  git tag -a "spark-v$VER" -m "Spark build $VER" || true
  git push "$GIT_REMOTE" --tags || true
  log "GitHub updated + tag spark-v$VER pushed"
else
  log "No new changes to commit"
fi

# ──────────────────────────────────────────────────────────────────────
#  SECTION 11 — SUMMARY
# ──────────────────────────────────────────────────────────────────────
log "──────────────────────────────────────────────"
log "Spark build complete!"
log "Version: $VER  (code $VRC)"
log "Mode: $BUILD_MODE"
log "EAS URL: ${ARTIFACT_URL:-n/a}"
log "Local AAB(s): ${LOCAL_AAB_PATHS:-none}"
log "Report: $REPORT_FILE"
log "──────────────────────────────────────────────"
