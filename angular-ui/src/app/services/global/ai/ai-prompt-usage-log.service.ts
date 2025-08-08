import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AiPromptUsageLogModel} from '../../../models/global/ai/ai-prompt-usage-log.model';
import {BaseService} from '../../base.service';

/**
 * Service for managing AI prompt usage logs.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class AiPromptUsageLogService extends BaseService<AiPromptUsageLogModel> {
    protected baseUrl = '/api/ai-prompt-usage-logs';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get AI prompt usage logs by user ID
     * @param userId - User ID to filter by
     * @returns Observable array of AI prompt usage logs
     */
    getByUserId(userId: string): Observable<AiPromptUsageLogModel[]> {
        return this.http.get<AiPromptUsageLogModel[]>(`${this.baseUrl}/by-user/${userId}`);
    }

    /**
     * Get AI prompt usage logs by prompt ID
     * @param promptId - Prompt ID to filter by
     * @returns Observable array of AI prompt usage logs
     */
    getByPromptId(promptId: string): Observable<AiPromptUsageLogModel[]> {
        return this.http.get<AiPromptUsageLogModel[]>(`${this.baseUrl}/by-prompt/${promptId}`);
    }

    /**
     * Get current user's AI prompt usage logs
     * @returns Observable array of current user's AI prompt usage logs
     */
    getMyLogs(): Observable<AiPromptUsageLogModel[]> {
        return this.http.get<AiPromptUsageLogModel[]>(`${this.baseUrl}/my`);
    }
}
