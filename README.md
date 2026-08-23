# Cuisson vapeur des légumes — Android

Portage Android (Kotlin + Jetpack Compose) de l'outil web « Guide de cuisson
vapeur des légumes » (trucdecomptable.fr), à partir du cahier des charges
[`docs/cahier-des-charges.md`](docs/cahier-des-charges.md). Ce document est la
source d'autorité pour toutes les exigences (EF-01..EF-30, tableau des
exigences non fonctionnelles §4, arborescence UX §5, clarifications
techniques §12) ; ce README documente comment le code s'y conforme et où il a
fallu trancher des points que le cahier des charges laisse ouverts.

## Structure du projet

Le repo est un build Gradle **multi-module**, découpé précisément pour que la
logique la plus critique du projet — le calcul du plan de cuisson optimisé
(EF-16) et les objectifs nutritionnels (EF-14) — reste **testable avec un
simple JDK + Gradle, sans SDK Android** :

```
cuisson-vapeur-legumes/
├── domain/     module Kotlin/JVM pur (plugin kotlin("jvm") uniquement,
│               zéro dépendance Android) : modèle Vegetable, catalogue des
│               28 légumes, CookingPlanCalculator (EF-16),
│               NutritionGoalsCalculator (EF-14 Option A).
│               → compile et teste avec `gradle`, aucun SDK requis.
│
└── app/        module Android (Compose, Room, Hilt, AlarmManager,
                notifications, BroadcastReceivers). Dépend de :domain pour
                le modèle/catalogue/algorithme.
                → nécessite un SDK Android + Android Studio pour compiler ;
                NON compilé/vérifié dans ce sandbox (voir plus bas).
```

Pourquoi ce découpage : `:domain` ne dépend d'aucune classe `android.*`, donc
`gradle :domain:test` peut tourner n'importe où avec juste un JDK — y
compris dans cet environnement d'agent qui n'a pas de SDK Android installé.
C'est ce module que vise la ligne du tableau NFR §4 : « Unitaires ≥ 80% sur
le module calcul plan optimisé ».

`gradle.properties` active `org.gradle.configureondemand=true`, ce qui
permet à `gradle :domain:test` de ne configurer **que** `:domain` (module
Kotlin/JVM pur) sans jamais évaluer `:app` (qui applique l'Android Gradle
Plugin et échouerait sans SDK installé). C'est ce qui rend le projet
buildable dans ce sandbox malgré la présence du module Android dans le même
`settings.gradle.kts`. Vérifié empiriquement : `gradle :domain:test` réussit
même avec `:app` présent dans le build.

## Vérifier le module `:domain` (n'importe quel environnement JDK + Gradle)

```bash
gradle :domain:test
```

**Résultat obtenu dans ce sandbox (JDK 21, Gradle 8.14.3) : 21 tests, 21
réussis, 0 échec, 0 erreur.**

```
VegetableCatalogTest        6 tests — 28 légumes, comptage des catégories
                             (antioxydants 8 / fibres 7 / vitamineC 2 /
                             protéines 1 / hydratation 2, cf. EF-14 D1),
                             valeurs des lignes de la table §6.
CookingPlanCalculatorTest   8 tests — légume seul, plusieurs légumes,
                             égalités de durée, T1/T2/T6 (§7), sélection
                             vide (IllegalArgumentException).
NutritionGoalsCalculatorTest 7 tests — cibles vides, T6 (8 antioxydants),
                             cible presque atteinte, catégorie à 1 légume,
                             sélection complète, doublons, ordre d'affichage.
```

Les tests couvrent explicitement les scénarios T1, T2 et T6 de la section 7
du cahier des charges, ainsi que les cas limites mentionnés dans la mission
(légume seul, légumes multiples à durées distinctes, égalités de durée,
sélection vide).

## Ouvrir / builder le projet complet (`:app`) dans Android Studio

1. Ouvrir le dossier racine du repo dans Android Studio (Koala/Ladybug ou plus
   récent recommandé — compileSdk/targetSdk 35).
2. Android Studio doit disposer d'un SDK Android installé (API 35) ; il
   proposera de l'installer automatiquement si absent.
3. Laisser Android Studio générer le wrapper Gradle (`gradlew`/`gradlew.bat`)
   à l'ouverture du projet — il n'a **pas** été généré dans ce sandbox (voir
   « Limitations connues » ci-dessous). Alternative en ligne de commande :
   `gradle wrapper` une fois, à la racine, dans un environnement avec accès
   réseau complet.
4. Build → Make Project, ou `./gradlew :app:assembleDebug` une fois le
   wrapper généré.

## Permissions (manifest `app/src/main/AndroidManifest.xml`)

