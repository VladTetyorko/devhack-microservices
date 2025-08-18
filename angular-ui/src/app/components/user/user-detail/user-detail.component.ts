import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {UserService} from '../../../services/user/user.service';
import {ProfileService} from '../../../services/user/profile.service';
import {UserAccessService} from '../../../services/user/user-access.service';
import {AuthenticationProviderService} from '../../../services/user/authentication-provider.service';
import {UserDTO} from '../../../models/user/user.model';
import {ProfileDTO} from '../../../models/user/profile.model';
import {UserAccessDTO} from '../../../models/user/user-access.model';
import {AuthenticationProviderDTO} from '../../../models/user/authentication-provider.model';
import {forkJoin} from 'rxjs';

@Component({
    selector: 'app-user-detail',
    templateUrl: './user-detail.component.html',
    styleUrls: ['./user-detail.component.css']
})
export class UserDetailComponent implements OnInit {
    userForm!: FormGroup;
    userId!: string;
    isLoading = true;
    error = '';
    success = '';
    user?: UserDTO;

    // Related entities loaded by IDs
    userProfile: ProfileDTO | null = null;
    userAccess: UserAccessDTO | null = null;
    userCredentials: AuthenticationProviderDTO[] = [];

    constructor(
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private userService: UserService,
        private profileService: ProfileService,
        private userAccessService: UserAccessService,
        private authProviderService: AuthenticationProviderService
    ) {
    }

    ngOnInit(): void {
        this.userId = this.route.snapshot.paramMap.get('id')!;

        // Initialize the form with ProfileDTO fields only
        this.userForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(2)]],
            aiUsageEnabled: [false],
            aiPreferredLanguage: ['en'],
            isVisibleToRecruiters: [true]
        });

        this.loadUserDetails();
    }

    loadUserDetails(): void {
        this.isLoading = true;
        this.userService.getById(this.userId).subscribe({
            next: (userData) => {
                this.user = userData;
                console.log('[DEBUG_LOG] Loaded user data:', userData);

                // Load related entities using IDs
                this.loadRelatedEntities(userData);
            },
            error: (err) => {
                this.error = 'Failed to load user details. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    private loadRelatedEntities(user: UserDTO): void {
        const requests = [];

        // Load profile
        if (user.profileId) {
            requests.push(this.profileService.getById(user.profileId));
        }

        // Load access
        if (user.accessId) {
            requests.push(this.userAccessService.getById(user.accessId));
        }

        // Load credentials
        if (user.credentialIds && user.credentialIds.length > 0) {
            requests.push(this.authProviderService.getByIds(user.credentialIds));
        }

        if (requests.length > 0) {
            forkJoin(requests).subscribe({
                next: (results) => {
                    let resultIndex = 0;

                    // Assign results based on what was requested
                    if (user.profileId) {
                        this.userProfile = results[resultIndex++] as ProfileDTO;
                    }
                    if (user.accessId) {
                        this.userAccess = results[resultIndex++] as UserAccessDTO;
                    }
                    if (user.credentialIds && user.credentialIds.length > 0) {
                        this.userCredentials = results[resultIndex++] as AuthenticationProviderDTO[];
                    }

                    // Update form with loaded profile data
                    this.updateFormWithLoadedData();
                    this.isLoading = false;

                    console.log('[DEBUG_LOG] Loaded related entities:', {
                        profile: this.userProfile,
                        access: this.userAccess,
                        credentials: this.userCredentials
                    });
                },
                error: (err) => {
                    console.error('[DEBUG_LOG] Error loading related entities:', err);
                    this.error = 'Failed to load user details. ' + err.message;
                    this.isLoading = false;
                }
            });
        } else {
            this.isLoading = false;
        }
    }

    private updateFormWithLoadedData(): void {
        this.userForm.patchValue({
            name: this.userProfile?.name || '',
            aiUsageEnabled: this.userProfile?.aiUsageEnabled || false,
            aiPreferredLanguage: this.userProfile?.aiPreferredLanguage || 'en',
            isVisibleToRecruiters: this.userProfile?.isVisibleToRecruiters || true
        });
    }

    saveUser(): void {
        if (this.userForm.valid && this.userProfile) {
            // Update profile data using ProfileService
            const updatedProfile: ProfileDTO = {
                ...this.userProfile,
                name: this.userForm.value.name,
                aiUsageEnabled: this.userForm.value.aiUsageEnabled,
                aiPreferredLanguage: this.userForm.value.aiPreferredLanguage,
                isVisibleToRecruiters: this.userForm.value.isVisibleToRecruiters
            };

            this.profileService.update(this.userProfile.id!, updatedProfile).subscribe({
                next: (updatedProfileData) => {
                    this.userProfile = updatedProfileData;
                    this.success = 'User profile updated successfully!';
                    setTimeout(() => this.success = '', 3000);
                    console.log('[DEBUG_LOG] Profile updated successfully:', updatedProfileData);
                },
                error: (err) => {
                    console.error('[DEBUG_LOG] Error updating profile:', err);
                    this.error = 'Failed to update user profile. ' + (err.error?.message || err.message || 'Unknown error');
                }
            });
        } else {
            if (!this.userProfile) {
                this.error = 'Cannot update user: Profile data not loaded.';
            } else {
                this.userForm.markAllAsTouched();
            }
        }
    }

    deleteUser(): void {
        if (confirm('Are you sure you want to delete this user?')) {
            this.userService.delete(this.userId).subscribe({
                next: () => {
                    this.router.navigate(['/users']);
                },
                error: (err) => {
                    this.error = 'Failed to delete user. ' + err.message;
                }
            });
        }
    }

    goBack(): void {
        this.router.navigate(['/users']);
    }
}
