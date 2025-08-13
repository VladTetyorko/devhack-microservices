import {AuthService} from "../../../services/basic/auth.service";
import {Router} from "@angular/router";
import {UserDTO} from "../../../models/user/user.model";
import {Subscription} from "rxjs";
import {AuthState} from "../../../models/basic/auth.model";
import {Directive, OnDestroy, OnInit} from "@angular/core";

@Directive()
export class NavbarBase implements OnInit, OnDestroy {
    isMenuCollapsed = true;
    isAuthenticated = false;
    currentUser: UserDTO | null = null;
    private authSubscription: Subscription | null = null;

    constructor(
        protected authService: AuthService,
        private router: Router
    ) {
    }

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