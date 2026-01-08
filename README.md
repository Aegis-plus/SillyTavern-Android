# SillyTavern Android Launcher

A lightweight Android wrapper for [SillyTavern](https://github.com/SillyTavern/SillyTavern). This application provides a dedicated WebView environment to access your self-hosted SillyTavern instance with native-like features.

## ✨ Features

-   **Full-Screen Experience:** Removes browser UI elements for an immersive chat experience.
-   **Native Permissions:** Seamless support for file uploads (images, characters) and camera/microphone access for multimodal features.
-   **Persistent Settings:** Save your instance URL and connect with a single tap.
-   **Lightweight:** Minimal overhead compared to running a full mobile browser.

## 🚀 Getting Started

### Download
The easiest way to get started is to download the latest pre-built APK from the [Releases](../../releases) page.

### Installation
1.  Download the `SillyTavern-vX.X.X.apk` file.
2.  Open the file on your Android device.
3.  If prompted, allow installation from unknown sources.
4.  Follow the on-screen instructions to complete the installation.

## 🛠️ Setup & Usage

1.  **Launch the App:** Open SillyTavern from your app drawer.
2.  **Configure URL:**
    -   Tap the **Settings** button.
    -   Enter the full URL of your SillyTavern instance (e.g., `http://192.168.1.5:8000` or your public tunnel URL).
    -   *Note: Ensure your phone can reach the host (e.g., same Wi-Fi or public IP).*
3.  **Save & Connect:**
    -   Tap **Save** to store the configuration.
    -   Tap **Connect** to load your SillyTavern interface.

## 🏗️ Building from Source

You can build the APK yourself using GitHub Actions or locally.

### Using GitHub Actions (No Android Studio Required)
1.  **Fork** this repository.
2.  Enable **Actions** in your fork's settings.
3.  The workflow will trigger on every push, or you can run it manually via the **Actions** tab.
4.  Once complete, download the **SillyTavern-Release** artifact from the workflow run summary.

### Local Development
This project is built with [Capacitor](https://capacitorjs.com/).

-   **Requirements:** Node.js (>=22.0.0), Android Studio, and JDK 21.
-   **Structure:**
    -   [`www/`](www/): Contains the launcher's configuration UI.
    -   [`android/`](android/): The native Android project.
    -   [`MainActivity.java`](android/app/src/main/java/com/sillytavern/android/MainActivity.java): Handles WebView permissions and core logic.

```bash
# Install dependencies
npm install

# Sync changes to the Android project
npx cap sync android
```

### Signing the App

To create a distributable (signed) APK, you need a keystore.

1.  **Generate a Keystore:**
    ```bash
    keytool -genkey -v -keystore release-key.keystore -alias sillytavern -keyalg RSA -keysize 2048 -validity 10000
    ```
2.  **Configure Environment Variables:**
    The build script expects the following environment variables:
    - `RELEASE_STORE_FILE`: Path to your keystore file (defaults to `release-key.keystore` in `android/app`).
    - `RELEASE_STORE_PASSWORD`: Your keystore password.
    - `RELEASE_KEY_ALIAS`: Your key alias (e.g., `sillytavern`).
    - `RELEASE_KEY_PASSWORD`: Your key password.

3.  **GitHub Actions Setup:**
    Add these as **Secrets** in your GitHub repository settings to enable signed releases via the automated workflow.

## 📄 License

This project is licensed under the [ISC License](LICENSE).
