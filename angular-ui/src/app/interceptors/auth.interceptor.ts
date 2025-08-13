import {Injectable, Injector} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {catchError, Observable, throwError} from 'rxjs';
import {Router} from '@angular/router';
import {AuthService} from '../services/basic/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
      private injector: Injector,
      private router: Router
  ) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Get AuthService lazily to avoid circular dependency
    const authService = this.injector.get(AuthService);

    // Get JWT token from auth service
    const token = authService.getToken();

    // Clone the request and add Authorization header if token exists
    let authRequest: HttpRequest<any>;
    if (token) {
      authRequest = request.clone({
        setHeaders: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });
    } else {
      authRequest = request.clone({
        setHeaders: {
          'Content-Type': 'application/json'
        }
      });
    }

    return next.handle(authRequest).pipe(
        catchError(error => {
          // Handle 401 Unauthorized responses
          if (error.status === 401) {
            // Get AuthService lazily to avoid circular dependency
            const authService = this.injector.get(AuthService);

            // Clear local auth state and redirect to login
            authService.logout().subscribe({
              complete: () => {
                this.router.navigate(['/login'], {
                  queryParams: {returnUrl: this.router.url}
                });
              }
            });
          }
          return throwError(() => error);
        })
    );
  }
}
