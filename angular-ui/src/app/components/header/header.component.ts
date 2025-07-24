import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';
import { AuthState } from '../../models/auth.model';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {
  isAuthenticated = false;
  currentUser: any = null;
  private authSubscription: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.authSubscription = this.authService.authState$.subscribe(
      (authState: AuthState) => {
        this.isAuthenticated = authState.isAuthenticated;
        this.currentUser = authState.user;
      }
    );
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
