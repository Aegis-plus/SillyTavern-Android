# SillyTavern Android Launcher

This is a simple Android launcher for [SillyTavern](https://github.com/SillyTavern/SillyTavern). It wraps your self-hosted SillyTavern instance in a WebView, allowing for:

-   **Full Screen Experience**
-   **File Uploads** (Images, etc.)
-   **Camera & Microphone Access** (for multimodal features)

## Download

You can download the latest APK from the [Releases](../../releases) page.

## How to Build (No Android Studio Required)

We use **GitHub Actions** to build the APK automatically.

### Option 1: Download from Releases (Recommended)
1.  Go to the [Releases](../../releases) page.
2.  Download the latest `SillyTavern-vX.X.X.apk`.
3.  Install it on your Android device.

### Option 2: Build Manually via GitHub Actions
1.  **Fork or Push** this repository to your own GitHub account.
2.  Go to the **Actions** tab in your repository.
3.  You should see a workflow named "Build Android APK" running (or you can trigger it manually).
4.  Once the build finishes (green checkmark), click on the workflow run.
5.  Scroll down to the **Artifacts** section.
6.  Download **SillyTavern-Debug**.
7.  Unzip the file to find `app-debug.apk`.
8.  Transfer this APK to your Android phone and install it.

## Setup on Phone

1.  Open the App.
2.  Click **Settings**.
3.  Enter the URL of your SillyTavern instance (e.g., `http://192.168.1.5:8000` or your ngrok/Cloudflare URL).
    *   *Note: If using a local IP, make sure your phone is on the same Wi-Fi network.*
4.  Click **Save**.
5.  Click **Connect**.

## Development

This project uses [Capacitor](https://capacitorjs.com/).

-   `www/` contains the launcher UI (HTML/CSS/JS).
-   `android/` contains the native Android project.
-   `MainActivity.java` handles the WebView permissions.
