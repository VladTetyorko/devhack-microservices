import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {
    VacancyResponseListComponent
} from '../../components/vacancy-response/vacancy-response-list/vacancy-response-list.component';
import {
    VacancyResponseDetailComponent
} from '../../components/vacancy-response/vacancy-response-detail/vacancy-response-detail.component';

import {VacancyResponseRoutingModule} from './vacancy-response-routing.module';
import {SharedModule} from '../shared/shared.module';

@NgModule({
    declarations: [
        VacancyResponseListComponent,
        VacancyResponseDetailComponent
    ],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterModule,
        VacancyResponseRoutingModule,
        SharedModule
    ],
    exports: [
        VacancyResponseListComponent,
        VacancyResponseDetailComponent
    ]
})
export class VacancyResponseModule {
}
