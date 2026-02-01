package com.sillytavern.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
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
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

import java.io.IOException;
import java.security.GeneralSecurityException;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;

public class MainActivity extends BridgeActivity {
    private static final int FILE_CHOOSER_RESULT_CODE = 1;
    private ValueCallback<Uri[]> filePathCallback;
    private static final String PREFS_NAME = "SillyTavernPrefs";
    private static final String KEY_AUTH_USER = "auth_user";
    private static final String KEY_AUTH_PASS = "auth_pass";
    private static final String KEY_BACKGROUND_MODE = "background_mode";
    private SwipeRefreshLayout swipeRefreshLayout;

    private SharedPreferences getSafeSharedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));

            return WindowInsetsCompat.CONSUMED;
        });

        // Configure system bars
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(false);
            windowInsetsController.setAppearanceLightNavigationBars(false);
        }

        setupSwipeRefresh();
        setupBackNavigation();
    }

    private void setupSwipeRefresh() {
        // Wrap the WebView in a SwipeRefreshLayout programmatically
        WebView webView = getBridge().getWebView();
        if (webView != null) {
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);

                swipeRefreshLayout = new SwipeRefreshLayout(this);
                swipeRefreshLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                // Add WebView to SwipeRefreshLayout
                swipeRefreshLayout.addView(webView, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                // Re-add SwipeRefreshLayout to the original parent
                parent.addView(swipeRefreshLayout);

                // Configure SwipeRefresh behavior
                swipeRefreshLayout.setOnRefreshListener(() -> {
                    webView.reload();
                    // Stop spinner after a short delay via WebViewClient onPageFinished or manual
                    // timeout
                    // Backup timeout in case onPageFinished doesn't fire
                    new android.os.Handler().postDelayed(() -> {
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 5000);
                });

                // NOTE: We do NOT use webView.setOnScrollChangeListener here anymore.
                // We rely on the injected JS + ScrollBridge to toggle enabled state.
                swipeRefreshLayout.setEnabled(true);
            }
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                WebView webView = getBridge().getWebView();
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    // Standard system back behavior (minimize app)
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get the WebView instance from Capacitor's Bridge
        WebView webView = this.getBridge().getWebView();

        // Add Javascript Interface for Auth, Background & Scroll
        webView.addJavascriptInterface(new AuthBridge(this), "AuthBridge");
        webView.addJavascriptInterface(new BackgroundBridge(this), "BackgroundBridge");
        webView.addJavascriptInterface(new ScrollBridge(), "ScrollBridge");

        // Sync background service state
        syncBackgroundService();

        // Enable Zoom Support
        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);

        // Set custom WebViewClient to handle Basic Auth and Zoom Fix
        webView.setWebViewClient(new BridgeWebViewClient(this.getBridge()) {
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                SharedPreferences prefs = getSafeSharedPreferences(MainActivity.this);
                String user = prefs.getString(KEY_AUTH_USER, null);
                String pass = prefs.getString(KEY_AUTH_PASS, null);

                if (user != null && !user.isEmpty() && pass != null) {
                    handler.proceed(user, pass);
                } else {
                    super.onReceivedHttpAuthRequest(view, handler, host, realm);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                // Inject JS for Zoom Fix and Scroll Handling
                String jsInjection =
                        // 0. CSS Force Zoom: Ensure touch-action allows scaling
                        "var style = document.createElement('style');" +
                                "style.innerHTML = 'html, body { touch-action: pan-x pan-y pinch-zoom !important; }';" +
                                "document.head.appendChild(style);" +

                // 1. Zoom Fix: Enforce viewport meta tag
                                "var enforceViewport = function() {" +
                                "   var meta = document.querySelector('meta[name=\"viewport\"]');" +
                                "   if (!meta) {" +
                                "       meta = document.createElement('meta');" +
                                "       meta.name = 'viewport';" +
                                "       document.head.appendChild(meta);" +
                                "   }" +
                                "   if (!meta.content.includes('user-scalable=yes')) {" +
                                "       meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=10.0, user-scalable=yes';"
                                +
                                "   }" +
                                "};" +
                                "enforceViewport();" +
                                "new MutationObserver(enforceViewport).observe(document.head, { childList: true, subtree: true, attributes: true });"
                                +

                // 2. Scroll Fix: Detect sticky touch start and update native SwipeRefreshLayout
                                "document.addEventListener('touchstart', function(e) {" +
                                "   var el = e.target;" +
                                "   var isScrolledToTop = true;" +
                                "   while (el) {" +
                                "       if (el.scrollHeight > el.clientHeight && el.scrollTop > 0) {" +
                                "           isScrolledToTop = false;" +
                                "           break;" +
                                "       }" +
                // Also check for window/body scroll
                                "       if (el === document.body || el === document.documentElement) {" +
                                "           if (window.scrollY > 0) isScrolledToTop = false;" +
                                "       }" +
                                "       el = el.parentElement;" +
                                "   }" +
                                "   if (window.ScrollBridge) {" +
                                "       window.ScrollBridge.enableSwipeRefresh(isScrolledToTop);" +
                                "   }" +
                                "}, { passive: true });";

                view.evaluateJavascript(jsInjection, null);
            }
        });

        // Set a custom WebChromeClient to handle permission requests and file selection
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                // Automatically grant all permissions (Camera, Microphone, etc.)
                request.grant(request.getResources());
            }

            // For Android 5.0+
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
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

    private void syncBackgroundService() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_BACKGROUND_MODE, false);
        Intent serviceIntent = new Intent(this, KeepAliveService.class);
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else {
            stopService(serviceIntent);
        }
    }

    public class BackgroundBridge {
        Context mContext;

        BackgroundBridge(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void setBackgroundMode(boolean enabled) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_BACKGROUND_MODE, enabled).apply();
            syncBackgroundService();
        }

        @JavascriptInterface
        public boolean isIgnoringBatteryOptimizations() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                return pm.isIgnoringBatteryOptimizations(mContext.getPackageName());
            }
            return true;
        }

        @JavascriptInterface
        public void requestIgnoreBatteryOptimizations() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                mContext.startActivity(intent);
            }
        }
    }

    public class AuthBridge {
        Context mContext;

        AuthBridge(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void setCredentials(String user, String pass) {
            SharedPreferences prefs = getSafeSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_AUTH_USER, user);
            editor.putString(KEY_AUTH_PASS, pass);
            editor.apply();
        }

        @JavascriptInterface
        public void clearCredentials() {
            SharedPreferences prefs = getSafeSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(KEY_AUTH_USER);
            editor.remove(KEY_AUTH_PASS);
            editor.apply();
        }
    }

    // NEW: Bridge to handle scroll state from JS
    public class ScrollBridge {
        @JavascriptInterface
        public void enableSwipeRefresh(boolean enabled) {
            if (swipeRefreshLayout != null) {
                // Must run on UI thread as this touches View properties
                runOnUiThread(() -> swipeRefreshLayout.setEnabled(enabled));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (filePathCallback == null)
                return;
            filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            filePathCallback = null;
        }
    }
}
