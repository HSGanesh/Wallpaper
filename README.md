# Chalkboard Clock — Live Wallpaper

A dark blackboard live wallpaper for Android, densely covered in
hand-drawn-style math and physics equations, diagrams and chalk texture —
with a **live, ticking clock** rendered in a chalk handwriting font in the
center, showing the real device time and updating every second.

## How it's built

- Pure Android (Kotlin), no third-party wallpaper libraries.
- `ChalkboardWallpaperService` (`WallpaperService.Engine`) renders:
  - A static cached layer (board background, chalk dust texture, ~30
    formulas, and 5 hand-sketched diagrams: sine wave, atom, magnetic
    field, circuit, sphere) — built once per screen size for efficiency.
  - A dynamic layer redrawn once per second: the live `HH:mm` clock, the
    date, and a rotating motivational caption — composited on top of the
    cached static layer, so battery/CPU cost stays low.
  - Subtle horizontal parallax as you swipe between home screens.
- `MainActivity` shows a preview and a "Set as Live Wallpaper" button that
  opens Android's live wallpaper picker directly on this wallpaper.
- Fonts: [Shadows Into Light](https://fonts.google.com/specimen/Shadows+Into+Light)
  for the clock, [Reenie Beanie](https://fonts.google.com/specimen/Reenie+Beanie)
  for the formulas — both SIL Open Font License 1.1 (see `/licenses`).

## Building the APK

This repo builds automatically via **GitHub Actions** on every push to
`main` (see `.github/workflows/build-apk.yml`). It:

1. Compiles a debug APK with the Android Gradle Plugin + Gradle 8.7 / JDK 17.
2. Uploads it as a workflow artifact.
3. Publishes it as a **GitHub Release** asset for direct download.

To build locally instead: open the project in Android Studio (Koala or
newer) and use **Build → Build APK(s)**, or run:

```
./gradlew assembleDebug
```

The output APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Installing on your phone

1. Download `app-debug.apk` from the latest GitHub Release.
2. Open it on your phone (via Files app, browser downloads, etc.).
3. If prompted, allow "install unknown apps" for the app you used to open
   the file — this is expected for any APK installed outside the Play
   Store.
4. Open the app, tap **Set as Live Wallpaper**, and confirm.
