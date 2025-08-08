import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BaseService} from './base.service';
import {UserOwnedEntity} from '../models/user-owned-entity.model';
import {Page, PageRequest} from '../models/basic/page.model';

/**
 * Base service class for personalized (user-owned) entities.
 * This class extends BaseService and provides common functionality for entities related to a person/user.
 * It follows OOP principles and DRY principle to avoid code duplication.
 *
 * @template T - The DTO type for the user-owned entity that extends UserOwnedEntity
 */
@Injectable()
export abstract class PersonalizedService<T extends UserOwnedEntity> extends BaseService<T> {

    constructor(protected override http: HttpClient) {
        super(http);
    }

    /**
     * Get current user's entities
     * @returns Observable array of user's entities
     */
    getMyEntities(): Observable<T[]> {
        return this.http.get<T[]>(`${this.baseUrl}/my`);
    }

    /**
     * Get entities by user ID
     * @param userId - User ID to filter by
     * @returns Observable array of entities
     */
    getByUser(userId: string): Observable<T[]> {
        return this.http.get<T[]>(`${this.baseUrl}/by-user/${userId}`);
    }

    /**
     * Search entities with user-specific criteria
     * @param searchParams - Object with search parameters including user-specific filters
     * @returns Observable array of entities
     */
    override search(searchParams: { [key: string]: string | undefined }): Observable<T[]> {
        return super.search(searchParams);
    }

    /**
     * Get entities by current user with pagination
     * @param pageRequest - Pagination parameters
     * @returns Observable page of user's entities
     */
    getMyEntitiesPaged(pageRequest: PageRequest): Observable<Page<T>> {
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

        return this.http.get<Page<T>>(`${this.baseUrl}/my/page`, {params});
    }
}
