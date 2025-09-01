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
    override search(keyword?: string, location?: string, company?: string): Observable<VacancyDTO[]>;
    override search(queryOrParams?: string | {
        [key: string]: string | undefined
    }, location?: string, company?: string): Observable<VacancyDTO[]> {
        if (typeof queryOrParams === 'object') {
            // Ensure we map common aliases to backend-expected params
            const params: { [key: string]: string | undefined } = {...queryOrParams};
            if (params['query'] && !params['keyword']) {
                params['keyword'] = params['query'];
                delete params['query'];
            }
            return super.search(params);
        }
        // Backend expects 'keyword' param for search
        return super.search({keyword: queryOrParams, location, company});
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
     * Filter by remote allowed flag using existing REST endpoint
     */
    getByRemote(remoteAllowed: boolean): Observable<VacancyDTO[]> {
        const params = new HttpParams().set('remoteAllowed', String(remoteAllowed));
        return this.http.get<VacancyDTO[]>(`${this.baseUrl}/by-remote`, {params});
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
