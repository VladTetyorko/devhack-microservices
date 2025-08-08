import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';

export type Theme = 'light' | 'dark';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {
    private readonly THEME_KEY = 'devhack-theme';
    private readonly DEFAULT_THEME: Theme = 'light';

    private themeSubject = new BehaviorSubject<Theme>(this.DEFAULT_THEME);
    public theme$: Observable<Theme> = this.themeSubject.asObservable();

    constructor() {
        this.initializeTheme();
    }

    /**
     * Initialize theme from localStorage or use default
     */
    private initializeTheme(): void {
        const savedTheme = this.getThemeFromStorage();
        const systemPrefersDark = this.getSystemPreference();

        // Priority: saved theme > system preference > default
        const initialTheme = savedTheme || (systemPrefersDark ? 'dark' : 'light');

        this.setTheme(initialTheme);
    }

    /**
     * Get theme preference from localStorage
     */
    private getThemeFromStorage(): Theme | null {
        try {
            const savedTheme = localStorage.getItem(this.THEME_KEY);
            return savedTheme === 'dark' || savedTheme === 'light' ? savedTheme : null;
        } catch (error) {
            console.warn('Failed to read theme from localStorage:', error);
            return null;
        }
    }

    /**
     * Get system theme preference
     */
    private getSystemPreference(): boolean {
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    }

    /**
     * Save theme to localStorage
     */
    private saveThemeToStorage(theme: Theme): void {
        try {
            localStorage.setItem(this.THEME_KEY, theme);
        } catch (error) {
            console.warn('Failed to save theme to localStorage:', error);
        }
    }

    /**
     * Apply theme to document body
     */
    private applyThemeToDocument(theme: Theme): void {
        const body = document.body;

        // Remove existing theme classes
        body.classList.remove('light-theme', 'dark-theme');

        // Add new theme class
        if (theme === 'dark') {
            body.classList.add('dark-theme');
        }
        // Light theme is default, no class needed
    }

    /**
     * Get current theme
     */
    getCurrentTheme(): Theme {
        return this.themeSubject.value;
    }

    /**
     * Set theme
     */
    setTheme(theme: Theme): void {
        this.themeSubject.next(theme);
        this.applyThemeToDocument(theme);
        this.saveThemeToStorage(theme);
    }

    /**
     * Toggle between light and dark theme
     */
    toggleTheme(): void {
        const currentTheme = this.getCurrentTheme();
        const newTheme: Theme = currentTheme === 'light' ? 'dark' : 'light';
        this.setTheme(newTheme);
    }

    /**
     * Check if current theme is dark
     */
    isDarkTheme(): boolean {
        return this.getCurrentTheme() === 'dark';
    }

    /**
     * Check if current theme is light
     */
    isLightTheme(): boolean {
        return this.getCurrentTheme() === 'light';
    }

    /**
     * Listen to system theme changes
     */
    listenToSystemThemeChanges(): void {
        if (window.matchMedia) {
            const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

            mediaQuery.addEventListener('change', (e) => {
                // Only auto-switch if user hasn't manually set a preference
                const savedTheme = this.getThemeFromStorage();
                if (!savedTheme) {
                    const newTheme: Theme = e.matches ? 'dark' : 'light';
                    this.setTheme(newTheme);
                }
            });
        }
    }

    /**
     * Reset theme to system preference
     */
    resetToSystemTheme(): void {
        try {
            localStorage.removeItem(this.THEME_KEY);
            const systemPrefersDark = this.getSystemPreference();
            const systemTheme: Theme = systemPrefersDark ? 'dark' : 'light';
            this.setTheme(systemTheme);
        } catch (error) {
            console.warn('Failed to reset theme:', error);
            this.setTheme(this.DEFAULT_THEME);
        }
    }
}