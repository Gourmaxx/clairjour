# Clairjour — SUIVI.md

## 🧭 Ligne directrice
**État** (2026-07-21) : Chantier de refonte v0.2 terminé ✅. Sécurité renforcée (backup chiffré AES-GCM + SQLCipher + notifs privées + import transactionnel), UX critique complétée (suppression addiction/journal, accessibilité TalkBack, undo rechute, onboarding flexible, i18n déclencheurs), et features clés livrées (Panic button/CrisisScreen, raisons personnelles, stats enrichies, filtres journal, dialogs pédagogiques). Build debug OK (~46 Mo — SQLCipher inclus). Prochaine étape : installer/tester sur téléphone + relancer build release signé + préparer publication Play Store.

## ✅ Fait (v0.1 + v0.2)
### v0.1 initial
- [x] Branding Clairjour + mockups + design doc
- [x] Scaffold Android Studio complet, Gradle 8.10.2 + AGP 8.7.3 + Kotlin 2.0.21
- [x] Room DB v1 (5 entités) + DataStore + WorkManager + Glance + Vico + i18n FR/EN
- [x] Écrans Onboarding, Home, Journal, Stats, Settings, AddictionEdit
- [x] Notifications + widget + backup JSON local + animation jalons
- [x] Icône adaptive + keystore release + build debug/release signé (v0.1.0, 11 juillet)

### v0.2 refonte — Sécurité (Phase 1)
- [x] **P1.1** Chiffrement backup JSON AES-256/GCM + PBKDF2 210k iters (`data/backup/BackupCrypto.kt`)
- [x] **P1.2** SQLCipher pour DB Room, clé stockée dans EncryptedSharedPreferences (`security/DatabasePassphraseProvider.kt`, `data/db/ClairjourDatabase.kt`)
- [x] **P1.3** `allowBackup=false` + `data_extraction_rules` restrictif (exclusion DB, SharedPrefs sécurisés, DataStore)
- [x] **P1.4** Import backup transactionnel (`withTransaction`), validation version, plafond 10 Mo / 50k rows par table
- [x] **P1.5** Notifications `VISIBILITY_SECRET` + PublicVersion neutre (`ReminderWorker`)
- [x] **P1.6** Retrait permission `RECEIVE_BOOT_COMPLETED` inutile + `usesCleartextTraffic=false` + `network_security_config.xml`
- [x] **P1.7** Signing config lit env vars `CLAIRJOUR_STORE_PASSWORD/KEY_PASSWORD` en priorité + doc keystore rotation

### v0.2 refonte — UX critique (Phase 2)
- [x] **P2.8** Suppression : bouton poubelle TopBar `AddictionEditScreen` (mode édition) + `SwipeToDismissBox` sur items `JournalScreen` avec `AlertDialog` confirmation
- [x] **P2.9** Accessibilité TalkBack : `contentDescription` partout, semantics sur `CounterBlock` (Home) et `StepIndicator` (Onboarding)
- [x] **P2.10** Undo rechute : `Snackbar` avec action "Annuler" 5s, `RelapseRepository.undoRelapse()` + snapshot
- [x] **P2.11** i18n déclencheurs journal : `stringArrayResource` avec clés EN persistées et labels FR/EN affichés
- [x] **P2.12** Onboarding flexible : DatePicker "sobre depuis", champs coût/unités marqués "(facultatif)" + bouton "Configurer plus tard"
- [x] **P2.13** Touch targets ≥ 48dp (chips humeur JournalScreen)

### v0.2 refonte — Features (Phase 3)
- [x] **P3.14** Panic button + `CrisisScreen` : FAB "Respire" sur Home, cercle de respiration animé 4s, contacts 3114 + Drogues Info Service (dial direct), raisons personnelles affichées
- [x] **P3.15** Raisons personnelles ("why") : nouveau champ `personalReasons: List<String>` sur `AddictionEntity` (DB v2), UI liste éditable dans `AddictionEditScreen`, affichage dans `CrisisScreen`, propagées dans backup
- [x] **P3.16** Documenté comme reste v0.3 (auto-backup) — trade-off sécurité vs praticité à trancher
- [x] **P3.17** Stats enrichies : streak actuel vs record, moyennes humeur 7j/30j, top 3 déclencheurs (30 derniers jours), état vide parlant sur graphe humeur

