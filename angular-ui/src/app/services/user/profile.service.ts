import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ProfileDTO} from '../../models/user/profile.model';
import {BaseService} from '../base.service';

/**
 * Service for managing user profiles.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class ProfileService extends BaseService<ProfileDTO> {
    protected baseUrl = '/api/profiles';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get profile for a specific user
     * @param userId - User ID
     * @returns Observable profile
     */
    getByUserId(userId: string): Observable<ProfileDTO> {
        return this.getSingleWithParams('by-user', {userId});
    }

    /**
     * Update CV file for a profile
     * @param profileId - Profile ID
     * @param cvFile - CV file to upload
     * @returns Observable updated profile
     */
    updateCvFile(profileId: string, cvFile: File): Observable<ProfileDTO> {
        const formData = new FormData();
        formData.append('cvFile', cvFile);
        return this.http.put<ProfileDTO>(`${this.baseUrl}/${profileId}/cv`, formData);
    }

    /**
     * Delete CV file from a profile
     * @param profileId - Profile ID
     * @returns Observable updated profile
     */
    deleteCvFile(profileId: string): Observable<ProfileDTO> {
        return this.http.delete<ProfileDTO>(`${this.baseUrl}/${profileId}/cv`);
    }

    /**
     * Update AI settings for a profile
     * @param profileId - Profile ID
     * @param aiSettings - AI settings to update
     * @returns Observable updated profile
     */
    updateAiSettings(profileId: string, aiSettings: {
        aiUsageEnabled?: boolean;
        aiPreferredLanguage?: string;
    }): Observable<ProfileDTO> {
        return this.http.patch<ProfileDTO>(`${this.baseUrl}/${profileId}/ai-settings`, aiSettings);
    }
}