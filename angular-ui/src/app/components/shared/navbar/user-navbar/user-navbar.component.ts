import {Component, OnDestroy, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {NavbarBase} from "../navbar.base";
import {AuthService} from "../../../../services/basic/auth.service";
import {Theme, ThemeService} from "../../../../services/theme/theme.service";
import {Subscription} from "rxjs";

@Component({
    selector: 'user-app-navbar',
    templateUrl: './user-navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class UserNavbarComponent extends NavbarBase implements OnInit, OnDestroy {
    currentTheme: Theme = 'light';
    private themeSubscription: Subscription | null = null;

    constructor(
        auth: AuthService,
        router: Router,
        private themeService: ThemeService
    ) {
        super(auth, router);
    }

    override ngOnInit(): void {
        super.ngOnInit();

        // Subscribe to theme changes
        this.themeSubscription = this.themeService.theme$.subscribe(
            (theme: Theme) => {
                this.currentTheme = theme;
            }
        );

        // Listen to system theme changes
        this.themeService.listenToSystemThemeChanges();
    }

    override ngOnDestroy(): void {
        super.ngOnDestroy();

        if (this.themeSubscription) {
            this.themeSubscription.unsubscribe();
        }
    }

    /**
     * Toggle between light and dark theme
     */
    toggleTheme(): void {
        this.themeService.toggleTheme();
    }

    /**
     * Get the appropriate icon class for the current theme
     */
    getThemeIcon(): string {
        return this.currentTheme === 'light' ? 'bi-moon-fill' : 'bi-sun-fill';
    }

    /**
     * Get the tooltip text for the theme toggle button
     */
    getThemeTooltip(): string {
        return this.currentTheme === 'light' ? 'Switch to dark theme' : 'Switch to light theme';
    }
}