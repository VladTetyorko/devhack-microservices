import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {InterviewStageDTO} from '../../models/global/interview-stage.model';
import {BaseService} from '../base.service';

/**
 * Service for managing interview stages.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class InterviewStageService extends BaseService<InterviewStageDTO> {
    protected baseUrl = '/api/interview-stages';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get interview stage by code
     * @param code - Stage code
     * @returns Observable interview stage
     */
    getByCode(code: string): Observable<InterviewStageDTO> {
        return this.http.get<InterviewStageDTO>(`${this.baseUrl}/by-code/${code}`);
    }

    /**
     * Get interview stages by category ID
     * @param categoryId - Category ID to filter by
     * @returns Observable array of interview stages
     */
    getByCategory(categoryId: string): Observable<InterviewStageDTO[]> {
        return this.http.get<InterviewStageDTO[]>(`${this.baseUrl}/by-category/${categoryId}`);
    }

    /**
     * Get all active interview stages
     * @returns Observable array of active interview stages
     */
    getActive(): Observable<InterviewStageDTO[]> {
        return this.http.get<InterviewStageDTO[]>(`${this.baseUrl}/active`);
    }

    /**
     * Get final interview stages
     * @returns Observable array of final interview stages
     */
    getFinalStages(): Observable<InterviewStageDTO[]> {
        return this.http.get<InterviewStageDTO[]>(`${this.baseUrl}/final`);
    }

    /**
     * Get interview stages in order
     * @returns Observable array of ordered interview stages
     */
    getOrderedStages(): Observable<InterviewStageDTO[]> {
        return this.http.get<InterviewStageDTO[]>(`${this.baseUrl}/ordered`);
    }

    /**
     * Update the order of interview stages
     * @param stageOrders - Array of stage IDs with their new order indices
     * @returns Observable void
     */
    updateOrder(stageOrders: { id: string, orderIndex: number }[]): Observable<void> {
        return this.http.put<void>(`${this.baseUrl}/update-order`, stageOrders);
    }
}
