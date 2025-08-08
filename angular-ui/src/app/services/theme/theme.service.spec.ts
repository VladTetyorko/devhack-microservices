import {TestBed} from '@angular/core/testing';
import {Theme, ThemeService} from './theme.service';

describe('ThemeService', () => {
    let service: ThemeService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(ThemeService);

        // Clear localStorage before each test
        localStorage.clear();

        // Remove any existing theme classes
        document.body.classList.remove('light-theme', 'dark-theme');
    });

    afterEach(() => {
        // Clean up after each test
        localStorage.clear();
        document.body.classList.remove('light-theme', 'dark-theme');
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should initialize with light theme by default', () => {
        expect(service.getCurrentTheme()).toBe('light');
        expect(service.isLightTheme()).toBe(true);
        expect(service.isDarkTheme()).toBe(false);
    });

    it('should toggle theme correctly', () => {
        // Start with light theme
        expect(service.getCurrentTheme()).toBe('light');

        // Toggle to dark
        service.toggleTheme();
        expect(service.getCurrentTheme()).toBe('dark');
        expect(service.isDarkTheme()).toBe(true);
        expect(document.body.classList.contains('dark-theme')).toBe(true);

        // Toggle back to light
        service.toggleTheme();
        expect(service.getCurrentTheme()).toBe('light');
        expect(service.isLightTheme()).toBe(true);
        expect(document.body.classList.contains('dark-theme')).toBe(false);
    });

    it('should set theme correctly', () => {
        service.setTheme('dark');
        expect(service.getCurrentTheme()).toBe('dark');
        expect(document.body.classList.contains('dark-theme')).toBe(true);

        service.setTheme('light');
        expect(service.getCurrentTheme()).toBe('light');
        expect(document.body.classList.contains('dark-theme')).toBe(false);
    });

    it('should persist theme to localStorage', () => {
        service.setTheme('dark');
        expect(localStorage.getItem('devhack-theme')).toBe('dark');

        service.setTheme('light');
        expect(localStorage.getItem('devhack-theme')).toBe('light');
    });

    it('should emit theme changes', (done) => {
        let emissionCount = 0;
        const expectedThemes: Theme[] = ['light', 'dark', 'light'];

        service.theme$.subscribe((theme: Theme) => {
            expect(theme).toBe(expectedThemes[emissionCount]);
            emissionCount++;

            if (emissionCount === expectedThemes.length) {
                done();
            }
        });

        // Initial emission should be 'light'
        // Then toggle to 'dark'
        service.toggleTheme();
        // Then toggle back to 'light'
        service.toggleTheme();
    });

    it('should reset to system theme', () => {
        // Set a manual theme first
        service.setTheme('dark');
        expect(localStorage.getItem('devhack-theme')).toBe('dark');

        // Reset to system theme
        service.resetToSystemTheme();
        expect(localStorage.getItem('devhack-theme')).toBeNull();

        // Should fall back to light theme (since we can't easily mock system preference in tests)
        expect(service.getCurrentTheme()).toBe('light');
    });
});