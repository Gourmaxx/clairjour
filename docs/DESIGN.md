# Clairjour — Design Document

**Version** : 0.1
**Date** : 2026-07-05
**Auteur** : Floran (avec Claude)

## 1. Vision produit

Clairjour est une application Android de suivi personnel de sobriété. L'utilisateur y déclare une ou plusieurs addictions (alcool, tabac, drogue, sucre, écran, jeu, autre), fixe une date de départ, et suit sa progression jour après jour à travers un compteur, des jalons, un pledge quotidien et un journal.

L'app est **100% locale** (aucun compte, aucun serveur), avec un backup optionnel vers Google Drive. Elle est disponible en français et en anglais.

**Non-goals du MVP** :
- Pas de communauté / dimension sociale
- Pas de compte utilisateur ni d'authentification
- Pas de coach conversationnel IA
- Pas de contenus audio/vidéo guidés
- Pas de sync temps réel multi-appareils (le backup Drive suffit)

## 2. Stack technique

| Domaine | Choix |
|---|---|
| Langage | Kotlin 2.x |
| UI | Jetpack Compose (Material 3) |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| DB locale | Room |
| Prefs / small state | DataStore Preferences |
| Navigation | Compose Navigation |
| Async | Coroutines + Flow |
| Background jobs | WorkManager (notifications quotidiennes) |
| Notifications | NotificationCompat + WorkManager |
| Backup cloud | Google Drive AppDataFolder scope (données invisibles pour l'utilisateur, pas d'OAuth étendu) |
| Widget | Glance (Compose pour widgets) |
| Charts | Vico (Compose-native, léger) |
| Tests | JUnit 5, Turbine, Compose UI tests |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 |

## 3. Modules Gradle

```
app/                    ← module principal, Compose UI, entry point
core-ui/                ← theme Clairjour, composants réutilisables
core-data/              ← Room DB, DataStore, DAOs, entités
core-domain/            ← use cases, modèles domaine (pas de dépendance Android)
core-notifications/     ← WorkManager workers + canaux
core-backup/            ← Google Drive AppDataFolder client
feature-onboarding/
feature-home/
feature-addiction-detail/
feature-journal/
feature-stats/
feature-settings/
widget/                 ← module Glance
```

Séparation stricte : `core-domain` ne dépend d'aucun framework Android.

## 4. Data model

### Entités Room

**Addiction**
```
id            UUID (PK)
name          String            ← libellé personnalisé (ex: "Alcool")
type          enum              ← ALCOHOL | TOBACCO | DRUG | SUGAR | SCREEN | GAMBLING | PORN | OTHER
startDate     Instant           ← moment de début de sobriété
costPerDay    Double?           ← coût quotidien (pour économies)
unitPerDay    Double?           ← ex: 20 cigarettes/jour
unitLabel     String?           ← "cigarettes", "verres"...
colorSeed     Int               ← seed pour couleur d'accent
isPrimary     Boolean           ← une seule addiction primaire à la fois
isActive      Boolean           ← soft-delete
createdAt     Instant
```

**JournalEntry**
```
id            UUID (PK)
date          LocalDate         ← une entrée max par jour
mood          Int (1..5)
notes         String
triggers      String (JSON list)
gratitude     String?
hadCravings   Boolean
createdAt     Instant
updatedAt     Instant
```

**Pledge**
```
id            UUID (PK)
addictionId   UUID (FK)
date          LocalDate
timestamp     Instant
```
Unique index (addictionId, date).

**MilestoneReached**
```
id            UUID (PK)
addictionId   UUID (FK)
milestoneDays Int
reachedAt     Instant
seenByUser    Boolean           ← pour animer une seule fois
```

