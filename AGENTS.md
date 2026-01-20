# Agent Guide: SillyTavern-Android

This repository contains a lightweight Android launcher for SillyTavern using Capacitor. It provides a full-screen WebView with native support for file uploads, permissions, and HTTP Basic Auth.

## Build and Development Commands

### Native (Android) Commands
- **Build APK:** `./gradlew assembleDebug` (run in `android/` directory)
- **Sync Web Assets:** `npx cap sync android`
- **Open in Android Studio:** `npx cap open android`
- **Run on Device:** `./gradlew installDebug`

### Web Assets
- **Location:** `www/` directory (`index.html`, `app.js`, `style.css`).
- **Update WebView:** After modifying files in `www/`, run `npx cap copy android`.

### Lint & Test
- **Test:** No automated tests currently implemented (check `package.json` scripts).
- **Lint:** No linter configured; follow existing styles.

## Code Style Guidelines

### Java (Android Native)
- **Location:** `android/app/src/main/java/com/sillytavern/android/MainActivity.java`
- **Naming:** CamelCase for classes (`MainActivity`), camelCase for methods (`onCreate`).
- **Dependencies:** Uses Capacitor `BridgeActivity` and standard Android `WebView` components.
- **Error Handling:** Use `try-catch` blocks for Intent starts or file operations.
- **Style:** 4-space indentation. Keep logic inside `MainActivity` or dedicated bridge classes.

### JavaScript (Web)
- **Location:** `www/app.js`
- **Naming:** CONSTANT_CASE for keys, camelCase for variables/functions.
- **Imports:** None (vanilla JS). Code is directly included in `index.html`.
- **UI Interaction:** Use `document.getElementById` for element selection.
- **Storage:** Use `localStorage` for app-specific settings (URL, auth flags).
- **Bridges:** Use `window.AuthBridge` to communicate with the native side. Always check for existence before calling.

### CSS
- **Location:** `www/style.css`
- **Style:** Modern CSS with Flexbox/Grid. Use classes for visibility (`.hidden`).

### Basic Auth Flow
Native side handles Basic Auth via `onReceivedHttpAuthRequest`. JS saves credentials via `AuthBridge.setCredentials()`. Native stores them in `SharedPreferences`.

## Rules & Constraints
- **Capacitor 8.0+:** Ensure compatibility with Capacitor 8.
- **Node.js:** >=22.0.0.
- **WebView:** Must support `http` cleartext (configured in `capacitor.config.json`).
- **Permissions:** Native side grants all requested permissions (Camera/Microphone) automatically for ST features.
