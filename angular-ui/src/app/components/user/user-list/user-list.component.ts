import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {UserService} from '../../../services/user.service';
import {UserDTO} from '../../../models/user/user.model';
import {Page, PageRequest} from '../../../models/basic/page.model';

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

                // Debug: Check for incomplete user data
                const incompleteUsers = page.content.filter((user: UserDTO) =>
                    !user.profile || !user.access || !user.credentials || user.credentials.length === 0
                );

                if (incompleteUsers.length > 0) {
                    console.warn('[DEBUG_LOG] Found users with incomplete data:', incompleteUsers);
                }

                this.userPage = page;
                this.allUsers = page.content || [];
                this.isLoading = false;

                console.log('[DEBUG_LOG] Total users in page:', page.content.length);
                console.log('[DEBUG_LOG] Total users overall:', page.totalElements);
                console.log('[DEBUG_LOG] Current page:', page.number + 1);
                console.log('[DEBUG_LOG] Total pages:', page.totalPages);
                console.log('[DEBUG_LOG] Users with profiles:', page.content.filter((u: UserDTO) => u.profile).length);
                console.log('[DEBUG_LOG] Users with access:', page.content.filter((u: UserDTO) => u.access).length);
                console.log('[DEBUG_LOG] Users with credentials:', page.content.filter((u: UserDTO) => u.credentials && u.credentials.length > 0).length);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading users:', err);
                this.error = 'Failed to load users. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
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
        const userName = user?.profile?.name || 'this user';

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
            filtered = filtered.filter(user =>
                (user.profile?.name?.toLowerCase().includes(searchLower)) ||
                (user.credentials?.[0]?.email?.toLowerCase().includes(searchLower))
            );
        }

        // Apply role filter
        if (this.selectedRole) {
            filtered = filtered.filter(user => user.access?.role === this.selectedRole);
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
