@echo off
echo ========================================
echo Quiz Attempt Debug Logger
echo ========================================
echo.
echo This will capture logs while you complete a quiz.
echo.
echo Instructions:
echo 1. Install the updated APK first if you haven't
echo 2. Open the app and sign in
echo 3. Press ENTER to start logging
echo 4. Complete a quiz in the app
echo 5. Press Ctrl+C to stop logging
echo.
pause

echo.
echo Starting log capture...
echo Filtering for: QuizViewModel, QuizRemoteRepo, AuthViewModel, UserSyncManager, OkHttp
echo.
echo ========================================
echo LOGS START
echo ========================================
echo.

adb logcat -c
adb logcat -s QuizViewModel:* QuizRemoteRepo:* AuthViewModel:* UserSyncManager:* OkHttp:*

echo.
echo ========================================
echo LOGS END
echo ========================================
