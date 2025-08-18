import {Router} from "@angular/router";
import {Component, OnDestroy, OnInit} from "@angular/core";
import {AuthService} from "../../../../services/basic/auth.service";
import {ProfileService} from "../../../../services/user/profile.service";
import {ProfileDTO} from "../../../../models/user/profile.model";
import {NavbarBase} from "../navbar.base";

@Component({
    selector: 'admin-app-navbar',
    templateUrl: './admin-navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class AdminNavbarComponent extends NavbarBase implements OnInit, OnDestroy {
    currentUserProfile: ProfileDTO | null = null;

    constructor(
        auth: AuthService,
        router: Router,
        private profileService: ProfileService
    ) {
        super(auth, router);
    }

    override ngOnInit(): void {
        super.ngOnInit();

        // Subscribe to auth state changes to load profile data
        this.authService.authState$.subscribe(authState => {
            if (authState.isAuthenticated && authState.user?.profileId) {
                this.loadUserProfile(authState.user.profileId);
            } else {
                this.currentUserProfile = null;
            }
        });
    }

    private loadUserProfile(profileId: string): void {
        this.profileService.getById(profileId).subscribe({
            next: (profile) => {
                this.currentUserProfile = profile;
                console.log('[DEBUG_LOG] Loaded admin user profile for navbar:', profile);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading admin user profile for navbar:', err);
                this.currentUserProfile = null;
            }
        });
    }
}