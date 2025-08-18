import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserDTO} from '../../models/user/user.model';
import {BaseService} from '../base.service';

/**
 * Registration request interface for creating new users
 */
export interface UserRegistrationRequest {
    name: string;
    email: string;
    role: string;
}

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
     * @param registrationData - User registration data
     * @returns Observable registered user
     */
    register(registrationData: UserRegistrationRequest): Observable<UserDTO> {
        return this.postToEndpoint('register', registrationData);
    }

    /**
     * Register a new manager user
     * @param registrationData - User registration data for manager
     * @returns Observable registered manager user
     */
    registerManager(registrationData: UserRegistrationRequest): Observable<UserDTO> {
        return this.postToEndpoint('register-manager', registrationData);
    }
}
