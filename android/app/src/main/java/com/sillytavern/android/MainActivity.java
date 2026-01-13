package com.sillytavern.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

public class MainActivity extends BridgeActivity {
    private static final int FILE_CHOOSER_RESULT_CODE = 1;
    private ValueCallback<Uri[]> filePathCallback;
    private static final String PREFS_NAME = "SillyTavernPrefs";
    private static final String KEY_AUTH_USER = "auth_user";
    private static final String KEY_AUTH_PASS = "auth_pass";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display (content behind system bars)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Apply window insets as padding to avoid obstruction and handle keyboard
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            // Apply the insets as padding to the view.
            // We use the maximum of system bars and IME for the bottom to ensure
            // content is pushed up when keyboard appears.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            
            return WindowInsetsCompat.CONSUMED;
        });

        // Configure system bars
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            // Set light status bar icons (false means light icons on dark background)
            windowInsetsController.setAppearanceLightStatusBars(false);
            // Set light navigation bar icons
            windowInsetsController.setAppearanceLightNavigationBars(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get the WebView instance from Capacitor's Bridge
        WebView webView = this.getBridge().getWebView();

        // Add Javascript Interface for Auth
        webView.addJavascriptInterface(new AuthBridge(this), "AuthBridge");

        // Enable Zoom Support
        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // Set custom WebViewClient to handle Basic Auth
        webView.setWebViewClient(new BridgeWebViewClient(this.getBridge()) {
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String user = prefs.getString(KEY_AUTH_USER, null);
                String pass = prefs.getString(KEY_AUTH_PASS, null);

                if (user != null && !user.isEmpty() && pass != null) {
                    handler.proceed(user, pass);
                } else {
                    super.onReceivedHttpAuthRequest(view, handler, host, realm);
                }
            }
        });

        // Set a custom WebChromeClient to handle permission requests and file selection
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                // Automatically grant all permissions (Camera, Microphone, etc.)
                // In a production app, you might want to check specific permissions
                request.grant(request.getResources());
            }

            // For Android 5.0+
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE);
                } catch (Exception e) {
                    MainActivity.this.filePathCallback = null;
                    return false;
                }
                return true;
            }
        });
    }

    public class AuthBridge {
        Context mContext;

        AuthBridge(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void setCredentials(String user, String pass) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_AUTH_USER, user);
            editor.putString(KEY_AUTH_PASS, pass);
            editor.apply();
        }

        @JavascriptInterface
        public void clearCredentials() {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(KEY_AUTH_USER);
            editor.remove(KEY_AUTH_PASS);
            editor.apply();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (filePathCallback == null) return;
            filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            filePathCallback = null;
        }
    }
}