| Permission | Justification |
|---|---|
| `SCHEDULE_EXACT_ALARM` | Programmer l'alarme de fin de cuisson et les rappels par étape à l'heure exacte (Android 12+), via `AlarmManager.setExactAndAllowWhileIdle` (EF-16/EF-19/EF-22). |
| `WAKE_LOCK` | Réveiller brièvement le CPU pour afficher/déclencher l'alarme de fin de façon fiable (EF-23). |
| `POST_NOTIFICATIONS` | Notification persistante de cuisson (EF-18/EF-20) et notification plein écran d'alarme (EF-23), requis explicitement à partir d'Android 13. |
| `VIBRATE` | Vibration des rappels d'étape (EF-19) et de l'alarme de fin (EF-23). |
| `RECEIVE_BOOT_COMPLETED` | **Absente du tableau NFR §4 d'origine** — nécessaire pour `BootReceiver`, qui ré-arme les alarmes après un redémarrage du téléphone à partir du timestamp de fin absolu persisté, conformément à l'exigence de reconstruction d'état du §12.2. Ajoutée en connaissance de cause ; à valider avec le tableau des permissions livré au client (§10 du cahier des charges). |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` | Requis par `CookingTimerService`, le service de fond minimal Android 12+ mentionné au §4 (« service de fond minimal sur Android 12+ »), qui maintient la notification persistante à jour. |
| `USE_FULL_SCREEN_INTENT` | Afficher `AlarmActivity` par-dessus l'écran verrouillé (EF-23 : « réveil d'écran »). |

## Conformité au §12 (clarifications techniques obligatoires)

- **Base de temps `ELAPSED_REALTIME`, pas `RTC`** : `AlarmScheduler`
  (`app/.../alarm/AlarmScheduler.kt`) programme systématiquement les alarmes
  via `AlarmManager.setExactAndAllowWhileIdle(ELAPSED_REALTIME_WAKEUP, ...)`,
  en convertissant à chaque appel le timestamp absolu persisté en délai
  `SystemClock.elapsedRealtime() + delta` — jamais d'alarme programmée
  directement sur une horloge murale.
- **Pause/reprise/prolongation annulent et replanifient TOUTES les
  alarmes** : `CookingSessionRepository.pause()`, `.resume()` et `.extend()`
  (`app/.../data/repository/CookingSessionRepository.kt`) appellent chacune
  `AlarmScheduler.cancelAll()` puis reprogramment l'alarme de fin **et**
  chaque rappel d'étape restant à partir du plan recalculé — jamais de
  décalage d'une alarme existante.
- **Timestamp de fin absolu persisté, pas une durée restante** :
  `CookingSessionEntity.endEpochMillis` (Room, table `cooking_session`) est
  un epoch millis absolu pendant que la cuisson tourne ; `BootReceiver` /
  `CookingSessionRepository.rearmAfterReboot()` l'utilisent pour reconstruire
  l'état et rearmer les alarmes après un kill process ou un redémarrage.

## Décisions non pinned par le cahier des charges (jugements pris pour cette implémentation)

Le cahier des charges est très précis sur *quoi* faire, mais laisse plusieurs
détails de mise en œuvre ouverts. Voici les choix faits, à valider par
Fabrice Heuvrard / le chef de produit si besoin :

- **Catalogue embarqué en Kotlin compilé plutôt qu'en JSON d'assets +
  Room** : le tableau NFR §4 mentionne « catalogue embarqué dans les assets,
  consulté depuis la BDD », mais la consigne de cette tâche demande
  explicitement un module `:domain` Kotlin/JVM pur portant le catalogue en
  dur, pour qu'il soit testable unitairement sans Android. Room n'est donc
  utilisé côté `:app` que pour les favoris, le panier, les réglages et la
  session de cuisson active — pas pour le catalogue lui-même.
- **Identifiants (`Vegetable.id`) et emojis** : absents du cahier des
  charges (qui ne donne que les noms d'affichage). Des slugs ascii stables
  ont été inventés (`"courgettes"`, `"chou_fleur"`, …) pour servir de clés
  Room/navigation, et un emoji a été choisi pour chaque légume — l'Unicode
  standard n'ayant pas d'emoji dédié pour plusieurs d'entre eux (navet,
  panais, poireau, artichaut, chou-fleur…), certains réutilisent un emoji
  approximatif ou générique (voir les commentaires dans
  `domain/.../catalog/VegetableCatalog.kt`).
- **« Chou kale » (Annexe A, ligne 17)** : la cellule « Vitamine A, C, K » a
  été interprétée comme **un seul** bienfait (une phrase listant 3
  vitamines), pas comme 3 bienfaits séparés — par cohérence avec le fait que
  toutes les autres lignes n'ont que 2 bienfaits séparés par une seule
  virgule.
