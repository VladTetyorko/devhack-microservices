import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {InterviewStageCategoryDTO} from '../../models/global/interview-stage-category.model';
import {BaseService} from '../base.service';

/**
 * Service for managing interview stage categories.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class InterviewStageCategoryService extends BaseService<InterviewStageCategoryDTO> {
    protected baseUrl = '/api/interview-stage-categories';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get interview stage category by code
     * @param code - Category code
     * @returns Observable interview stage category
     */
    getByCode(code: string): Observable<InterviewStageCategoryDTO> {
        return this.http.get<InterviewStageCategoryDTO>(`${this.baseUrl}/by-code/${code}`);
    }
}
