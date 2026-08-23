# Cahier des charges — Application Android « Cuisson vapeur des légumes »

> Portage mobile de l'outil gratuit « Guide de cuisson vapeur des légumes » du site **Truc de Comptable** (`trucdecomptable.fr/GRATUIT/cuisson.html`, Fabrice Heuvrard).
>
> **Version** : 1.0 — 23/08/2026
> **Différence clé avec la v1 de ce document** : rédigé à partir de l'analyse effective du code de la page web existante (catalogue de **28 légumes**, panier multi-légumes, objectifs nutritionnels, plan de cuisson optimisé), et non d'une hypothèse de « minuteur simple ».

---

## 1. Contexte et objectifs

### 1.1 Contexte
L'outil web existant aide l'utilisateur à cuire plusieurs légumes à la vapeur **en même temps** en calculant l'ordre d'ajout (le plus long en premier, chacun décalé de sa durée de cuisson), si bien que **tous les légumes sont prêts en même temps**. Il intègre aussi une dimension gamification : des « objectifs nutritionnels » (antioxydants, fibres, vitamine C…) qui se remplissent au fil de la sélection.

Limites de la version web constatées lors de l'analyse du code :

- le décompte repose sur `setInterval` : **il s'arrête si l'onglet perd le focus ou si le téléphone est verrouillé** ;
- le son de fin est un simple « bip » de 0,5 s, **sans vibration ni réveil d'écran** ;
- les cibles nutritionnelles sont **inatteignables** (ex. « Fibres 0/10 » alors que 7 légumes du catalogue sont taggés fibres ; « Vitamine C 0/6 » pour 2 légumes) ;
- la sélection est sauvegardée en `localStorage` mais **le rechargement est commenté** (aucune reprise de session).

### 1.2 Objectif
Développer une application Android qui :
1. **reproduit fidèlement les règles du site** (catalogue, calcul du plan optimisé, objectifs, favoris) ;
2. **corrige les limites web** : timer fiable en arrière-plan, alarme de fin indiscutable, cibles nutritionnelles cohérentes, reprise de session ;
3. fonctionne **100 % hors ligne, sans compte, sans collecte de données**.

### 1.3 Périmètre v1

| Incluant | Exclu (post-v1) |
|---|---|
| Catalogue des 28 légumes du site | iOS |
| Sélection multi-légumes + favoris | Comptes utilisateurs / cloud |
| Objectifs nutritionnels (cibles corrigées) | Ajout de légumes personnalisés |
| Plan de cuisson optimisé + modal de confirmation | Historique des cuissons / recettes |
| Timer total pause/reprise/arrêt, fiable en arrière-plan | Notifications push, analytics, pub |
| Alarme de fin : son + vibration + réveil écran (optionnel, non inclus) |
| Écran « Conseils » |

---

## 2. Cibles utilisateurs

| Persona | Besoin |
|---|---|
| Cuisinier occasionnel | Ne pas surcuire l'un des légumes du panier, savoir **quand ajouter** chacun |
| Cuisinier familial | Garder ses légumes favoris à portée de main |
| Utilisateur pressé | Panier + démarrage en ≤ 3 gestes depuis l'accueil |

---

## 3. Exigences functionnelles

