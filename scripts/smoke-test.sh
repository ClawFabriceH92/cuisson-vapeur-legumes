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

# ---------------------------------------------------------------- onglet Conserver
#
# Le smoke test ci-dessus ne prouve que l'accueil. L'écran « Conserver »
# (v1.13) est le seul qui rende des listes longues construites depuis un
# catalogue Kotlin : on l'ouvre pour de vrai, on vérifie que les deux familles
# de dates sont à l'écran, et on laisse une capture en artefact.

# Touche l'élément dont le texte ou la description contient $1.
#
# Le dump contient des nœuds parents dont la description agrège tout le texte
# de l'écran : un simple « premier nœud qui contient le libellé » tape donc au
# milieu de la page (vécu 01/11/2026, tap 540,1200 au lieu de la barre du bas).
# On garde donc le nœud de PLUS PETITE surface parmi les correspondances.
tap_label() {
  local cible="$1"
  adb shell uiautomator dump /sdcard/ui.xml > /dev/null 2>&1 || return 1
  adb shell cat /sdcard/ui.xml > /tmp/ui.xml || return 1

  local choisi
  choisi=$(awk -v cible="${cible}" '
    {
      ligne = $0
      if (match(ligne, /bounds="\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]"/) == 0) next
      b = substr(ligne, RSTART, RLENGTH)
      gsub(/[^0-9]/, " ", b)
      split(b, a, " ")
      x1 = a[1]; y1 = a[2]; x2 = a[3]; y2 = a[4]
      surface = (x2 - x1) * (y2 - y1)
      exact  = (index(ligne, "text=\"" cible "\"") > 0 || index(ligne, "content-desc=\"" cible "\"") > 0)
      proche = (index(tolower(ligne), tolower(cible)) > 0)
      if (!exact && !proche) next
      # surface en tête du tri : une correspondance exacte passe avant tout
      printf "%d %s %d %d\n", surface + (exact ? 0 : 1000000000), (exact ? "E" : "P"), int((x1 + x2) / 2), int((y1 + y2) / 2)
    }' /tmp/ui.xml | sort -n | head -1)

  [ -n "${choisi}" ] || return 1
  adb shell input tap "$(echo "${choisi}" | awk '{print $3}')" "$(echo "${choisi}" | awk '{print $4}')"
  echo "    tap ${cible} -> $(echo "${choisi}" | awk '{print $2" "$3","$4}')"
}

# Filet de sécurité : la barre du bas a N onglets de largeur égale ; « Conserver »
# est le dernier. Utilisé seulement si le dump n'a pas su le désigner.
tap_dernier_onglet() {
  local taille largeur hauteur densite
  taille=$(adb shell wm size | grep -o '[0-9]\+x[0-9]\+' | tail -1)
  largeur=${taille%x*}; hauteur=${taille#*x}
  densite=$(adb shell wm density | grep -o '[0-9]\+' | head -1)
  local marge=$(( 48 * densite / 160 ))
  echo "    tap de secours -> $(( largeur * 11 / 12 )),$(( hauteur - marge ))"
  adb shell input tap $(( largeur * 11 / 12 )) $(( hauteur - marge ))
}

echo "==> Ouverture de l'onglet Conserver"
if ! tap_label "Conserver"; then
  echo "    libellé non trouvé dans le dump, tap calculé sur la barre du bas"
  tap_dernier_onglet
fi
sleep 4
adb logcat -d > logcat-conservation.txt

ECRAN=$(adb shell uiautomator dump /sdcard/ui2.xml > /dev/null 2>&1; adb shell cat /sdcard/ui2.xml || true)
# Les deux repères de l'en-tête : ils prouvent que l'écran est bien celui-là et
# que les deux familles sont présentées séparément.
for attendu in "consommer jusqu" "DDM"; do
  if ! echo "${ECRAN}" | grep -qi "${attendu}"; then
    CRASH_BLOCK=$(grep -A40 "FATAL EXCEPTION" logcat-conservation.txt || true)
    fail "L'écran Conserver ne montre pas « ${attendu} » : la liste des dates n'est pas rendue."
  fi
done

# Capture du haut de l'écran avant de défiler (c'est ce que voit l'utilisateur).
adb exec-out screencap -p > screenshot-conservation.png || true

# Un défilement, puis un produit fragile doit apparaître : la liste est rendue
# par un LazyColumn, donc seuls les éléments visibles sont dans le dump.
adb shell input swipe 540 1800 540 700 300 || true
sleep 2
ECRAN2=$(adb shell uiautomator dump /sdcard/ui3.xml > /dev/null 2>&1; adb shell cat /sdcard/ui3.xml || true)
if ! echo "${ECRAN2}" | grep -qi "œuf\|oeuf"; then
  CRASH_BLOCK=$(grep -A40 "FATAL EXCEPTION" logcat-conservation.txt || true)
  fail "Après défilement, les produits de la famille DLC ne sont pas affichés."
fi

if grep -q "FATAL EXCEPTION" logcat-conservation.txt; then
  CRASH_BLOCK=$(grep -A40 "FATAL EXCEPTION" logcat-conservation.txt || true)
  fail "Un crash est survenu en ouvrant l'écran Conserver."
fi

echo "==> SMOKE TEST COMPLET: accueil OK, écran Conserver rendu, produits DLC listés."
