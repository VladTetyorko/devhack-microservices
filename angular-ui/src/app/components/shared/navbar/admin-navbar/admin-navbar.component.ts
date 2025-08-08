import {Router} from "@angular/router";
import {Component} from "@angular/core";
import {AuthService} from "../../../../services/basic/auth.service";
import {NavbarBase} from "../navbar.base";

@Component({
    selector: 'admin-app-navbar',
    templateUrl: './admin-navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class AdminNavbarComponent extends NavbarBase {
    constructor(auth: AuthService, router: Router) {
        super(auth, router);
    }
}