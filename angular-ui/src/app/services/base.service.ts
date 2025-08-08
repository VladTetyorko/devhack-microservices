import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Page, PageRequest} from '../models/basic/page.model';

/**
 * Base service class that provides common CRUD operations for all services.
 * This class follows OOP principles and DRY principle to avoid code duplication.
 *
 * @template T - The DTO type for the entity
 */
@Injectable()
export abstract class BaseService<T> {
    protected abstract baseUrl: string;

    constructor(protected http: HttpClient) {
    }

    /**
     * Get all entities
     * @returns Observable array of entities
     */
    getAll(): Observable<T[]> {
        return this.http.get<T[]>(this.baseUrl);
    }

    /**
     * Get entities with pagination
     * @param pageRequest - Pagination parameters
     * @returns Observable page of entities
     */
    getAllPaged(pageRequest: PageRequest): Observable<Page<T>> {
        let params = new HttpParams();

        if (pageRequest.page !== undefined) {
            params = params.set('page', pageRequest.page.toString());
        }
        if (pageRequest.size !== undefined) {
            params = params.set('size', pageRequest.size.toString());
        }
        if (pageRequest.sort && pageRequest.sort.length > 0) {
            pageRequest.sort.forEach((sortParam: string) => {
                params = params.append('sort', sortParam);
            });
        }

        return this.http.get<Page<T>>(`${this.baseUrl}/page`, {params});
    }

    /**
     * Get entity by ID
     * @param id - Entity ID
     * @returns Observable entity
     */
    getById(id: string): Observable<T> {
        return this.http.get<T>(`${this.baseUrl}/${id}`);
    }

    /**
     * Create new entity
     * @param entity - Entity to create
     * @returns Observable created entity
     */
    create(entity: T): Observable<T> {
        return this.http.post<T>(this.baseUrl, entity);
    }

    /**
     * Update existing entity
     * @param id - Entity ID
     * @param entity - Updated entity data
     * @returns Observable updated entity
     */
    update(id: string, entity: T): Observable<T> {
        return this.http.put<T>(`${this.baseUrl}/${id}`, entity);
    }

    /**
     * Delete entity by ID
     * @param id - Entity ID
     * @returns Observable void
     */
    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }

    /**
     * Generic search method with query parameters
     * @param searchParams - Object with search parameters
     * @returns Observable array of entities
     */
    search(searchParams: { [key: string]: string | undefined }): Observable<T[]> {
        let params = new HttpParams();

        Object.keys(searchParams).forEach(key => {
            const value = searchParams[key];
            if (value) {
                params = params.set(key, value);
            }
        });

        return this.http.get<T[]>(`${this.baseUrl}/search`, {params});
    }

    /**
     * Generic method for custom GET requests with parameters
     * @param endpoint - Custom endpoint (relative to baseUrl)
     * @param queryParams - Optional query parameters
     * @returns Observable array of entities
     */
    protected getWithParams(endpoint: string, queryParams?: {
        [key: string]: string | number | undefined
    }): Observable<T[]> {
        let params = new HttpParams();

        if (queryParams) {
            Object.keys(queryParams).forEach(key => {
                const value = queryParams[key];
                if (value !== undefined) {
                    params = params.set(key, value.toString());
                }
            });
        }

        return this.http.get<T[]>(`${this.baseUrl}/${endpoint}`, {params});
    }

    /**
     * Generic method for custom GET requests with parameters that return a single entity
     * @param endpoint - Custom endpoint (relative to baseUrl)
     * @param queryParams - Optional query parameters
     * @returns Observable single entity
     */
    protected getSingleWithParams(endpoint: string, queryParams?: {
        [key: string]: string | number | undefined
    }): Observable<T> {
        let params = new HttpParams();

        if (queryParams) {
            Object.keys(queryParams).forEach(key => {
                const value = queryParams[key];
                if (value !== undefined) {
                    params = params.set(key, value.toString());
                }
            });
        }

        return this.http.get<T>(`${this.baseUrl}/${endpoint}`, {params});
    }

    /**
     * Generic method for custom POST requests
     * @param endpoint - Custom endpoint (relative to baseUrl)
     * @param body - Request body
     * @returns Observable response
     */
    protected postToEndpoint<R = T>(endpoint: string, body: any): Observable<R> {
        return this.http.post<R>(`${this.baseUrl}/${endpoint}`, body);
    }
}
