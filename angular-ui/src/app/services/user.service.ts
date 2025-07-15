import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserDTO} from '../models/user.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private baseUrl = '/api/users';

    constructor(private http: HttpClient) {
    }

    getAll(): Observable<UserDTO[]> {
        return this.http.get<UserDTO[]>(this.baseUrl);
    }

    getById(id: string): Observable<UserDTO> {
        return this.http.get<UserDTO>(`${this.baseUrl}/${id}`);
    }

    findByEmail(email: string): Observable<UserDTO> {
        const params = new HttpParams().set('email', email);
        return this.http.get<UserDTO>(`${this.baseUrl}/by-email`, {params});
    }

    register(user: UserDTO): Observable<UserDTO> {
        return this.http.post<UserDTO>(`${this.baseUrl}/register`, user);
    }

    registerManager(user: UserDTO): Observable<UserDTO> {
        return this.http.post<UserDTO>(`${this.baseUrl}/register-manager`, user);
    }

    update(id: string, user: UserDTO): Observable<UserDTO> {
        return this.http.put<UserDTO>(`${this.baseUrl}/${id}`, user);
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
}