- **Filtre saison (EF-04)** : implémenté comme un choix simple (single-select,
  5 pastilles : 4 saisons + « Toute l'année »), togglable, plutôt qu'un
  multi-select — le cahier des charges ne précise pas ce comportement.
- **Écran Réglages hors barre de navigation basse** : la barre du bas ne
  porte que 5 destinations (Accueil, Catalogue, Favoris, Objectifs, Conseils)
  — Réglages est accessible via une icône dans la barre du haut d'Accueil,
  Material Design recommandant de ne pas dépasser ~5 items en barre basse.
- **Vocabulaire « panier »** : le cahier des charges signale lui-même
  (§12.1) l'ambiguïté entre le panier de sélection (EF-06/08) et l'action
  « AJOUTER MAINTENANT » pendant la cuisson (EF-18), mais la présente cette
  observation comme une *proposition* à trancher, pas une exigence — non
  résolue ici, conformément au périmètre de la tâche (gouvernance/portée
  hors sujet).
- **État dégradé si `SCHEDULE_EXACT_ALARM` refusée (§12.1)** : également
  laissé comme point ouvert par le cahier des charges lui-même (« à
  trancher avant J1 »). `AlarmScheduler` expose `canScheduleExactAlarms()`
  et retombe silencieusement sur `AlarmManager.set()` (alarme non-exacte) en
  cas de `SecurityException` plutôt que de bloquer le démarrage de la
  cuisson — mais aucun bandeau d'avertissement UI dédié n'a été construit.
- **Accessibilité des objectifs nutritionnels (§12.1)** : implémentée par
  anticipation malgré son statut de « proposition » — l'état atteint/non
  atteint utilise une icône **et** un texte, jamais la couleur seule.
- **EF-28 (langue)** : le réglage de langue est persisté (Room), et les
  ressources `values-en/strings.xml` existent, mais le changement de langue
  **in-app** (indépendant de la langue système) n'est pas câblé au runtime
  (pas d'appel à `AppCompatDelegate.setApplicationLocales` / recréation
  d'activité) — seul le suivi de la langue système fonctionne nativement via
  les ressources `values-en`.

## Limitations connues / non vérifié dans ce sandbox

- **Aucune vérification de build/exécution du module `:app`** : ce sandbox
  ne dispose d'aucun SDK Android (pas d'`ANDROID_HOME`, pas d'émulateur). Tout
  le code Kotlin/XML sous `app/src/` a été écrit avec soin (imports et
  signatures d'API vérifiés par lecture de la documentation AGP ~8.5 /
  Compose BOM 2024.09 / Hilt ~2.51 / Room ~2.6), mais **n'a pas compilé** ici
  — à valider dans Android Studio avec un SDK réel avant toute mise en
  production.
- **Wrapper Gradle non généré** : `gradlew`/`gradlew.bat` sont absents ;
  Android Studio les génère automatiquement à l'ouverture du projet, ou
  lancer `gradle wrapper` une fois dans un environnement réseau complet.
- **Icônes/assets placeholders** : l'icône de lancement (adaptive icon) et
  l'icône de notification sont des vector drawables minimalistes maison, pas
  un vrai design system — à remplacer avant livraison (cf. livrable « Kit UI
  Figma » du §10 du cahier des charges, hors périmètre de cette tâche).
- **Traductions EN de premier jet** : `values-en/strings.xml` est une
  traduction automatique non relue par un locuteur natif — le cahier des
  charges lui-même laisse la gouvernance des langues comme point ouvert
  (D4/§12.1), explicitement hors périmètre de cette tâche.
- **Aucun fichier son d'alarme fourni** : les 3 profils de son (EF-26/D3 :
  « bip / carillon / longue note ») pointent chacun vers une sonnerie
  système différente (`RingtoneManager.TYPE_NOTIFICATION` /
  `TYPE_ALARM` / `TYPE_RINGTONE`) plutôt que vers un asset audio dédié —
  aucun fichier son n'a été fourni avec la spec ; des sons réels devront être
  ajoutés en assets `raw/` avant livraison, per D3.
- **Pas de tests unitaires/instrumentés sur `:app`** : seul `:domain` est
  couvert par des tests exécutables dans cet environnement. Le plan de test
  NFR §4 (« tests UI smoke des 5 écrans », « test de non-régression du timer
  en arrière-plan ») nécessite un device/émulateur et n'a pas pu être
  exécuté ici.
- **Taille APK / consommation batterie non mesurées** (NFR §4 : < 15 Mo, <
  2 %/8 h) — nécessitent un build réel + profiling sur device.

## Annexe A exportée en JSON (livrable §10.6)

Non générée dans cette passe (hors périmètre explicite de la tâche) ; le
catalogue canonique est `domain/src/main/kotlin/.../catalog/VegetableCatalog.kt`
et peut être sérialisé en JSON facilement si besoin plus tard.
