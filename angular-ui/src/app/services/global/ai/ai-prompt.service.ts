import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AiPromptModel} from '../../../models/global/ai/ai-prompt.model';
import {BaseService} from '../../base.service';

/**
 * Service for managing AI prompts.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class AiPromptService extends BaseService<AiPromptModel> {
    protected baseUrl = '/api/ai-prompts';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get AI prompt by code
     * @param code - Prompt code
     * @returns Observable AI prompt
     */
    getByCode(code: string): Observable<AiPromptModel> {
        return this.http.get<AiPromptModel>(`${this.baseUrl}/by-code/${code}`);
    }

    /**
     * Get AI prompts by category ID
     * @param categoryId - Category ID to filter by
     * @returns Observable array of AI prompts
     */
    getByCategoryId(categoryId: string): Observable<AiPromptModel[]> {
        return this.http.get<AiPromptModel[]>(`${this.baseUrl}/by-category/${categoryId}`);
    }

    /**
     * Get all active AI prompts
     * @returns Observable array of active AI prompts
     */
    getActive(): Observable<AiPromptModel[]> {
        return this.http.get<AiPromptModel[]>(`${this.baseUrl}/active`);
    }

    /**
     * Activate an AI prompt
     * @param id - Prompt ID to activate
     * @returns Observable activated AI prompt
     */
    activate(id: string): Observable<AiPromptModel> {
        return this.http.patch<AiPromptModel>(`${this.baseUrl}/${id}/activate`, {});
    }

    /**
     * Deactivate an AI prompt
     * @param id - Prompt ID to deactivate
     * @returns Observable deactivated AI prompt
     */
    deactivate(id: string): Observable<AiPromptModel> {
        return this.http.patch<AiPromptModel>(`${this.baseUrl}/${id}/deactivate`, {});
    }
}
