import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {forkJoin, Observable, of} from 'rxjs';
import {AuthenticationProviderDTO} from '../../models/user/authentication-provider.model';
import {BaseService} from '../base.service';

/**
 * Service for managing authentication providers.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class AuthenticationProviderService extends BaseService<AuthenticationProviderDTO> {
    protected baseUrl = '/api/auth-providers';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Load multiple authentication providers by their IDs
     * @param ids - Array of authentication provider IDs
     * @returns Observable array of authentication providers
     */
    getByIds(ids: string[]): Observable<AuthenticationProviderDTO[]> {
        if (!ids || ids.length === 0) {
            return of([]);
        }

        // Use forkJoin to make parallel requests for each ID
        const requests = ids.map(id => this.getById(id));
        return forkJoin(requests);
    }

    /**
     * Get authentication providers for a specific user
     * @param userId - User ID
     * @returns Observable array of authentication providers
     */
    getByUserId(userId: string): Observable<AuthenticationProviderDTO[]> {
        return this.getWithParams('by-user', {userId});
    }
}