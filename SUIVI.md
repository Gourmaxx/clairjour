# Clairjour — SUIVI.md

## 🧭 Ligne directrice
**État** (2026-07-07) : Audit qualité effectué — 23 correctifs appliqués (bugs critiques, optimisations, code smell). Prochaine étape : rebuild APK debug puis installer sur téléphone pour tester le flow réel.

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
- [x] Écrans : Onboarding (4 étapes), Home (compteur + pledge + motivation + journal quick), Journal (liste + éditeur), Stats (cards + mood chart Canvas), Settings, AddictionDetail (jalons + relapse), AddictionEdit
- [x] Navigation Compose (bottom bar + routes)
- [x] Notifications : 3 canaux + workers pledge/journal + scheduler WorkManager
- [x] Widget Glance (compteur écran d'accueil)
- [x] i18n FR + EN (strings.xml complet)
- [x] Tests unitaires : Milestones, Converters
- [x] **Build debug SUCCESSFUL** → `app/build/outputs/apk/debug/app-debug.apk`

## ⏳ Reste à faire
- [ ] Rebuild APK debug après les correctifs d'audit
- [ ] Installer et tester l'APK sur téléphone (`adb install`)
- [ ] P7 restant — Backup Google Drive AppDataFolder (pas encore implémenté ; l'export/import JSON local est simple à ajouter)
- [x] Câbler ReminderScheduler dans le flow settings (toggle ON/OFF + changement d'heure → WorkManager mis à jour)
- [x] Sélecteur d'heure pour les rappels (TimePicker M3 dans settings)
- [x] Sélecteur de date de début (DatePicker M3 dans AddictionEditScreen)
- [ ] Animation d'atteinte de jalon
- [ ] Signature release + build release APK
- [ ] Icône adaptive plus travaillée (actuellement soleil vectoriel simple)
- [ ] Tests instrumentation Room + UI Compose

## 📋 Notes / gotchas
- **Audit 2026-07-07** : AddictionDao passé de `interface` à `abstract class` pour supporter `@Transaction`. RelapseDao a `countForDate`. JournalEditorScreen accepte un `date: LocalDate?` optionnel (null = aujourd'hui) — fix bug entrée passée ouvre le mauvais jour.

- **compileSdk = 36** requis (imposé par androidx.core 1.15 + work 2.10) — SDK 36 était déjà installé sur la machine
- **Warning bénin** : "SDK XML version 4 encountered but tools understand only version 3" — build passe quand même
- Wrapper Gradle 8.10.2 (copié depuis AlcoLimit, distribution téléchargée automatiquement au premier build)
- JDK 21 bundled dans Android Studio (`C:\Program Files\Android\Android Studio\jbr\`) — obligatoire d'exporter `JAVA_HOME` pour le build CLI
- Room + KSP OK, motivations JSON parsé au démarrage via kotlinx.serialization
- Widget Glance : nécessite `androidx.glance:glance-appwidget:1.1.1` et `glance-material3` — attention aux imports `ColorProvider` (deux packages, prendre `androidx.glance.color`)
- Mockups HTML branding : toujours servi sur `http://localhost:8765/` (à arrêter quand plus besoin)
- Deprecation `Icons.Filled.ArrowBack` → passer sur `Icons.AutoMirrored.Filled.ArrowBack` (déjà fait dans AddictionDetailScreen)
- Chemin APK final : `sobriety-app/app/build/outputs/apk/debug/app-debug.apk` (~23.8 Mo)

## 🚀 Pour installer sur téléphone
```powershell
adb install "C:\users\flora\documents\dev\sobriety-app\app\build\outputs\apk\debug\app-debug.apk"
```
Ou ouvrir le projet dans Android Studio Panda (File → Open → `sobriety-app`) et cliquer Run.
