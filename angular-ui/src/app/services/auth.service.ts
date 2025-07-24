import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, AuthState } from '../models/auth.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authStateSubject = new BehaviorSubject<AuthState>(this.loadAuthState());
  public authState$ = this.authStateSubject.asObservable();

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService
  ) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', credentials)
      .pipe(
        tap(response => {
          if (response.token) {
            this.tokenStorage.saveToken(response.token);
            if (response.user) {
              this.tokenStorage.saveUser(response.user);
            }
            this.setAuthState({
              isAuthenticated: true,
              token: response.token,
              user: response.user
            });
          }
        })
      );
  }

  logout(): void {
    this.tokenStorage.signOut();
    this.setAuthState({
      isAuthenticated: false,
      token: null,
      user: null
    });
  }

  isAuthenticated(): boolean {
    return this.authStateSubject.value.isAuthenticated;
  }

  getToken(): string | null {
    return this.tokenStorage.getToken();
  }

  getCurrentUser(): any | null {
    return this.tokenStorage.getUser();
  }

  private setAuthState(state: AuthState): void {
    this.authStateSubject.next(state);
  }

  private loadAuthState(): AuthState {
    const token = this.tokenStorage.getToken();
    const user = this.tokenStorage.getUser();

    return {
      isAuthenticated: !!token,
      token,
      user
    };
  }
}
