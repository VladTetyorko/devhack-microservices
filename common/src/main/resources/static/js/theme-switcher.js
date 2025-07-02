/**
 * Theme switcher for DevHack application
 * Handles switching between light and dark themes
 */

// Check for saved theme preference or use default light theme
document.addEventListener('DOMContentLoaded', function () {
    const savedTheme = localStorage.getItem('theme') || 'light';
    setTheme(savedTheme);

    // Update toggle button icon based on current theme
    updateToggleIcon(savedTheme);
});

/**
 * Toggle between light and dark themes
 */
function toggleTheme() {
    const currentTheme = document.body.classList.contains('dark-theme') ? 'dark' : 'light';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';

    setTheme(newTheme);
    updateToggleIcon(newTheme);

    // Save theme preference to localStorage
    localStorage.setItem('theme', newTheme);
}

/**
 * Set the theme to either light or dark
 * @param {string} theme - 'light' or 'dark'
 */
function setTheme(theme) {
    if (theme === 'dark') {
        document.body.classList.add('dark-theme');

        // Add dark theme stylesheet if it doesn't exist
        if (!document.getElementById('dark-theme-css')) {
            const darkThemeLink = document.createElement('link');
            darkThemeLink.id = 'dark-theme-css';
            darkThemeLink.rel = 'stylesheet';
            darkThemeLink.href = '/css/dark-theme.css';
            document.head.appendChild(darkThemeLink);
        }
    } else {
        document.body.classList.remove('dark-theme');

        // Remove dark theme stylesheet if it exists
        const darkThemeLink = document.getElementById('dark-theme-css');
        if (darkThemeLink) {
            darkThemeLink.remove();
        }
    }
}

/**
 * Update the toggle button icon based on current theme
 * @param {string} theme - 'light' or 'dark'
 */
function updateToggleIcon(theme) {
    const toggleIcon = document.getElementById('theme-toggle-icon');
    if (toggleIcon) {
        if (theme === 'dark') {
            toggleIcon.classList.remove('fa-moon');
            toggleIcon.classList.add('fa-sun');
        } else {
            toggleIcon.classList.remove('fa-sun');
            toggleIcon.classList.add('fa-moon');
        }
    }
}