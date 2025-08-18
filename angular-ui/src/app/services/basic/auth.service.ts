import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, catchError, Observable, of, tap} from 'rxjs';
import {
    AuthState,
    LoginRequest,
    LoginResponse,
    PasswordResetConfirm,
    PasswordResetRequest,
    RegisterRequest,
    RegisterResponse,
    UserDTO
} from '../../models/basic/auth.model';
import {UserAccessDTO} from '../../models/user/user-access.model';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private authStateSubject = new BehaviorSubject<AuthState>(this.loadAuthState());
    private roleSubject = new BehaviorSubject<'USER' | 'ADMIN' | null>(null);

    public authState$ = this.authStateSubject.asObservable();
    public currentRole$ = this.roleSubject.asObservable();

    private readonly API_BASE = '/api/auth';

    constructor(
        private http: HttpClient
    ) {
    }

    login(credentials: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.API_BASE}/login`, credentials).pipe(
            tap(response => {
                if (response.success && response.user) {
                    // Store JWT tokens
                    if (response.accessToken) {
                        this.saveTokenToStorage(response.accessToken);
                    }
                    if (response.refreshToken) {
                        this.saveRefreshTokenToStorage(response.refreshToken);
                    }

                    this.saveUserToStorage(response.user);
                    this.setAuthState({
                        isAuthenticated: true,
                        user: response.user,
                        roles: response.roles || []
                    });
                    (this.getUserRoles().includes("ROLE_SYSTEM") || this.getUserRoles().includes("ROLE_MANAGER")) ?
                        this.setRole('ADMIN') : this.setRole('USER');
                }
            }),
            catchError(error => {
                console.error('Login error:', error);
                throw error;
            })
        );
    }

    register(registerData: RegisterRequest): Observable<RegisterResponse> {
        return this.http.post<RegisterResponse>(`${this.API_BASE}/register`, registerData)
            .pipe(
                catchError(error => {
                    console.error('Registration error:', error);
                    throw error;
                })
            );
    }

    requestPasswordReset(resetData: PasswordResetRequest): Observable<any> {
        return this.http.post(`${this.API_BASE}/password-reset/request`, resetData)
            .pipe(
                catchError(error => {
                    console.error('Password reset request error:', error);
                    throw error;
                })
            );
    }

    confirmPasswordReset(confirmData: PasswordResetConfirm): Observable<any> {
        return this.http.post(`${this.API_BASE}/password-reset/confirm`, confirmData)
            .pipe(
                catchError(error => {
                    console.error('Password reset confirm error:', error);
                    throw error;
                })
            );
    }

    logout(): Observable<any> {
        // For JWT authentication, logout is handled client-side
        this.clearAllFromStorage();
        this.setAuthState({
            isAuthenticated: false,
            user: null,
            roles: []
        });
        return of(null);
    }

    isAuthenticated(): boolean {
        return this.authStateSubject.value.isAuthenticated;
    }

    getCurrentUser(): UserDTO | null {
        return this.authStateSubject.value.user;
    }

    getUserRoles(): string[] {
        return this.authStateSubject.value.roles;
    }

    hasRole(role: string): boolean {
        return this.getUserRoles().includes(role);
    }

    hasAnyRole(roles: string[]): boolean {
        const userRoles = this.getUserRoles();
        return roles.some(role => userRoles.includes(role));
    }

    getToken(): string | null {
        return localStorage.getItem('accessToken');
    }

    getRefreshToken(): string | null {
        return localStorage.getItem('refreshToken');
    }

    private setAuthState(state: AuthState): void {
        this.authStateSubject.next(state);
    }

    private loadAuthState(): AuthState {
        const user = this.getUserFromStorage();
        const token = this.getToken();

        // Check if we have both user and valid token
        const isAuthenticated = !!(user && token && !this.isTokenExpired(token));

        // Load user access data asynchronously to get roles
        if (isAuthenticated && user?.accessId) {
            this.loadUserAccessAndUpdateRoles(user.accessId);
        }

        return {
            isAuthenticated,
            user: isAuthenticated ? user : null,
            roles: [] // Roles will be loaded asynchronously
        };
    }

    /**
     * Load user access data by ID and update the auth state with roles
     */
    private loadUserAccessAndUpdateRoles(accessId: string): void {
        this.http.get<UserAccessDTO>(`/api/user-access/${accessId}`).subscribe({
            next: (userAccess) => {
                const currentState = this.authStateSubject.value;
                const roles = userAccess.role ? [userAccess.role] : [];

                // Update auth state with loaded roles
                this.setAuthState({
                    ...currentState,
                    roles: roles
                });

                // Set role for UI navigation
                (roles.includes("ROLE_SYSTEM") || roles.includes("ROLE_MANAGER")) ?
                    this.setRole('ADMIN') : this.setRole('USER');
            },
            error: (err) => {
                console.error('Error loading user access data:', err);
                // Keep empty roles on error
            }
        });
    }

    private isTokenExpired(token: string): boolean {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const expiry = payload.exp * 1000; // Convert to milliseconds
            return Date.now() > expiry;
        } catch (e) {
            return true; // If we can't parse the token, consider it expired
        }
    }

    private saveTokenToStorage(token: string): void {
        localStorage.setItem('accessToken', token);
    }

    private saveRefreshTokenToStorage(token: string): void {
        localStorage.setItem('refreshToken', token);
    }

    private saveUserToStorage(user: UserDTO): void {
        localStorage.setItem('currentUser', JSON.stringify(user));
    }

    private getUserFromStorage(): UserDTO | null {
        const userStr = localStorage.getItem('currentUser');
        if (userStr) {
            try {
                return JSON.parse(userStr);
            } catch (e) {
                console.error('Error parsing user from storage:', e);
                this.clearAllFromStorage();
            }
        }
        return null;
    }

    private clearUserFromStorage(): void {
        localStorage.removeItem('currentUser');
    }

    private clearAllFromStorage(): void {
        localStorage.removeItem('currentUser');
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
    }

    setRole(role: 'USER' | 'ADMIN') {
        this.roleSubject.next(role);
    }

    loadRoleFromStorage() {
        const stored = localStorage.getItem('role') as 'USER' | 'ADMIN' | null;
        if (stored) this.roleSubject.next(stored);
    }

}
