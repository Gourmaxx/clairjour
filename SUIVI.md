# Clairjour — SUIVI.md

## 🧭 Ligne directrice
**État** (2026-07-11) : Toutes les tâches terminées ✅. APK debug (~23 Mo) + APK release signé (~3,5 Mo, minifié R8) buildés avec succès. Prochaine étape : installer sur téléphone + préparer publication Play Store.

## ✅ Fait
- [x] Choix du branding (Clairjour — bleu profond + or)
- [x] Mockups HTML des 3 directions (localhost:8765)
- [x] Design doc complet (docs/DESIGN.md)
- [x] Scaffold projet Android Studio (single-module, packages internes)
- [x] Gradle 8.10.2 + AGP 8.7.3 + Kotlin 2.0.21 + version catalog
- [x] Room DB : 5 entités (Addiction, JournalEntry, Pledge, MilestoneReached, RelapseEvent) + DAOs + Converters
- [x] DataStore : SettingsRepository (thème, langue, notifs, onboarding)
- [x] Bibliothèque 50 motivations originales FR/EN (assets/motivations.json)
- [x] Thème Clairjour (Color, Type, Theme) + Material 3 light/dark
- [x] Écrans : Onboarding, Home, Journal, Stats, Settings, AddictionEdit
- [x] Navigation Compose (bottom bar + routes)
- [x] Notifications : 3 canaux + workers + scheduler WorkManager
- [x] Widget Glance (compteur écran d'accueil)
- [x] i18n FR + EN (strings.xml complet)
- [x] Tests unitaires : Milestones, Converters
- [x] Câbler ReminderScheduler dans le flow settings + TimePicker M3
- [x] Sélecteur de date de début (DatePicker M3)
- [x] **Animation jalon** — overlay Compose scale+fade + célébration plein écran + insertion/observation en DB (MilestoneDao)
- [x] **Backup JSON local** — BackupRepository (export/import ContentResolver), section Settings avec file pickers Android, Toast feedback
- [x] **Icône adaptive redesignée** — soleil levant avec rayons or (#D4A857) sur fond bleu (#1E3A5F)
- [x] **Tests instrumentation** — AddictionDaoTest (5 tests) + MilestoneDaoTest (5 tests), Room in-memory
- [x] **Keystore release généré** — `clairjour-release.jks` + `keystore.properties` (exclu du git)
- [x] **Build debug SUCCESSFUL** (2026-07-11) — 39 tasks, `app/build/outputs/apk/debug/app-debug.apk`
- [x] **Build release SUCCESSFUL** (2026-07-11) — 54 tasks, signé, minifié R8, `app/build/outputs/apk/release/app-release.apk` (3,5 Mo)

## ⏳ Reste à faire
- [ ] Installer et tester les APK sur téléphone (`adb install`)
- [ ] Publication Play Store (checklist CLAUDE.md)

## 📋 Notes / gotchas
- **Animation jalons** : HomeViewModel insère les jalons atteints en DB (IGNORE conflict = idempotent), observe les non-vus via MilestoneDao.observeFor(), HomeScreen affiche MilestoneCelebrationOverlay. dismissMilestone() appelle markSeen().

- **Backup** : BackupRepository.kt dans `data/backup/`. DTOs @Serializable dans BackupData.kt (Instant → Long, LocalDate → String). ContentResolver pour lire/écrire les Uris du file picker. DAOs enrichis de getAll() + deleteAll() + insertAll().

- **Keystore release** : alias `clairjour-key`, password `Cl4irj0ur!2026`, valide 10 000 jours. Fichiers dans racine projet, **EXCLUS DU GIT** via .gitignore.

- **compileSdk = 36** requis (imposé par androidx.core 1.15 + work 2.10)
- Warning bénin : "SDK XML version 4 encountered but tools understand only version 3"
- JDK 21 bundled : `C:\Program Files\Android\Android Studio\jbr\` — exporter JAVA_HOME pour build CLI
- Chemin APK debug : `app/build/outputs/apk/debug/app-debug.apk`
- Chemin APK release : `app/build/outputs/apk/release/app-release.apk`

## 🚀 Pour installer sur téléphone
```powershell
adb install "C:\users\flora\documents\dev\Application\Clair jour\app\build\outputs\apk\debug\app-debug.apk"
```
