import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {VacancyListComponent} from '../../components/vacancy/vacancy-list/vacancy-list.component';
import {VacancyDetailComponent} from '../../components/vacancy/vacancy-detail/vacancy-detail.component';
import {VacancyEditComponent} from '../../components/vacancy/vacancy-edit/vacancy-edit.component';

import {VacancyRoutingModule} from './vacancy-routing.module';
import {SharedModule} from '../shared/shared.module';

@NgModule({
    declarations: [
        VacancyListComponent,
        VacancyDetailComponent,
        VacancyEditComponent
    ],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterModule,
        VacancyRoutingModule,
        SharedModule
    ],
    exports: [
        VacancyListComponent,
        VacancyDetailComponent
    ]
})
export class VacancyModule {
}
