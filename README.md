# AstraOS

AstraOS is a desktop-style Android environment for Android 11 Google TV (32-bit armeabi-v7a). It is an original project, visually inspired by desktop operating systems, not a Windows clone.

## Included in this prototype

- First-run setup: username, optional password, user icon, wallpaper, light/dark preference and clock preference
- Optional password lock screen
- Desktop with persistent shortcuts and AstraMenu
- Right-click / long-press app context menu: Add to Home, Launch, App info
- Overlays internal windows: move, minimize, maximize/restore, close, multiple windows, stacking
- Astra Settings: wallpaper, username, icon, password, theme, clock, desktop icon and sound preferences
- Google TV / Android Settings shortcut
- File picker: TXT, HTML, images, ZIP and APK hand-off to Android; EXE warning
- ZIP extraction to AstraOS app-specific storage with path traversal protection
- Notepad with locally saved text
- Calculator
- Photos image viewer
- Task Manager controls for Overlays windows
- Built-in browser starting at YouTube

## Important limitations

This is a functional prototype, not a complete operating system. Browser playback and YouTube sign-in/playback depend on the Google TV WebView, DRM support, YouTube policies and device firmware. The browser is not a full desktop browser. APK installation always depends on Android's own confirmation and security settings. Windows EXE files cannot run directly on Android. Files are limited to those the user explicitly selects through Android's document picker. The sound preference is stored but system sound effects are not implemented yet. The clock display uses the device's current local time; custom time-zone/date formats are not yet configurable.

Overlays is an in-app window manager within one Android Activity; it does not create native Android freeform windows or run other Android apps inside AstraOS windows.

## Build

GitHub Actions builds a debug APK for armeabi-v7a. Open the Actions tab, select the latest successful workflow run, and download the `AstraOS-debug` artifact. Install the APK on the target device and test it there.

Target: Android 11 / minSdk 30 / targetSdk 32 / armeabi-v7a.
