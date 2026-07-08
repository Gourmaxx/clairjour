# Graph Report - .  (2026-07-06)

## Corpus Check
- Corpus is ~19,144 words - fits in a single context window. You may not need a graph.

## Summary
- 467 nodes · 684 edges · 33 communities (29 shown, 4 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 44 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_App Init & Navigation|App Init & Navigation]]
- [[_COMMUNITY_Settings & Locale|Settings & Locale]]
- [[_COMMUNITY_Design & Architecture Docs|Design & Architecture Docs]]
- [[_COMMUNITY_Addiction DAO & Repository|Addiction DAO & Repository]]
- [[_COMMUNITY_Journal DAO & Repository|Journal DAO & Repository]]
- [[_COMMUNITY_Addiction Detail & Edit UI|Addiction Detail & Edit UI]]
- [[_COMMUNITY_Pledge DAO & Repository|Pledge DAO & Repository]]
- [[_COMMUNITY_UI Components Library|UI Components Library]]
- [[_COMMUNITY_Room Database Core|Room Database Core]]
- [[_COMMUNITY_Relapse DAO & Repository|Relapse DAO & Repository]]
- [[_COMMUNITY_Journal Editor UI|Journal Editor UI]]
- [[_COMMUNITY_MainActivity & Theme|MainActivity & Theme]]
- [[_COMMUNITY_Room Type Converters|Room Type Converters]]
- [[_COMMUNITY_Settings ViewModel|Settings ViewModel]]
- [[_COMMUNITY_Stats Screen|Stats Screen]]
- [[_COMMUNITY_Reminder Scheduler|Reminder Scheduler]]
- [[_COMMUNITY_Notification Workers|Notification Workers]]
- [[_COMMUNITY_Motivations Repository|Motivations Repository]]
- [[_COMMUNITY_Milestones Domain Logic|Milestones Domain Logic]]
- [[_COMMUNITY_Home Widget (Glance)|Home Widget (Glance)]]
- [[_COMMUNITY_Formatters & Utilities|Formatters & Utilities]]
- [[_COMMUNITY_Converters Tests|Converters Tests]]
- [[_COMMUNITY_Milestones Tests|Milestones Tests]]
- [[_COMMUNITY_Streak Calculator|Streak Calculator]]
- [[_COMMUNITY_Navigation Routes|Navigation Routes]]
- [[_COMMUNITY_Notification Channels|Notification Channels]]
- [[_COMMUNITY_Widget Receiver|Widget Receiver]]
- [[_COMMUNITY_Gradle Wrapper Script|Gradle Wrapper Script]]

## God Nodes (most connected - your core abstractions)
1. `Clairjour — Design Document` - 21 edges
2. `AppContainer` - 19 edges
3. `SettingsRepository` - 15 edges
4. `AddictionEntity` - 14 edges
5. `ClairjourNavHost()` - 14 edges
6. `AddictionEditViewModel` - 13 edges
7. `HomeScreen()` - 13 edges
8. `SettingsViewModel` - 13 edges
9. `AddictionDao` - 12 edges
10. `JournalEntryEntity` - 11 edges

## Surprising Connections (you probably didn't know these)
- `Cap — Alternative Branding Direction` --semantically_similar_to--> `Clairjour Visual Theme (deep blue + gold)`  [INFERRED] [semantically similar]
  mockups/index.html → docs/DESIGN.md
- `Cordée — Alternative Branding Direction` --semantically_similar_to--> `Clairjour Visual Theme (deep blue + gold)`  [INFERRED] [semantically similar]
  mockups/index.html → docs/DESIGN.md
- `Clairjour — SUIVI.md (Project Tracking)` --references--> `Clean Architecture + MVVM + Hilt`  [INFERRED]
  SUIVI.md → docs/DESIGN.md
- `Clairjour — Design Document` --references--> `Clairjour Android Sobriety App`  [EXTRACTED]
  docs/DESIGN.md → SUIVI.md
- `StatSmallCard()` --calls--> `ClairjourCard()`  [INFERRED]
  app/src/main/kotlin/com/clairjour/app/ui/screen/addiction/AddictionDetailScreen.kt → app/src/main/kotlin/com/clairjour/app/ui/components/ClairjourComponents.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Clairjour Data Layer — Room + DataStore + Entities** — room_db, datastore_settings, entity_addiction, entity_journal_entry, entity_pledge [INFERRED 0.95]
- **Clairjour Branding Direction Selection** — mockups_index, mockups_logos, clairjour_theme [EXTRACTED 1.00]
- **Clairjour Background Services — WorkManager + Widget + Backup** — workmanager_notifications, glance_widget, google_drive_backup [INFERRED 0.85]

## Communities (33 total, 4 thin omitted)

### Community 0 - "App Init & Navigation"
Cohesion: 0.07
Nodes (32): ClairjourApplication, AppContainer, DefaultAppContainer, BottomTab, ClairjourApp(), viewModelFactoryOf(), ClairjourNavHost(), PaddingValues (+24 more)

### Community 1 - "Settings & Locale"
Cohesion: 0.07
Nodes (23): LocaleManager, AppLanguage, Keys, Boolean, Flow, Int, SettingsRepository, ThemeMode (+15 more)

### Community 2 - "Design & Architecture Docs"
Cohesion: 0.09
Nodes (38): Debug APK Build (app-debug.apk, ~23.8 MB), Clairjour Android Sobriety App, Clairjour Visual Theme (deep blue + gold), Clean Architecture + MVVM + Hilt, DataStore SettingsRepository, Clairjour — Design Document, Addiction Entity (Room), JournalEntry Entity (Room) (+30 more)

