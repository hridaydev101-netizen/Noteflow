# NoteFlow — Android tablet notes app

This is a native Android Studio project built with Kotlin and Jetpack Compose.

## Included in this starter build
- Notes list with create, open, and delete
- Editable note title and typed text
- Finger/stylus drawing canvas
- Pen, highlighter, eraser tool selection
- Ink colours and stroke width control
- Ruled, grid, and blank paper backgrounds
- Undo for recent strokes
- Light/dark theme toggle
- Local autosave using Android SharedPreferences

## Build an APK on Windows
1. Install **Android Studio** from https://developer.android.com/studio
2. Open Android Studio and choose **Open**.
3. Select the `NoteFlow` folder (the folder containing `settings.gradle.kts`).
4. Allow Gradle sync to finish. Install Android SDK Platform 35 if prompted.
5. Choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. The debug APK will be at:
   `app/build/outputs/apk/debug/app-debug.apk`
7. Copy the APK to your tablet and install it. If Android asks, allow installation from that source.

## Notes
- This project targets Android 8.0 (API 26) and newer.
- Drawing accepts touch and stylus pointer input through Compose pointer gestures.
- This is a starter version. PDF export, folders, search, image insertion, pressure-sensitive brushes, and robust multi-page notebooks are not implemented yet.
- To create a signed release APK, use **Build → Generate Signed Bundle / APK** in Android Studio.
