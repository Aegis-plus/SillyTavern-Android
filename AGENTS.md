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

### Security
- **Storage:** Uses `EncryptedSharedPreferences` (AES256_SIV/AES256_GCM) for storing Basic Auth credentials. Falls back to standard `SharedPreferences` if keystore access fails.

### Top Bar Interface
- **Layout:** Native `LinearLayout` (Horizontal) anchored to the top of the screen (height: 48dp).
- **Refresh Button:** Minimalist button ("R") to reload the current page.
- **Zoom Slider:** `SeekBar` control allowing user to scale the UI from 50% to 200%.
    - **Persistence:** Zoom level is saved in `EncryptedSharedPreferences` (`sillytavern_zoom_level`).
    - **Logic:** Injects `document.body.style.zoom` to scale web content dynamically.
- **Swipe Refresh:** DISABLED/REMOVED. Interaction is now button-based only.

### Back Navigation Flow
- **Logic:** `handleOnBackPressed` intercepts back gestures.
- **Behavior:**
    1. Checks WebView history. If present, goes back.
    2. If no history, checks current URL.
    3. If not at Root (`/`), navigates to Root using `uri.getAuthority()` to preserve custom ports.
    4. If at Root, exits application.

### Zoom Support
- **Native:** Configured with `setSupportZoom(true)` and `setBuiltInZoomControls(true)`.
- **Injection:** Injects Javascript to enforce `user-scalable=yes` in the viewport meta tag and CSS `touch-action` rules.
- **Top Bar Control:** The slider provides the primary user-facing zoom control.

## Rules & Constraints
- **Capacitor 8.0+:** Ensure compatibility with Capacitor 8.
- **Node.js:** >=22.0.0.
- **WebView:** Must support `http` cleartext (configured in `capacitor.config.json`).
- **Permissions:**
    - `INTERNET`: Network access.
    - `CAMERA`, `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`: For WebRTC/media features.
    - `READ/WRITE_EXTERNAL_STORAGE`: For file uploads (legacy).
    - `READ_MEDIA_IMAGES/VIDEO/AUDIO`: For file uploads (Android 13+).
    - `FOREGROUND_SERVICE`: For `KeepAliveService`.
    - `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: For background endurance.
