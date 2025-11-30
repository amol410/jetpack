GPT Notes – Jetpack Project
===========================

Purpose
- Compose Android quiz/notes app with Firebase Auth/Analytics/Crashlytics/FCM + AdMob; PHP backend provides content and quiz sync.

Architecture
- Entry: app/src/main/java/com/dolphin/jetpack/MainActivity.kt wiring Auth -> main pager (Notes, Quizzes, History, Statistics) with drawer, theme via util/ThemePreferences.
- ViewModels provided via AppModule (Room + Retrofit): QuizViewModel, QuizListViewModel, NotesViewModel, HistoryViewModel, StatisticsViewModel, AuthViewModel.
- Data: Room DB (QuizAttempt/QuestionAnswer/QuizState/Quiz/Chapter/Note) with converters. ContentRepository for chapters/notes/quizzes (network + offline cache). QuizRepositoryImpl handles attempts/state/stats/export + remote sync via QuizRemoteRepositoryImpl (Retrofit to PHP endpoints).
- Networking: RetrofitClient adds X-API-Key header (ApiConfig). Models in data/remote/ApiModels.kt; NetworkResult wrapper.
- UI screens in presentation/screens: Login/Email auth, QuizSelection (offline toggle, resume), QuizInProgress (timer + autosave), QuizResult, History + Detail (backend merge), Statistics (charts via MPAndroidChart wrapper in presentation/components/Chart.kt), Notes/LessonNotes with HTML render, Settings, Material3 showcase.
- Ads: ads/AdManager.kt and InterstitialAdManager.kt for AdMob interstitials.
- Utilities: AnalyticsHelper (Firebase events), ThemePreferences (DataStore), FCMTokenManager + QuizFCMService.

Backend folder
- backend/api PHP endpoints matching Retrofit: get_chapters, get_topic, get_notes, get_quizzes, get_quiz, save_quiz_attempt, get_user_quiz_attempts, get_quiz_attempt_detail, get_user_statistics, session/user register. SQL setup/migration scripts and admin guides included.

Risk/cleanup notes
- Several files contain garbled log strings (likely copy/paste corruption): data/remote/QuizRemoteRepositoryImpl.kt, data/remote/UserSyncManager.kt, presentation/screens/QuizResultScreen.kt, presentation/viewmodel/HistoryViewModel.kt.
- app/build.gradle.kts VerifyProguardRulesTask has malformed println ("?" ...), could break build.
- Hardcoded IDs/keys: ApiConfig.BASE_URL/API_KEY, AdMob app/unit IDs in Manifest and ads/AdManager.kt, Firebase config in google-services.json.
- AdMob interstitial shows after every threshold (COMPONENT_VIEW_THRESHOLD=3) with no frequency cap; ensure UX/Store compliance.
- Remote sync uses fallback to local; network errors surfaced via optimistic UI.

Quick file map
- Main shell: MainActivity.kt
- DI/VM factory: AppModule.kt
- Data: data/local/*, data/remote/*, data/repository/*
- Domain models: domain/model/Models.kt
- ViewModels: presentation/viewmodel/*
- Screens: presentation/screens/*
- Ads: ads/*
- Backend: backend/api/*.php, backend/sql scripts.

Next steps (if resuming later)
- Fix corrupted strings and gradle println.
- Consider moving secrets/IDs to build configs.
- Add/verify tests for repositories and resume-state persistence.