### v0.2 refonte — Polish (Phase 4)
- [x] **P4.18** Filtres journal : FilterChips humeur 1-5 + cravings + bouton "Effacer les filtres"
- [x] **P4.19** États vides parlants (StatsScreen graphe humeur)
- [x] **P4.20** Feedback save : `Snackbar` "Domaine/Entrée enregistrée" sur `AddictionEditScreen` et `JournalEditorScreen`
- [x] **P4.21** Prefix `€ ` sur champ coût (AddictionEditScreen + Onboarding)
- [x] **P4.22** `BuildConfig.VERSION_NAME` dynamique (SettingsScreen)
- [x] **P4.23** Vico beta gardé (2.0.0 GA pas encore sortie — surveiller)
- [x] **P4.24** Pledge : `AlertDialog` pédagogique avant l'engagement (explication "qu'est-ce que l'engagement du jour")

### Build final
- [x] `./gradlew assembleDebug` **SUCCESSFUL** (2026-07-21, ~46 Mo APK avec SQLCipher)
- [x] `./gradlew testDebugUnitTest` **SUCCESSFUL** (2026-07-21)

## ⏳ Reste à faire
- [ ] Installer et tester APK sur téléphone (`adb install app-debug.apk`)
- [ ] Vérifier sur device : chiffrement backup (export + réimport avec passphrase), swipe-to-delete journal, undo rechute, Crisis screen (dial 3114), suppression addiction
- [ ] Regénérer keystore avec `storePassword ≠ keyPassword` avant publication + déplacer `.jks` hors dossier projet vers `~/.android/keystores/`
- [ ] Build release signé (`./gradlew assembleRelease`) — vérifier taille avec R8
- [ ] Publication Play Store : screenshots, store listing FR/EN, politique de confidentialité
- [ ] **v0.3 features possibles** : auto-backup WorkManager (nécessite décision sur stockage passphrase), Vico 2.0.0 stable dès sortie, remontée des raisons perso sur HomeScreen

## 📋 Notes / gotchas
- **Migration DB v1→v2** : `fallbackToDestructiveMigration()` actif — passage à SQLCipher réinitialise la DB de toute façon (bad-magic sur ancien fichier plaintext). Les utilisateurs v0.1 doivent réonboarder OU utiliser un backup JSON legacy (pas chiffré) exporté avant la mise à jour. À documenter dans le changelog.
- **BackupRepository.import** est backward-compat : détecte le magic `CLAIRJOUR1` et fallback sur JSON legacy si absent — permet la migration en douceur des rares testeurs v0.1.
- **Passphrase backup** : jamais stockée. Le user doit la retenir. C'est un choix conscient (sécurité > praticité). Une auto-backup nécessiterait de la stocker dans le Keystore Android, ce qui serait à trancher.
- **SQLCipher** : `System.loadLibrary("sqlcipher")` chargé une seule fois via `ensureNativeLibs()`. La lib pèse ~15 Mo (raison de la taille APK). Alternative future : réduire via ABI splits.
- **`allowBackup=false`** : bloque le cloud-backup Google et le device-transfer. Les données restent locales — la seule copie externe possible est un backup manuel chiffré.
- **Keystore** : commentaire ajouté dans `keystore.properties`. En prod, passer par variables d'env `CLAIRJOUR_STORE_PASSWORD` / `CLAIRJOUR_KEY_PASSWORD`. Actuellement `storePassword == keyPassword == Cl4irj0ur!2026` — à changer lors de la génération du nouveau keystore.
- **CrisisScreen numbers** : 3114 (prévention suicide FR) + 0800235236 (Drogues Info Service FR). Adapter si i18n étendue.
- **compileSdk = 36** (imposé par androidx.core 1.15 + work 2.10)
- **JDK 21** bundled Android Studio : `C:\Program Files\Android\Android Studio\jbr\` (exporter `JAVA_HOME` pour CLI)
- **Chemins APK** : `app/build/outputs/apk/debug/app-debug.apk` (~46 Mo) et `app/build/outputs/apk/release/app-release.apk`

## 🚀 Pour installer sur téléphone
```powershell
adb install "C:\users\flora\documents\dev\Application\Clair jour\app\build\outputs\apk\debug\app-debug.apk"
```