### Community 3 - "Addiction DAO & Repository"
Cohesion: 0.10
Nodes (13): AddictionDao, Flow, Instant, List, String, AddictionEntity, AddictionRepository, Boolean (+5 more)

### Community 4 - "Journal DAO & Repository"
Cohesion: 0.12
Nodes (13): JournalDao, Flow, List, LocalDate, String, JournalEntryEntity, JournalRepository, Boolean (+5 more)

### Community 5 - "Addiction Detail & Edit UI"
Cohesion: 0.10
Nodes (14): AddictionDetailUiState, AddictionDetailViewModel, AddictionEditUiState, AddictionEditViewModel, Boolean, Instant, StateFlow, String (+6 more)

### Community 6 - "Pledge DAO & Repository"
Cohesion: 0.13
Nodes (11): Flow, Int, LocalDate, String, PledgeDao, PledgeEntity, Flow, Int (+3 more)

### Community 7 - "UI Components Library"
Cohesion: 0.16
Nodes (19): ClairjourBrandBadge(), ClairjourCard(), Float, Modifier, PaddingValues, String, MilestoneProgress(), ChronoSeparator() (+11 more)

### Community 8 - "Room Database Core"
Cohesion: 0.13
Nodes (10): ClairjourDatabase, get(), Context, Flow, Int, List, String, MilestoneDao (+2 more)

### Community 9 - "Relapse DAO & Repository"
Cohesion: 0.15
Nodes (9): Flow, List, String, RelapseDao, RelapseEventEntity, Flow, List, String (+1 more)

### Community 10 - "Journal Editor UI"
Cohesion: 0.17
Nodes (8): EditorUiState, JournalEditorViewModel, JournalListUiState, JournalViewModel, Boolean, Int, StateFlow, String

### Community 11 - "MainActivity & Theme"
Cohesion: 0.14
Nodes (11): MainActivity, ClairjourTheme(), contentColorFor(), LocaleContextWrapper, WithLocale(), Bundle, Color, ComponentActivity (+3 more)

### Community 12 - "Room Type Converters"
Cohesion: 0.24
Nodes (6): Converters, Instant, List, LocalDate, String, Long

### Community 13 - "Settings ViewModel"
Cohesion: 0.21
Nodes (6): Boolean, Int, StateFlow, String, SettingsUiState, SettingsViewModel

### Community 14 - "Stats Screen"
Cohesion: 0.19
Nodes (11): Int, List, Modifier, PaddingValues, String, MoodChart(), StatCard(), StatsScreen() (+3 more)

### Community 15 - "Reminder Scheduler"
Cohesion: 0.38
Nodes (4): Context, Int, ReminderScheduler, Duration

### Community 16 - "Notification Workers"
Cohesion: 0.25
Nodes (8): JournalReminderWorker, Context, Int, String, PledgeReminderWorker, showReminder(), CoroutineWorker, Result

### Community 17 - "Motivations Repository"
Cohesion: 0.31
Nodes (4): String, Motivation, List, MotivationsRepository

### Community 18 - "Milestones Domain Logic"
Cohesion: 0.39
Nodes (5): Float, Int, List, Milestone, Milestones

### Community 19 - "Home Widget (Glance)"
Cohesion: 0.25
Nodes (6): CounterWidget, Context, Int, String, GlanceAppWidget, GlanceId

### Community 20 - "Formatters & Utilities"
Cohesion: 0.32
Nodes (5): Formatters, Context, Double, LocalDate, String

### Community 23 - "Streak Calculator"
Cohesion: 0.33
Nodes (4): Instant, Int, LocalDate, Streak

### Community 26 - "Widget Receiver"
Cohesion: 0.50
Nodes (3): CounterWidgetReceiver, GlanceAppWidget, GlanceAppWidgetReceiver

### Community 27 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **5 isolated node(s):** `Keys`, `BottomTab`, `Glance Widget (day counter)`, `Onboarding Screen (4 steps)`, `Debug APK Build (app-debug.apk, ~23.8 MB)`
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AppContainer` connect `App Init & Navigation` to `Settings & Locale`, `Addiction DAO & Repository`, `Journal DAO & Repository`, `Pledge DAO & Repository`, `UI Components Library`, `Relapse DAO & Repository`, `Stats Screen`, `Motivations Repository`?**
  _High betweenness centrality (0.267) - this node is a cross-community bridge._
- **Why does `HomeScreen()` connect `UI Components Library` to `App Init & Navigation`, `Addiction Detail & Edit UI`?**
  _High betweenness centrality (0.077) - this node is a cross-community bridge._
- **Why does `AddictionEntity` connect `Addiction DAO & Repository` to `UI Components Library`?**
  _High betweenness centrality (0.074) - this node is a cross-community bridge._
- **Are the 9 inferred relationships involving `ClairjourNavHost()` (e.g. with `ClairjourApp()` and `AddictionDetailScreen()`) actually correct?**
  _`ClairjourNavHost()` has 9 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `BottomTab`, `Glance Widget (day counter)` to the rest of the system?**
  _11 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `App Init & Navigation` be split into smaller, more focused modules?**
  _Cohesion score 0.06976744186046512 - nodes in this community are weakly interconnected._
- **Should `Settings & Locale` be split into smaller, more focused modules?**
  _Cohesion score 0.07195121951219512 - nodes in this community are weakly interconnected._