### 3.1 Catalogue
- **EF-01** : Catalogue initial de **28 légumes** repris **à l'identique** du site : nom, plage affichée (`time`), durée moteur (`duration`, borne haute de la plage), bienfaits (`benefits`), catégorie nutritionnelle (`categories`), emoji, saison, kcal/100g. *(Annexe A)*
- **EF-02** : Recherche texte sur **nom et bienfaits** (comportement web actuel).
- **EF-03** : Tri par **temps croissant** ou **nom alphabétique** (2 boutons, état actif visible).
- **EF-04** : Filtre saison (badges colorés du site : Printemps, Été, Automne, Hiver, saison mixtes, « Toute l'année ») — *nouveau vs web*.
- **EF-05** : Fiche détail d'un légume au tap : temps, saison, calories, bienfaits, catégorie, icône.

### 3.2 Panier (sélection multi-légumes)
- **EF-06** : Sélection / dé-sélection en 1 tap, indication visuelle claire (bordure + coche, comme le web).
- **EF-07** : Compteur « N légumes dans le panier » + liste des nommés cochés.
- **EF-08** : Feedback à chaque changement (« Courgettes ajoutée au panier » / « retirée ») — toast, comme le web.
- **EF-09** : Bouton « Démarrer » **désactivé à panier vide** (règle web actuelle).
- **EF-10** : **Reprise de session** : la sélection est persistée et restaurée à l'ouverture de l'app *(corrige le rechargement commenté du web)*, avec un moyen de vider le panier en un geste.

### 3.3 Favoris
- **EF-11** : Cœur sur chaque carte (état persisté), comme le web.
- **EF-12** : Écran « Favoris » : liste avec état « Sélectionné / Sélectionner » cliquable + retrait du favori.

### 3.4 Objectifs nutritionnels
- **EF-13** : 5 cibles affichées en temps réel, compteur `x/cible` et état visuel atteint (vert ✓) / non atteint (rouge ✗) :
  - Antioxydants, Fibres, Vitamine C, Protéines végétales, Hydratation.
- **EF-14** : **Cibles alignées sur le catalogue réel (correction du web)** — décision à valider :
  - *Option A (recommandée)* : cibles = nombre de légumes effectivement taggés (Antioxydants 8, Fibres 7, Vitamine C 2, Protéines 1, Hydratation 2) ; une cible est atteinte dès que **tous les légumes de la catégorie** sont au panier.
  - *Option B* : conserver les cibles web (9 / 10 / 6 / 1 / 2) et tagger les 8 légumes manquants (poivrons, fenouil, panais, patate douce, radis → vérifier) pour les rendre atteignables.
- **EF-15** : Badges de catégorie sur chaque carte (comme le web).

### 3.5 Plan de cuisson optimisé (règle cœur du concept)
- **EF-16** : **Algorithme** (identique au web, spécifié formellement ici) :
  ```
  Entrée : sélection S = {v1..vn}, chaque vi a une durée dᵢ (min, borne haute)
  T        = max(dᵢ)
  départ(i) = T − dᵢ
  Résultat : étapes triées par (départ ascendant, puis dᵢ décroissant)
  Timer    = T minutes
  ```
- **EF-17** : **Modal de confirmation** avant démarrage (UI existante) : titre « Ordre optimal de cuisson », liste numérotée (icône, nom, saison, temps de cuisson, « Maintenant » ou « Dans N min — Ajouter au panier »), bloc « Instructions » (temps total, ordre chronologique, « tous finissent en même temps »), boutons **Démarrer la cuisson** / **Annuler**.
- **EF-18** : Liste d'étapes affichée **pendant** la cuisson : chaque étape passe de « à venir » → « **AJOUTER MAINTENANT** » (à `départ(i)`) → « ajoutée ✓ » (à `départ(i) + dᵢ`). *(Correction du web : le site marque « complété » arbitrairement 30 s après le départ ; ici l'état reflète la fin réelle de la cuisson du légume.)*
- **EF-19** : Rappel sonore/vibre **léger** (distinct de l'alarme de fin) à chaque échéance d'ajout d'un légume, même app en arrière-plan.

### 3.6 Minuteur
- **EF-20** : Décompte MM:SS de `T`, anneau de progression + pourcentage.
- **EF-21** : **Pause** (bascule « Reprendre ») / **Reprise** / **Arrêt** (réinitialise tout : décompte, étapes, plan masqué) — comportements web actualisés.
- **EF-22** : Précision **±1 seconde** sur 30 min, y compris app en arrière-plan ou écran éteint.
- **EF-23** : Fin de cuisson : **réveil d'écran + son ≥ 3 s (répétable) + vibration**, même app fermée ; bouton d'action « Prolonger +2 min » dans la notification d'alarme.
- **EF-24** : Prolongation possible depuis le timer principal : +1 / +2 / +5 min (recalcule l'état « prêt » de toutes les étapes — toutes les étiquettes « Dans N min » restent cohérentes).

### 3.7 Contenu éditorial
- **EF-25** : Écran « Conseils » avec les 6 conseils du site : température 100 °C / ébullition constante ; aromates dans l'eau (thym, laurier, romarin) ; découpe uniforme ; test à la fourchette ; cuisson vapeur ≈ 80 % de vitamines préservées vs 40 % en ébullition ; service immédiat (huile d'olive + sel de mer).

### 3.8 Réglages & données
- **EF-26** : Volume d'alarme, choix de son (≥ 3 sons), vibration (on/off + intensité), durée d'alarme (3–30 s).
- **EF-27** : Thème clair / sombre / système.
- **EF-28** : Langue : FR par défaut ; architecture i18n prête (traductions EN/ES au minimum livrées — *à valider*).
- **EF-29** : **Réinitialisation des données locales** (favoris, sélection, réglages) dans les réglages.
- **EF-30** : Aucune donnée ne sort du téléphone. Mention d'information (pas de DPO requis, 0 donnée collectée) visible sur l'écran d'accueil/About.

---

## 4. Exigences non fonctionnelles

| Catégorie | Exigence |
|---|---|
| **OS min** | Android 8.0 (API 26) — tests de non-régression Android 10 (API 29) et Android 15 (API 35) |
| **Langage/UI** | Kotlin, Jetpack Compose |
| **Architecture** | MVVM + Repository, navigation Compose, DI Hilt (ou Koin selon préférence) |
| **Persistance** | Room (favoris, dernière sélection, réglages) ; catalogue embarqué dans les assets, consulté depuis la BDD |
| **Minuteur** | `AlarmManager.setExactAndAllowWhileIdle` + notification persistante ; service de fond minimal sur Android 12+ |
| **Éveil écran** | `FLAG_SHOW_WHEN_LOCKED` / `FLAG_DISMISSABLE_KEYGUARD` + wake lock temporaire |
| **Taille APK** | < 15 Mo |
| **Batterie** | < 2 % par 8 h en veille avec un timer armé |
| **Réseau** | **Aucune** requête réseau dans la v1 |
| **Accessibilité** | TalkBack complet ; décompte ≥ 72 dp ; cibles tactiles ≥ 64 dp sur les actions primaires (Démarrer, Pause, Arrêt, Prolonger) |
| **Perf** | Démarrage à premier écran < 2 s ; rendu grille 28 légumes fluide (60 fps scroll) |
| **Sécurité** | Aucune permission superflue ; liste des permissions justifiée : `SCHEDULE_EXACT_ALARM` (Android 12+), `WAKE_LOCK`, `POST_NOTIFICATIONS`, `VIBRATE` |
| **Conformité** | RGPD : zéro donnée personnelle collectée → mention simple ; politique de confidentialité Play |
| **Test** | Unitaires ≥ 80 % sur le module « calcul plan optimisé » ; tests UI smoke des 5 écrans ; test de non-régression du timer en arrière-plan (téléphone verrouillé 10 min) |

---

## 5. UX — arborescence et écrans

```
Accueil (panier + décompte éventuel)
 ├─ Catalogue (grille de 28 légumes)
 │    ├─ Recherche (nom / bienfaits)
 │    ├─ Tri : Temps | Nom
 │    ├─ Filtre saison
 │    └─ Tap carte → [sélection] → Tap long (ou chevron) → Fiche détail
 ├─ Favoris
 │    └─ [Sélectionner] / [Retirer]
 ├─ Objectifs nutritionnels (compteurs live)
 ├─ Conseils
 ├─ [Démarrer la cuisson] (activé si panier ≠ ∅)
 │    └─ Modal « Ordre optimal de cuisson »
 │         ├─ [Démarrer la cuisson] → Écran timer actif
 │         └─ [Annuler] → retour panier
 └─ Réglages (alarme, thème, langue, données)
```

### Écran « Timer actif » (détail)
- Grand décompte MM:SS centré, anneau de progression, pourcentage.
- Liste verticale des étapes du plan de cuisson, chacune :
  - **état « à venir »** (grisé) → **état « AJOUTER MAINTENANT »** (mis en avant, pulsation) → **état « ajoutée ✓ »** (vert) ;
- Barre d'actions : **Pause/Reprendre**, **Prolonger +2**, **Arrêter**.
- Notification persistante visible dans le tiroir : icône + temps restant + action « Prolonger ».

### Écran « Alarme de fin »
- Plein écran, réveil d'écran, son + vibration.
- Gros bouton **« OK, c'est prêt »** (éteint alarme) et **« Prolonger +2 min »**.
- Toast/notification « Cuisson terminée 🎉 » (comme le site).

---

## 6. Données initiales (extrait — voir Annexe A)

Catégories nutritionnelles utilisées par le moteur (clés internes du site) :
`antioxydants · fibres · vitamineC · proteines · hydratation`

Exemples de données reprises du code source de la page :

| Légume | Plage | Durée moteur | Catégorie | Saison | kcal/100g |
|---|---|---|---|---|---|
| Courgettes | 5-7 min | 7 | hydratation | Été | 17 |
| Carottes | 15-20 min | 20 | fibres | Toute l'année | 41 |
| Brocoli | 7-10 min | 10 | antioxydants | Printemps/Été | 34 |
| Betteraves | 20-25 min | 25 | antioxydants | Automne/Hiver | 43 |
| Épinards | 2-3 min | 3 | — | Printemps/Automne | 23 |

*(La liste complète des 28 légumes est fournie en Annexe A, extraite telle quelle du JS de la page.)*

---

## 7. Tests d'acceptation (scénarios)

| # | Scénario | Résultat attendu |
|---|---|---|
| T1 | Je sélectionne **Brocoli (10 min)** seul → Démarrer | Modal affiche « Maintenant », timer = 10 min |
| T2 | Je sélectionne **Courgettes (7) + Épinards (3)** → Démarrer | Modal : courgettes « Maintenant », épinards « Dans 4 min » ; timer = **7 min** |
| T3 | Pendant la cuisson T2, à **t = 3 min** | L'étape Épinards passe en « AJOUTER MAINTENANT » + bip léger + notification |
| T4 | À **t = 7 min** (fin T2) | Alarme plein écran, son ≥ 3 s, vibration, réveil écran, même si app fermée / écran verrouillé |
| T5 | Je pause à **t = 2 min**, j'attends 60 s, je reprends | Le décompte reprend à **5:00** exactement (±1 s) |
| T6 | Je sélectionne les 8 légumes antioxydants du catalogue | La cible « Antioxydants » passe en vert ✓ (Option A) |
| J'appuie « Prolonger +2 min » à la fin | Timer repart à 2:00, alarme silencieuse, notification mise à jour |
| T8 | Je réinstalle l'app | Favoris et sélection restaurés si « Réinitialisation » non faite ; sinon état propre |
| T9 | Je ferme l'app, je verrouille le phone, j'attends la fin | **Alarme toujours déclenchée** |

---

## 8. Jalon et estimation

**Hypothèse** : 1 dev Android Kotlin/Compose + 1 designer UI, 1 chef de produit (partiel).

| Phase | Contenu | Durée |
|---|---|---|
| **J0 — Specs figées** | Décisions tranchées (Option A/B cibles, sons, langues) | 0,5 sem |
| **J1 — Conception** | Maquettes Compose, kit UI, choix des 3 sons | 1 sem |
| **J2 — MVP** | Catalogue + panier + favoris + objectif + modal + timer (EF-01 → EF-23) | 3 sem |
| **J3 — Finitions** | Alarme fiable AR, écran réglages, accessibilité, thème, i18n | 2 sem |
| **J4 — QA + pub** | Tests device lab + Play Console | 1 sem |
| **Total estimé** | | **≈ 7,5 semaines** |

**Coûts indicatifs (France, 2026)**

| Poste | Montant |
|---|---|
| Dev Android (7,5 sem × 5 j) | 3 500 – 6 000 €/sem |
| Design UI (maquettes + kit) | 800 – 1 500 € |
| Play Console (one-shot) | 25 € |
| Tests device lab (Firebase Test Lab, optionnel) | 0 – 300 € |
| **Total estimé hors maintenance** | **≈ 40 000 – 75 000 €** |
| Maintenance/an (bugs + mises à jour Android) | 10 %/an du dev |

---

## 9. Risques et mitigations

| Risque | G | M | Mitigation |
|---|---|---|---|
| Android 12+ refuse `setExact` sans autorisation | **Élevé** | Fort | Suggestion d'activation « autorisations d'alarme exactes » au premier démarrage + fallback notification persistante |
| Battery saver tuant la notification d'alarme | Moyen | Fort | Wake lock temporaire + documentation (page « Conseils » : désactiver l'optimisation batterie sur l'app) |
| Cibles nutritionnelles incohérentes (héritage web) | Moyen | Fort | Décision Option A/B tranchée à J0, documentée dans l'app |
| Catalogue incomplet pour des usages réels (tomates, aubergines, patates douces OK, mais pas de courge, patate, pois de château) | Moyen | Moyen | v2 : ajout de légumes perso (EF hors périmètre v1) |
| UX « 3 sons » non testés → son inaudible en cuisine | Moyen | Moyen | Sélection de 3 profils (bip, carillon, longue note) + volume au maximum par défaut |
| Dev Kotlin/Compose disponible en nombre limité | Bas | Moyen | Pool de 3 freelances + code review systématique |

---

## 10. Livrables

1. **Source code** (Git lab ou GitHub) avec README de build.
2. **APK et AAB** signés (internal track / production).
3. **Dossier de livraison** : architecture, guide des permissions, plan de test, notes de version.
4. **Guide utilisateur** (PDF court) : 5 pages — prise en main, lecture du plan de cuisson, alarme, réglages.
5. **Kit UI** (maquettes Figma) pour réutiliser le design system en v2.
6. **Liste des données initiales** (Annexe A) exportable en JSON pour future maintenance.

---

## 11. Décisions à trancher avant J1 (check-list)

| # | Question | Recommandation |
|---|---|---|
| D1 | Option A ou B pour les cibles nutritionnelles ? | **Option A** (cibles = nombre de légumes taggés, 8/7/2/1/2) |
| D2 | Reprise de session à l'ouverture (sélection persistée) ? | **Oui** (EF-10) |
| D3 | Nombre et type de sons d'alarme | **3 sons** + volume max par défaut |
| D4 | Langues livrées en v1 | FR + EN (i18n prête pour ES) |
| D5 | Filtre saison inclus en v1 ? | **Oui** (EF-04) |
| D6 | Prolongation pendant l'alarme (+2 min) ? | **Oui** (EF-23/24) |
| D7 | Badge « Ajoutés » sur les étapes déjà passées | **Oui** (EF-18) |
| D8 | Thème sombre par défaut ? | **Système** (suivi du système) |

---

## 12. Points ouverts identifiés (fonctionnel / technique — à trancher avant J1)

Revue complémentaire du cahier des charges, ciblée sur les zones où le document décrit un **comportement visible** sans spécifier le **mécanisme** qui le rend possible — ce sont les points les plus susceptibles de faire dériver l'estimation §8.

### 12.1 Fonctionnel / UX

- **Collision de vocabulaire « panier »** : le terme désigne à la fois la sélection avant cuisson (EF-06/EF-08) et l'action pendant la cuisson (EF-18 « AJOUTER MAINTENANT »). Deux actions différentes, même mot → ambiguïté en dev comme en UI. **Proposition** : renommer l'une des deux occurrences (ex. « panier de sélection » vs « ajouter à la marmite / au panier vapeur »).
- **Accessibilité des cibles nutritionnelles** : EF-13 utilise vert ✓ / rouge ✗ ; les symboles limitent déjà la dépendance à la couleur seule (WCAG 1.4.1), mais ce n'est pas formulé comme exigence testable. **Proposition** : ajouter explicitement « l'état atteint/non atteint ne doit jamais reposer uniquement sur la couleur » aux critères de recette de l'écran Objectifs.
- **État dégradé si permission refusée** (`SCHEDULE_EXACT_ALARM` Android 13+, `POST_NOTIFICATIONS` Android 13+) non spécifié : alarme approximative ? bandeau d'avertissement permanent ? blocage du démarrage de cuisson ? **Proposition** : nouvel EF décrivant le comportement de repli et son déclenchement dans l'écran de démarrage.

### 12.2 Technique / fiabilité

- **Interaction pause/reprise avec les alarmes système non spécifiée** : EF-21 décrit le comportement UI, pas ce qui se passe côté `AlarmManager`. Il faut annuler et replanifier l'alarme de fin *et* toutes les alarmes de rappel par étape (EF-19) à chaque pause / reprise / prolongation (EF-24), en recalculant `départ(i)` pour les étapes restantes. Sans ce mécanisme, le scénario T5 (reprise exacte à 5:00 ±1 s) n'est pas atteignable de façon fiable.
- **Reconstruction d'état après kill process ou redémarrage du téléphone** non prévue : si l'app est tuée pendant une cuisson (cas fréquent avec les surcouches OEM agressives déjà citées en §9), il faut persister un **timestamp de fin absolu** (et non une durée restante) pour que l'UI et les alarmes se resynchronisent correctement à la réouverture ou après un `BOOT_COMPLETED`. À ajouter en exigence non fonctionnelle (§4).
- **Choix de la base de temps des alarmes** : préciser l'usage du temps écoulé (`ELAPSED_REALTIME`, via `setExactAndAllowWhileIdle`) plutôt que de l'horloge murale (`RTC`), pour éviter toute dérive en cas de changement d'heure ou de fuseau pendant une cuisson en cours.

---

## Annexe A — Catalogue des 28 légumes (extrait du code source de la page)

| # | Nom | Plage | Durée | Bénéfices | Catégorie | Saison | kcal/100g |
|---|---|---|---|---|---|---|---|
| 1 | Courgettes | 5-7 min | 7 | Hydratation, Faible en calories | hydratation | Été | 17 |
| 2 | Carottes | 15-20 min | 20 | Bêta-carotène, Fibres | fibres | Toute l'année | 41 |
| 3 | Haricots verts | 6-8 min | 8 | Fibres, Vitamine A | fibres | Été | 31 |
| 4 | Petits pois | 3-5 min | 5 | Protéines végétales, Fibres | proteines | Printemps/Été | 84 |
| 5 | Pommes de terre | 15-20 min | 20 | Potassium, Glucides | — | Toute l'année | 77 |
| 6 | Brocoli | 7-10 min | 10 | Vitamine C & K, Antioxydants | antioxydants | Printemps/Été | 34 |
| 7 | Chou-fleur | 8-10 min | 10 | Antioxydants, Vitamine C | antioxydants | Automne/Hiver | 25 |
| 8 | Asperges | 4-6 min | 6 | Folates, Diurétique | — | Printemps | 20 |
| 9 | Choux de Bruxelles | 10-12 min | 12 | Vitamine C & K | vitamineC | Automne/Hiver | 43 |
| 10 | Épinards | 2-3 min | 3 | Fer, Calcium | — | Printemps/Automne | 23 |
| 11 | Poivrons | 6-8 min | 8 | Vitamine C, Antioxydants | vitamineC | Été | 31 |
| 12 | Fenouil | 10-12 min | 12 | Digestion, Fibres | — | Automne/Hiver | 31 |
| 13 | Navets | 15-18 min | 18 | Fibres, Vitamine C | fibres | Automne/Hiver | 28 |
| 14 | Panais | 15-18 min | 18 | Vitamine C, Potassium | — | Automne/Hiver | 75 |
| 15 | Patate douce | 10-15 min | 15 | Bêta-carotène, Fibres | — | Automne | 86 |
| 16 | Betteraves | 20-25 min | 25 | Nitrates, Antioxydants | antioxydants | Automne/Hiver | 43 |
| 17 | Chou kale | 3-5 min | 5 | Vitamine A, C, K | antioxydants | Automne/Hiver | 49 |
| 18 | Radis | 2-3 min | 3 | Antioxydants, Détox | antioxydants | Printemps/Été | 16 |
| 19 | Aubergines | 8-12 min | 12 | Fibres, Antioxydants | antioxydants | Été | 25 |
| 20 | Tomates | 3-5 min | 5 | Lycopène, Vitamine C | antioxydants | Été | 18 |
| 21 | Oignons | 8-10 min | 10 | Quercétine, Soufre | — | Toute l'année | 40 |
| 22 | Ail | 5-7 min | 7 | Allicine, Antioxydants | antioxydants | Toute l'année | 149 |
| 23 | Poireaux | 8-12 min | 12 | Vitamine K, Fibres | fibres | Automne/Hiver | 61 |
| 24 | Céleri | 6-8 min | 8 | Vitamine K, Hydratation | hydratation | Toute l'année | 16 |
| 25 | Endives | 4-6 min | 6 | Vitamine B9, Fibres | fibres | Automne/Hiver | 17 |
| 26 | Artichauts | 15-20 min | 20 | Cynarine, Fibres | fibres | Printemps/Été | 47 |
| 27 | Champignons | 4-6 min | 6 | Vitamine D, Sélénium | — | Toute l'année | 22 |
| 28 | Maïs | 8-10 min | 10 | Lutéine, Fibres | fibres | Été | 86 |

> NB : les valeurs « Durée » sont les **bornes hautes** de la plage affichée (valeur `duration` du JS du site) — c'est la valeur retenue par le moteur du calcul.
> NB 2 : le code source contient **28 légumes**, pas 20 — j'ai corrigé la v1 de ce cahier des charges.
> NB 3 : les 28 lignes ci-dessus sont **exactement** celles de la page (mêmes temps, mêmes calories, mêmes saisons, mêmes catégories). Toute modification doit être validée par Fabrice Heuvrard.

---

## Changelog du cahier des charges

| Version | Date | Auteurs | Notes |
|---|---|---|---|
| 1.0 | 23/08/2026 | Hermes Agent | Version initiale (hypothèse « minuteur mono-légume » — **obsolète**) |
| 2.0 | 23/08/2026 | Hermes Agent | **Réécrit à partir de l'analyse du code source du site** : 28 légumes, panier multi-légumes, objectifs nutritionnels (cibles corrigées), plan de cuisson optimisé, timer AR, alarme indiscutable. Ajout des 4 « découvertes web » à corriger |
