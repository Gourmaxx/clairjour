# Clairjour

Application Android de suivi de sobriété multi-addictions — compteur temps réel, journal, jalons animés, statistiques, sauvegarde locale chiffrée.

## Fonctionnalités

- **Multi-addictions** — suivre plusieurs domaines en parallèle (tabac, alcool, sucre, écrans, etc.).
- **Compteur XL** — jours + heures/minutes/secondes, avec ligne calendaire année/mois/jour dès un mois de sobriété.
- **Journal** — entrées libres, filtres, swipe-to-dismiss avec confirmation.
- **Jalons animés** — 1/3/7/14/30/60/90/180/365 jours + célébration overlay quand un jalon tombe.
- **Rechute + undo** — bouton direct sur la Home, snackbar d'annulation avec restauration des milestones.
- **Panic button** — écran de crise dédié par addiction, avec raisons personnelles.
- **Backup chiffré** — export/import JSON local, chiffrement AES-256/GCM + PBKDF2 (210k iters), passphrase utilisateur.
- **SQLCipher** — base Room chiffrée, clé stockée dans `EncryptedSharedPreferences`.
- **i18n** — FR/EN, sélecteur in-app (lit `LocalConfiguration.current`, jamais `Locale.getDefault()`).
- **Widget Home + notifications** privées (`VISIBILITY_SECRET`).

## Stack

Kotlin 2.0 · Jetpack Compose (BOM) · Room + SQLCipher · DataStore · WorkManager · Vico charts · Glance widget · AGP 8.7 · Java 17.

Pas de DI (ni Hilt ni Koin) — injection manuelle via `AppContainer`.

## Build

```bash
./gradlew assembleDebug
./gradlew installDebug
```

APK à jour dans [Releases](../../releases/latest).

Build release signé : `keystore.properties` à la racine (gitignoré) pointant vers `clairjour-release.jks`. Attention au chemin dans `storeFile` — relatif à `app/`, donc `storeFile=../clairjour-release.jks`.

## Licence

MIT — voir [LICENSE](LICENSE).
