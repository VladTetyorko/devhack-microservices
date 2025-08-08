import {Component, OnInit} from '@angular/core';
import {AuthService} from "../services/basic/auth.service";
import {UserDTO} from "../models/user/user.model";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
    currentUser?: UserDTO | null;

    constructor(public auth: AuthService) {
    }

    ngOnInit(): void {
        this.currentUser = this.auth.getCurrentUser()
    }

    get isAuthenticated(): boolean {
        return this.auth.isAuthenticated();
    }
}
