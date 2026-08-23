#!/usr/bin/env bash
#
# Launch-and-verify smoke test, run against a real emulator in CI.
#
# Compiling successfully proves nothing about whether the app starts — the
# first published debug build compiled cleanly, passed every unit test, and
# still crashed on launch. This script reproduces exactly what a user does
# (install, tap the icon) and fails the build if the app dies or never
# reaches the foreground, so a crashing APK can never be published.
set -euo pipefail

PACKAGE="com.trucdecomptable.cuissonvapeur"
ACTIVITY="${PACKAGE}/.MainActivity"
SETTLE_SECONDS=10

APK=$(ls app/build/outputs/apk/debug/*.apk | head -1)
echo "==> Installing ${APK}"
# -g pre-grants runtime permissions so the test exercises the app itself,
# not the permission dialogs.
adb install -r -g "${APK}"

echo "==> Clearing logcat and launching ${ACTIVITY}"
adb logcat -c
adb shell am start -W -n "${ACTIVITY}"

echo "==> Waiting ${SETTLE_SECONDS}s for the app to settle"
sleep "${SETTLE_SECONDS}"

adb logcat -d > logcat.txt

fail() {
  echo ""
  echo "=================== SMOKE TEST FAILED ==================="
  echo "$1"
  echo ""
  echo "--- Crash / error output from logcat -------------------"
  # Every pipeline here is `|| true`-guarded: under `set -o pipefail`, head
  # closing the pipe early (SIGPIPE) or grep matching nothing would abort
  # this function before it printed anything useful.
  if [ -n "${CRASH_BLOCK:-}" ]; then
    echo "${CRASH_BLOCK}" | head -80 || true
  else
    grep -iE "${PACKAGE}|AndroidRuntime|ActivityManager" logcat.txt | tail -60 || true
  fi
  echo "========================================================"
  exit 1
}

# Only our own crashes count: the emulator's stock apps (GMS, dialer, ...)
# throw their own exceptions during boot and must not fail this test.
CRASH_BLOCK=$(awk "/FATAL EXCEPTION/{flag=1} flag" logcat.txt || true)
if echo "${CRASH_BLOCK}" | grep -q "Process: ${PACKAGE}"; then
  fail "The app crashed after launch (FATAL EXCEPTION in ${PACKAGE})."
fi

if grep -qE "Force finishing activity ${PACKAGE}|ANR in ${PACKAGE}" logcat.txt; then
  fail "The app was force-finished or hit an ANR after launch."
fi

# A crash isn't always logged as FATAL EXCEPTION (e.g. a native abort, or a
# process killed during startup), so also assert the app really is the
# focused window — the only proof it actually reached a usable state.
echo "==> Verifying the app is in the foreground"
FOCUS=$(adb shell dumpsys window 2>/dev/null | grep -E "mCurrentFocus|mFocusedApp" || true)
echo "${FOCUS}"
if ! echo "${FOCUS}" | grep -q "${PACKAGE}"; then
  fail "The app is not the focused window after launch — it never started or died silently."
fi

# And that its process is genuinely alive rather than restarting in a loop.
echo "==> Verifying the app process is running"
if ! adb shell pidof "${PACKAGE}" > /dev/null 2>&1; then
  fail "No running process for ${PACKAGE} — the app died after launch."
fi

echo ""
echo "==> SMOKE TEST PASSED: ${PACKAGE} launched and is in the foreground."
