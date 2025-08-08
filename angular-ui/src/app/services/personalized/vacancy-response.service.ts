import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {VacancyResponseDTO} from '../../models/personalized/vacancy-response.model';
import {PersonalizedService} from '../personalized.service';

/**
 * Service for managing vacancy responses.
 * Extends PersonalizedService to inherit common CRUD operations for user-owned entities and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class VacancyResponseService extends PersonalizedService<VacancyResponseDTO> {
    protected baseUrl = '/api/vacancy-responses';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get current user's vacancy responses
     * @returns Observable array of user's vacancy responses
     */
    getMyResponses(): Observable<VacancyResponseDTO[]> {
        return this.http.get<VacancyResponseDTO[]>(`${this.baseUrl}/my-responses`);
    }

    /**
     * Get vacancy responses by vacancy ID
     * @param vacancyId - Vacancy ID to filter by
     * @returns Observable array of vacancy responses
     */
    getByVacancy(vacancyId: string): Observable<VacancyResponseDTO[]> {
        return this.http.get<VacancyResponseDTO[]>(`${this.baseUrl}/by-vacancy/${vacancyId}`);
    }

    /**
     * Create a new vacancy response for a specific vacancy
     * @param vacancyId - Vacancy ID to create response for
     * @returns Observable created vacancy response
     */
    createForVacancy(vacancyId: string): Observable<VacancyResponseDTO> {
        return this.http.post<VacancyResponseDTO>(`${this.baseUrl}/for-vacancy/${vacancyId}`, {});
    }
}
