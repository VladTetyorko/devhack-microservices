import {Component, OnInit} from '@angular/core';
import {AuthService} from './services/basic/auth.service';
import {Observable} from "rxjs";
import {map} from "rxjs/operators";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    title = 'DevHack Application';
    isAdmin$!: Observable<boolean>;

    constructor(public auth: AuthService) {
    }

    ngOnInit() {
        this.auth.loadRoleFromStorage();

        this.isAdmin$ = this.auth.currentRole$.pipe(
            map(role => role === 'ADMIN')
        );
    }

}
