import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {VacancyDTO} from '../../models/global/vacancy.model';
import {BaseService} from '../base.service';

/**
 * Service for managing vacancies.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class VacancyService extends BaseService<VacancyDTO> {
    protected baseUrl = '/api/vacancies';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Search vacancies with multiple criteria
     * @param query - Search query
     * @param location - Location filter
     * @param company - Company filter
     * @returns Observable array of vacancies
     */
    override search(searchParams: { [key: string]: string | undefined }): Observable<VacancyDTO[]>;
    override search(query?: string, location?: string, company?: string): Observable<VacancyDTO[]>;
    override search(queryOrParams?: string | {
        [key: string]: string | undefined
    }, location?: string, company?: string): Observable<VacancyDTO[]> {
        if (typeof queryOrParams === 'object') {
            return super.search(queryOrParams);
        }
        return super.search({query: queryOrParams, location, company});
    }

    /**
     * Get vacancies by company
     * @param company - Company name to filter by
     * @returns Observable array of vacancies
     */
    getByCompany(company: string): Observable<VacancyDTO[]> {
        return this.http.get<VacancyDTO[]>(`${this.baseUrl}/by-company/${company}`);
    }

    /**
     * Get vacancies by location
     * @param location - Location to filter by
     * @returns Observable array of vacancies
     */
    getByLocation(location: string): Observable<VacancyDTO[]> {
        return this.http.get<VacancyDTO[]>(`${this.baseUrl}/by-location/${location}`);
    }

    /**
     * Get recent vacancies
     * @param limit - Optional limit for number of vacancies
     * @returns Observable array of recent vacancies
     */
    getRecent(limit?: number): Observable<VacancyDTO[]> {
        let params = new HttpParams();
        if (limit) params = params.set('limit', limit.toString());

        return this.http.get<VacancyDTO[]>(`${this.baseUrl}/recent`, {params});
    }

    /**
     * Parse vacancy from URL
     * @param url - URL to parse vacancy from
     * @returns Observable parsed vacancy
     */
    parseVacancy(url: string): Observable<VacancyDTO> {
        const body = {url};
        return this.http.post<VacancyDTO>(`${this.baseUrl}/parse`, body);
    }
}
