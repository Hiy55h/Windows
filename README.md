# AstraOS

AstraOS is an Android-based desktop-style environment designed for Android 11 Google TV devices. Its visual style is inspired by modern desktop operating systems, but AstraOS is its own project and is not a Windows clone.

## Core experience

- First-run setup wizard
- Lock screen and user sign-in
- Desktop with wallpaper and app shortcuts
- AstraMenu app launcher
- Internal window manager named **Overlays**
- Built-in apps that open in movable windows

## First-run setup

On the first launch, guide the user through:

1. Welcome screen
2. PC/user name
3. Password setup
4. User icon selection
5. Wallpaper selection
6. Theme selection
7. Date/time display preferences
8. Finish and open the desktop

Save setup choices locally so the wizard does not appear on every launch. Provide a way to change these choices later in Astra Settings.

## AstraMenu

- Show the installed/built-in app list.
- Right-clicking an app opens its context menu.
- The context menu includes **Add to Home**.
- Choosing **Add to Home** adds a desktop shortcut for that app.
- Desktop shortcuts remain after restarting AstraOS.
- Apps can also be launched from their desktop shortcuts.

## Overlays window system

Overlays is AstraOS's internal window manager. Built-in apps open as windows inside the AstraOS desktop.

Window features:

- Move windows
- Minimize
- Maximize/restore
- Close
- Multiple windows at once
- Change window stacking order

Initial built-in apps:

- File Manager
- Astra Settings
- Google TV Settings shortcut
- Notepad
- Calculator
- Photos
- Task Manager

The initial implementation should use AstraOS-managed windows inside one Android Activity. It should not depend on Android 11 providing native desktop/freeform windows.

## Settings

### Astra Settings

- Change wallpaper
- Change user name
- Change password
- Change user icon
- Light/dark theme
- Desktop icon preferences
- Date/time display preferences
- System sound toggle
- Installed app management

### Google TV Settings

Provide a shortcut that attempts to open the device's native Google TV / Android Settings screen. Availability depends on the device firmware and Android permissions.

## File Manager

- Browse files that Android grants AstraOS permission to access.
- Open supported files with built-in apps.
- Display file information.
- Extract ZIP archives.
- Detect EXE files and explain that Windows EXE programs cannot run directly on Android.

Supported initial file types:

| Type | Initial behavior |
| --- | --- |
| TXT | Open in Notepad |
| HTML | Open in built-in browser/viewer |
| PNG / JPG | Open in Photos |
| ZIP | Extract archive |
| APK | Show Android package information; installation requires Android's normal install flow and user confirmation |
| EXE | Show an unsupported Windows executable message |

A Windows EXE file is not directly executable on Android. Do not present EXE support as available unless a separate compatible runtime is implemented and tested.

## Technical direction

- Target OS: Android 11
- Target device class: Google TV
- Target ABI: armeabi-v7a (32-bit)
- Input: keyboard and mouse
- Touch-first controls and TV-remote-specific focus navigation are not required for the initial version.
- Use local persistent storage for the user profile, setup completion state, wallpaper/theme preferences, and desktop shortcuts.

## Development status

An initial Android Studio project and GitHub Actions debug-APK workflow are now included. The current prototype has a first-run setup wizard, optional password lock screen, desktop, AstraMenu app list with long-press/right-click context actions, persistent Add to Home shortcuts, internal Overlays windows, Astra Settings wallpaper/theme/user-name controls, a Google TV Settings shortcut, a basic file picker/text viewer, Notepad, and Calculator.\n\nThis is an early prototype, not a finished OS. The APK must still complete a successful GitHub Actions build and be tested on the target Google TV device. Some planned features (full file browsing, ZIP extraction, image viewing, persistent notes, richer settings, and YouTube/browser app integration) are not implemented yet.
