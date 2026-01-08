const STORAGE_KEY = 'sillytavern_url';

// Elements
const mainScreen = document.getElementById('main-screen');
const settingsScreen = document.getElementById('settings-screen');
const currentUrlDisplay = document.getElementById('current-url-display');
const urlInput = document.getElementById('url-input');

// Buttons
const connectBtn = document.getElementById('connect-btn');
const settingsBtn = document.getElementById('settings-btn');
const saveBtn = document.getElementById('save-btn');
const cancelBtn = document.getElementById('cancel-btn');

function init() {
    const storedUrl = localStorage.getItem(STORAGE_KEY);
    updateDisplay(storedUrl);
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
    updateDisplay(url);
    hideSettings();
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

init();
