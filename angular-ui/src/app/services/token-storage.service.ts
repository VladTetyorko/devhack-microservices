import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class TokenStorageService {
    private readonly TOKEN_KEY = 'auth_token';
    private readonly USER_KEY = 'auth_user';

    constructor() {
    }

    signOut(): void {
        window.localStorage.clear();
    }

    public saveToken(token: string): void {
        window.localStorage.removeItem(this.TOKEN_KEY);
        window.localStorage.setItem(this.TOKEN_KEY, token);
    }

    public getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    public saveUser(user: any): void {
        window.localStorage.removeItem(this.USER_KEY);
        window.localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }

    public getUser(): any {
        const user = window.localStorage.getItem(this.USER_KEY);
        if (user) {
            return JSON.parse(user);
        }

        return null;
    }
}