**RelapseEvent** (optionnel, si l'utilisateur reset avec note)
```
id            UUID (PK)
addictionId   UUID (FK)
date          LocalDate
notes         String?
triggers      String (JSON list)
previousStreakDays  Int         ← pour "record précédent"
```

### Motivations

Bibliothèque locale de citations originales / du domaine public, embarquée en JSON dans les assets. Chaque motivation :
```
{
  "id": 1,
  "fr": "…",
  "en": "…",
  "author": "…"        // null si anonyme
}
```
Sélection déterministe par `date.toEpochDay() % motivations.size` pour que le contenu du jour soit stable.

### Jalons standards

`1, 3, 7, 14, 30, 60, 90, 180, 365, 730, 1095, 1825, 3650` jours (soit 1j → 10a).

## 5. Écrans

### 5.1 Onboarding (1er lancement uniquement)
- Écran 1 : accueil Clairjour + tagline
- Écran 2 : choix de la langue (fr/en, pré-rempli selon locale)
- Écran 3 : ajout de la première addiction (type + nom + date de début + coût quotidien optionnel)
- Écran 4 : demande de permission notifications (Android 13+)

### 5.2 Home
Écran principal après onboarding.
- Grand compteur central : **N jours** (ou "Aujourd'hui" si N=0)
- Sous-titre : nom de l'addiction primaire
- Sélecteur horizontal si plusieurs addictions
- Progress bar vers le prochain jalon + libellé ("35 jours avant 6 mois")
- Bouton **"S'engager pour aujourd'hui"** (masqué si déjà fait) → ouvre un modal de pledge
- Card "Pensée du jour" (citation)
- Card "Journal" : bouton d'ajout d'entrée du jour (ou aperçu si déjà écrite)

### 5.3 Détail addiction
Accès via tap sur le compteur ou via une addiction secondaire.
- Header : nom + type + date de début + streak actuel
- Timeline verticale des jalons (atteints en or, à venir en gris)
- Cards stats : économies cumulées, unités évitées (si renseigné), temps depuis début
- Bouton discret "Signaler une rechute" (bas de page, secondaire) → dialog de confirmation → reset avec option de note

### 5.4 Journal
- Liste chronologique des entrées (les plus récentes en haut)
- FAB pour ajouter/éditer l'entrée du jour
- Éditeur : slider mood (1-5), texte libre, chips triggers, champ gratitude, toggle cravings
- Recherche par mot-clé + filtre par mood

### 5.5 Statistiques
- Graphique jours sobres par addiction (Vico line chart)
- Graphique mood dans le temps
- Cards agrégées : jours totaux sobres cumulés, économies totales, jalons atteints

### 5.6 Paramètres
- Langue (fr/en, override du système)
- Thème (system / light / dark)
- Notifications : heure du pledge matin, heure du rappel journal soir, activation par canal
- Backup Google Drive : connecter compte, dernière sauvegarde, restaurer, backup manuel
- Export / import JSON (fichier local)
- Gestion addictions : éditer, réordonner, archiver, définir la primaire
- À propos : version, licences

## 6. Notifications

Deux canaux :
- `daily_pledge` : le matin (heure paramétrable, défaut 08:00) — "Un nouveau jour clair"
- `journal_reminder` : le soir (défaut 21:00) — "Comment s'est passée ta journée ?"
- `milestone` : instantanée à l'atteinte d'un jalon — "Tu viens d'atteindre 30 jours"

Implémentation : `WorkManager` avec `PeriodicWorkRequest` par canal, respect du Do-Not-Disturb.

## 7. Backup Google Drive

Scope minimal : **AppDataFolder** (dossier caché, propre à l'app, invisible dans Drive UI). Aucune donnée utilisateur exposée à d'autres apps.

- Format : JSON structuré, une archive par backup
- Cadence : automatique quotidienne (WorkManager), rétention 7 derniers backups
- Bouton manuel dans Settings
- Restauration : écrase la DB locale après confirmation

## 8. Thème Clairjour

Palette (déjà validée) :
```
primary        #1E3A5F
onPrimary      #F5F1E8
accent         #D4A857
surface (light)#F5F1E8
surface (dark) #14283F
onSurface      #2C2C2C / #F5F1E8
```

Typographie :
- Display / titles : **Fraunces** (serif)
- Body / UI : **Inter** (sans)
- Mono (compteurs) : **Fraunces** en variante wght 600, feature `ss01`

Motifs UI : coins arrondis 12dp, séparateurs fins or, halos doux, animations sobres (fade + translate 200ms).

## 9. i18n

Deux locales : `values/` (en, défaut) + `values-fr/`. Toutes les chaînes visibles vont dans `strings.xml`. Sélecteur en Settings force la locale via `AppCompatDelegate.setApplicationLocales`.

Formatage nombres/dates : `java.time` + `NumberFormat.getCurrencyInstance(locale)`.

## 10. Tests

- **Unit** (JUnit 5) : use cases, repositories, formatters, sélecteur de citation, calcul jalons
- **Instrumented** : DAOs Room (in-memory), migrations, WorkManager workers
- **UI** (Compose test) : flows critiques → onboarding, ajout addiction, pledge, ajout entrée journal, atteinte jalon

Objectif : couverture des couches domain + data à 80%+ ; UI aux happy paths.

## 11. Sécurité / vie privée

- Aucune donnée envoyée à un serveur tiers (hors Drive, opt-in, scope AppDataFolder)
- Pas de tracking / analytics
- Journal chiffré au repos avec `EncryptedSharedPreferences` pour la clé, DB Room ouverte via SQLCipher (optionnel MVP+1 si complexité trop lourde ; MVP peut se contenter d'une DB standard vu qu'Android chiffre le storage par défaut depuis Android 10)

## 12. Découpage d'implémentation (phases)

1. **P0 — Scaffold** : projet Gradle multi-module, theme Clairjour, navigation vide
2. **P1 — Core data** : Room + entités + DAOs + migrations, DataStore
3. **P2 — Onboarding + Home** : flow onboarding, ajout addiction, compteur, pledge
4. **P3 — Jalons + motivations** : bibliothèque motivations, calcul jalons, animations d'atteinte
5. **P4 — Journal** : liste, éditeur, filtres
6. **P5 — Stats** : Vico charts, cards
7. **P6 — Notifications** : WorkManager, 3 canaux, réglages
8. **P7 — Settings + Backup** : écran settings, Drive AppDataFolder, export/import JSON
9. **P8 — Widget** : Glance widget compteur
10. **P9 — i18n + polish** : ressources FR/EN complètes, animations, revue UX
11. **P10 — Tests + release** : couverture, build release signé

Chaque phase se termine par un build qui tourne + tests passants.
