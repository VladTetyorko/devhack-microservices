import {NgModule} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {RouterModule} from '@angular/router';

import {UserNavbarComponent} from '../../components/shared/navbar/user-navbar/user-navbar.component';
import {AdminNavbarComponent} from '../../components/shared/navbar/admin-navbar/admin-navbar.component';
import {FooterComponent} from '../../components/shared/footer/footer.component';

import {PaginationComponent} from '../../components/shared/pagination/pagination.component';
import {ActionButtonsComponent} from '../../components/shared/action-buttons/action-buttons.component';
import {LocalDateTimePipe} from '../../pipes/local-date-time.pipe';

@NgModule({
    declarations: [
        UserNavbarComponent,
        AdminNavbarComponent,
        FooterComponent,
        PaginationComponent,
        ActionButtonsComponent,
        LocalDateTimePipe
    ],
    imports: [
        CommonModule,
        RouterModule
    ],
    providers: [
        DatePipe
    ],
    exports: [
        UserNavbarComponent,
        AdminNavbarComponent,
        FooterComponent,
        PaginationComponent,
        ActionButtonsComponent,
        LocalDateTimePipe
    ]
})
export class SharedModule {
}
