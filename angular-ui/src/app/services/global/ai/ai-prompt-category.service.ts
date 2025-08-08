import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AiPromptCategoryModel} from '../../../models/global/ai/ai-prompt-category.model';
import {BaseService} from '../../base.service';

/**
 * Service for managing AI prompt categories.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class AiPromptCategoryService extends BaseService<AiPromptCategoryModel> {
    protected baseUrl = '/api/ai-prompt-categories';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get AI prompt category by code
     * @param code - Category code
     * @returns Observable AI prompt category
     */
    getByCode(code: string): Observable<AiPromptCategoryModel> {
        return this.http.get<AiPromptCategoryModel>(`${this.baseUrl}/by-code/${code}`);
    }

    /**
     * Get AI prompt categories with their associated prompts
     * @returns Observable array of AI prompt categories with prompts
     */
    getWithPrompts(): Observable<AiPromptCategoryModel[]> {
        return this.http.get<AiPromptCategoryModel[]>(`${this.baseUrl}/with-prompts`);
    }
}
