const STORAGE_KEY = 'sillytavern_url';
const AUTH_ENABLED_KEY = 'sillytavern_auth_enabled';
const AUTH_USER_KEY = 'sillytavern_auth_user';
const AUTH_PASS_KEY = 'sillytavern_auth_pass';

// Elements
const mainScreen = document.getElementById('main-screen');
const settingsScreen = document.getElementById('settings-screen');
const currentUrlDisplay = document.getElementById('current-url-display');
const urlInput = document.getElementById('url-input');
const authToggle = document.getElementById('auth-toggle');
const authFields = document.getElementById('auth-fields');
const authUser = document.getElementById('auth-user');
const authPass = document.getElementById('auth-pass');

// Buttons
const connectBtn = document.getElementById('connect-btn');
const settingsBtn = document.getElementById('settings-btn');
const saveBtn = document.getElementById('save-btn');
const cancelBtn = document.getElementById('cancel-btn');

function init() {
    const storedUrl = localStorage.getItem(STORAGE_KEY);
    
    // Sync auth settings to native side on startup
    syncAuthToNative();

    if (!storedUrl) {
        // First time open - set default and show settings
        const defaultUrl = 'http://127.0.0.1:8000';
        localStorage.setItem(STORAGE_KEY, defaultUrl);
        updateDisplay(defaultUrl);
        showSettings();
    } else {
        // URL exists - auto connect
        updateDisplay(storedUrl);
        connect();
    }
}

function updateDisplay(url) {
    if (url) {
        currentUrlDisplay.textContent = url;
        connectBtn.disabled = false;
        connectBtn.style.opacity = '1';
    } else {
        currentUrlDisplay.textContent = 'No URL Set';
        connectBtn.disabled = true;
        connectBtn.style.opacity = '0.5';
    }
}

function showSettings() {
    mainScreen.classList.add('hidden');
    settingsScreen.classList.remove('hidden');
    urlInput.value = localStorage.getItem(STORAGE_KEY) || '';
    
    const authEnabled = localStorage.getItem(AUTH_ENABLED_KEY) === 'true';
    authToggle.checked = authEnabled;
    authUser.value = localStorage.getItem(AUTH_USER_KEY) || '';
    authPass.value = localStorage.getItem(AUTH_PASS_KEY) || '';
    
    toggleAuthFields();
}

function toggleAuthFields() {
    if (authToggle.checked) {
        authFields.classList.remove('hidden');
    } else {
        authFields.classList.add('hidden');
    }
}

function hideSettings() {
    settingsScreen.classList.add('hidden');
    mainScreen.classList.remove('hidden');
}

function saveSettings() {
    let url = urlInput.value.trim();
    if (!url) return;

    if (!url.startsWith('http://') && !url.startsWith('https://')) {
        url = 'http://' + url;
    }

    localStorage.setItem(STORAGE_KEY, url);
    
    // Save Auth Settings
    localStorage.setItem(AUTH_ENABLED_KEY, authToggle.checked);
    if (authToggle.checked) {
        localStorage.setItem(AUTH_USER_KEY, authUser.value.trim());
        localStorage.setItem(AUTH_PASS_KEY, authPass.value);
    } else {
        // Optional: Clear credentials if disabled, or keep them but don't use them
        // For now, we keep them in storage but won't send them to native if disabled
    }

    syncAuthToNative();
    updateDisplay(url);
    hideSettings();
}

function syncAuthToNative() {
    const enabled = localStorage.getItem(AUTH_ENABLED_KEY) === 'true';
    const user = localStorage.getItem(AUTH_USER_KEY) || '';
    const pass = localStorage.getItem(AUTH_PASS_KEY) || '';

    if (window.AuthBridge) {
        if (enabled && user && pass) {
            window.AuthBridge.setCredentials(user, pass);
        } else {
            window.AuthBridge.clearCredentials();
        }
    } else {
        console.log('AuthBridge not available');
    }
}

function connect() {
    const url = localStorage.getItem(STORAGE_KEY);
    if (url) {
        window.location.href = url;
    }
}

// Event Listeners
settingsBtn.addEventListener('click', showSettings);
cancelBtn.addEventListener('click', hideSettings);
saveBtn.addEventListener('click', saveSettings);
connectBtn.addEventListener('click', connect);
authToggle.addEventListener('change', toggleAuthFields);

init();
