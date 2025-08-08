import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserDTO} from '../../models/user/user.model';
import {BaseService} from '../base.service';

/**
 * Service for managing users.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class UserService extends BaseService<UserDTO> {
    protected baseUrl = '/api/users';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Find user by email address
     * @param email - Email address to search for
     * @returns Observable user
     */
    findByEmail(email: string): Observable<UserDTO> {
        const params = new HttpParams().set('email', email);
        return this.http.get<UserDTO>(`${this.baseUrl}/by-email`, {params});
    }

    /**
     * Register a new user
     * @param user - User data for registration
     * @returns Observable registered user
     */
    register(user: UserDTO): Observable<UserDTO> {
        return this.postToEndpoint('register', user);
    }

    /**
     * Register a new manager user
     * @param user - User data for manager registration
     * @returns Observable registered manager user
     */
    registerManager(user: UserDTO): Observable<UserDTO> {
        return this.postToEndpoint('register-manager', user);
    }
}
