import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserAccessDTO} from '../../models/user/user-access.model';
import {BaseService} from '../base.service';

/**
 * Service for managing user access settings and roles.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class UserAccessService extends BaseService<UserAccessDTO> {
    protected baseUrl = '/api/user-access';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get user access settings for a specific user
     * @param userId - User ID
     * @returns Observable user access settings
     */
    getByUserId(userId: string): Observable<UserAccessDTO> {
        return this.getSingleWithParams('by-user', {userId});
    }

    /**
     * Get user access settings by role
     * @param role - Role name (e.g., 'ADMIN', 'USER', 'MANAGER')
     * @returns Observable array of user access settings
     */
    getByRole(role: string): Observable<UserAccessDTO[]> {
        return this.getWithParams('by-role', {role});
    }

    /**
     * Update user role
     * @param userAccessId - User access ID
     * @param role - New role
     * @returns Observable updated user access
     */
    updateRole(userAccessId: string, role: string): Observable<UserAccessDTO> {
        return this.http.patch<UserAccessDTO>(`${this.baseUrl}/${userAccessId}/role`, {role});
    }

    /**
     * Update AI usage permission
     * @param userAccessId - User access ID
     * @param aiUsageAllowed - Whether AI usage is allowed
     * @returns Observable updated user access
     */
    updateAiUsage(userAccessId: string, aiUsageAllowed: boolean): Observable<UserAccessDTO> {
        return this.http.patch<UserAccessDTO>(`${this.baseUrl}/${userAccessId}/ai-usage`, {aiUsageAllowed});
    }

    /**
     * Update account lock status
     * @param userAccessId - User access ID
     * @param accountLocked - Whether account is locked
     * @returns Observable updated user access
     */
    updateLockStatus(userAccessId: string, accountLocked: boolean): Observable<UserAccessDTO> {
        return this.http.patch<UserAccessDTO>(`${this.baseUrl}/${userAccessId}/lock-status`, {accountLocked});
    }

    /**
     * Get current user's access settings
     * @returns Observable current user's access settings
     */
    getMyUserAccess(): Observable<UserAccessDTO> {
        return this.http.get<UserAccessDTO>(`${this.baseUrl}/my-access`);
    }
}