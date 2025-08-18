import {Component, OnInit} from '@angular/core';
import {AuthService} from "../services/basic/auth.service";
import {UserDTO} from "../models/user/user.model";
import {ProfileService} from "../services/user/profile.service";
import {ProfileDTO} from "../models/user/profile.model";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
    currentUser?: UserDTO | null;
    currentUserProfile?: ProfileDTO | null;

    constructor(
        public auth: AuthService,
        private profileService: ProfileService
    ) {
    }

    ngOnInit(): void {
        this.currentUser = this.auth.getCurrentUser();

        // Load profile data if user is authenticated and has profileId
        if (this.currentUser?.profileId) {
            this.loadUserProfile(this.currentUser.profileId);
        }
    }

    private loadUserProfile(profileId: string): void {
        this.profileService.getById(profileId).subscribe({
            next: (profile) => {
                this.currentUserProfile = profile;
                console.log('[DEBUG_LOG] Loaded user profile for home:', profile);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading user profile for home:', err);
                this.currentUserProfile = null;
            }
        });
    }

    get isAuthenticated(): boolean {
        return this.auth.isAuthenticated();
    }
}
