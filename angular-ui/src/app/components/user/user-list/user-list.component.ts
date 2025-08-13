import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {UserService} from '../../../services/user/user.service';
import {AuthenticationProviderService} from '../../../services/user/authentication-provider.service';
import {ProfileService} from '../../../services/user/profile.service';
import {UserAccessService} from '../../../services/user/user-access.service';
import {UserDTO} from '../../../models/user/user.model';
import {AuthenticationProviderDTO} from '../../../models/user/authentication-provider.model';
import {ProfileDTO} from '../../../models/user/profile.model';
import {UserAccessDTO} from '../../../models/user/user-access.model';
import {Page, PageRequest} from '../../../models/basic/page.model';
import {forkJoin, of} from 'rxjs';
import {tap} from 'rxjs/operators';

@Component({
    selector: 'app-user-list',
    templateUrl: './user-list.component.html',
    styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
    userPage: Page<UserDTO> | null = null;
    allUsers: UserDTO[] = []; // Keep for filtering
    isLoading = true;
    error = '';
    successMessage = '';

    // Related entities loaded by IDs
    userCredentials: Map<string, AuthenticationProviderDTO[]> = new Map();
    userProfiles: Map<string, ProfileDTO> = new Map();
    userAccess: Map<string, UserAccessDTO> = new Map();

    // Search and filter properties
    searchTerm = '';
    selectedRole = '';
    viewMode = 'table'; // 'table' or 'cards'

    // Pagination properties
    currentPageRequest: PageRequest = {
        page: 0,
        size: 10,
        sort: ['createdAt,desc']
    };

    // Skeleton loading
    skeletonItems = Array(6).fill(0); // Show 6 skeleton items while loading

    constructor(
        private userService: UserService,
        private authProviderService: AuthenticationProviderService,
        private profileService: ProfileService,
        private userAccessService: UserAccessService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadUsers();
    }

    loadUsers(): void {
        this.isLoading = true;
        this.error = '';
        this.userService.getAllPaged(this.currentPageRequest).subscribe({
            next: (page) => {
                console.log('[DEBUG_LOG] Loaded users page:', page);
                this.userPage = page;
                this.allUsers = page.content || [];

                // Load related entities for all users
                this.loadRelatedEntities(page.content).subscribe({
                    next: () => {
                        this.isLoading = false;
                        console.log('[DEBUG_LOG] Total users in page:', page.content.length);
                        console.log('[DEBUG_LOG] Total users overall:', page.totalElements);
                        console.log('[DEBUG_LOG] Current page:', page.number + 1);
                        console.log('[DEBUG_LOG] Total pages:', page.totalPages);
                        console.log('[DEBUG_LOG] Loaded credentials for users:', this.userCredentials.size);
                        console.log('[DEBUG_LOG] Loaded profiles for users:', this.userProfiles.size);
                        console.log('[DEBUG_LOG] Loaded access for users:', this.userAccess.size);
                    },
                    error: (err) => {
                        console.error('[DEBUG_LOG] Error loading related entities:', err);
                        this.isLoading = false;
                        // Don't show error for related entities, just log it
                    }
                });
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading users:', err);
                this.error = 'Failed to load users. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    private loadRelatedEntities(users: UserDTO[]) {
        // Clear existing data
        this.userCredentials.clear();
        this.userProfiles.clear();
        this.userAccess.clear();

        const requests = [];

        // Load credentials for all users
        for (const user of users) {
            if (user.credentialIds && user.credentialIds.length > 0) {
                const credentialsRequest = this.authProviderService.getByIds(user.credentialIds).pipe(
                    tap(credentials => this.userCredentials.set(user.id!, credentials))
                );
                requests.push(credentialsRequest);
            }

            // Load profile
            if (user.profileId) {
                const profileRequest = this.profileService.getById(user.profileId).pipe(
                    tap(profile => this.userProfiles.set(user.id!, profile))
                );
                requests.push(profileRequest);
            }

            // Load access
            if (user.accessId) {
                const accessRequest = this.userAccessService.getById(user.accessId).pipe(
                    tap(access => this.userAccess.set(user.id!, access))
                );
                requests.push(accessRequest);
            }
        }

        // Return observable that completes when all requests are done
        return requests.length > 0 ? forkJoin(requests) : of([]);
    }

    // Helper methods to access loaded entities
    getUserCredentials(userId: string): AuthenticationProviderDTO[] {
        return this.userCredentials.get(userId) || [];
    }

    getUserProfile(userId: string): ProfileDTO | null {
        return this.userProfiles.get(userId) || null;
    }

    getUserAccess(userId: string): UserAccessDTO | null {
        return this.userAccess.get(userId) || null;
    }

    onSearch(): void {
        this.applyFilters();
    }

    onRoleFilter(): void {
        this.applyFilters();
    }

    applyFilters(): void {
        // For now, we'll reload the data when filters change
        // In a more advanced implementation, we could send filter parameters to the backend
        this.currentPageRequest.page = 0; // Reset to first page when filtering
        this.loadUsers();
    }

    clearSearch(): void {
        this.searchTerm = '';
        this.applyFilters();
    }

    clearRoleFilter(): void {
        this.selectedRole = '';
        this.applyFilters();
    }

    viewDetail(id: string): void {
        this.router.navigate(['/users', id]);
    }

    editUser(id: string): void {
        this.router.navigate(['/users', id]);
    }

    createNew(): void {
        this.router.navigate(['/users/register']);
    }

    deleteUser(id: string, event: Event): void {
        event.stopPropagation();

        const user = this.allUsers.find(u => u.id === id);
        const profile = this.getUserProfile(id);
        const userName = profile?.name || 'this user';

        if (confirm(`Are you sure you want to delete ${userName}? This action cannot be undone.`)) {
            this.userService.delete(id).subscribe({
                next: () => {
                    this.successMessage = `User ${userName} has been successfully deleted.`;
                    this.loadUsers();
                    // Clear success message after 5 seconds
                    setTimeout(() => this.successMessage = '', 5000);
                },
                error: (err) => {
                    this.error = 'Failed to delete user. ' + err.message;
                }
            });
        }
    }

    // Pagination event handlers
    onPageChange(page: number): void {
        this.currentPageRequest.page = page;
        this.loadUsers();
    }

    onPageSizeChange(size: number): void {
        this.currentPageRequest.size = size;
        this.currentPageRequest.page = 0; // Reset to first page when changing size
        this.loadUsers();
    }

    // Getter for filtered users (for template compatibility)
    get filteredUsers(): UserDTO[] {
        if (!this.userPage) return [];

        let filtered = [...this.userPage.content];

        // Apply search filter
        if (this.searchTerm.trim()) {
            const searchLower = this.searchTerm.toLowerCase().trim();
            filtered = filtered.filter(user => {
                const profile = this.getUserProfile(user.id!);
                const credentials = this.getUserCredentials(user.id!);
                return (profile?.name?.toLowerCase().includes(searchLower)) ||
                    (credentials[0]?.email?.toLowerCase().includes(searchLower));
            });
        }

        // Apply role filter
        if (this.selectedRole) {
            filtered = filtered.filter(user => {
                const access = this.getUserAccess(user.id!);
                return access?.role === this.selectedRole;
            });
        }

        return filtered;
    }

    // Utility methods for the template
    getInitials(name?: string): string {
        if (!name) return '?';
        return name.split(' ')
            .map(word => word.charAt(0).toUpperCase())
            .slice(0, 2)
            .join('');
    }

    getRoleBadgeClass(role: string): string {
        switch (role?.toUpperCase()) {
            case 'ADMIN':
                return 'bg-danger';
            case 'MANAGER':
                return 'bg-warning text-dark';
            case 'USER':
                return 'bg-success';
            default:
                return 'bg-secondary';
        }
    }

    trackByUserId(index: number, user: UserDTO): string {
        return user.id || index.toString();
    }
}
