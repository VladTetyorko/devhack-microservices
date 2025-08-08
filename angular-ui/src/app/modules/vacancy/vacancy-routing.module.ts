import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {VacancyListComponent} from '../../components/vacancy/vacancy-list/vacancy-list.component';
import {VacancyDetailComponent} from '../../components/vacancy/vacancy-detail/vacancy-detail.component';

const routes: Routes = [
    {path: '', component: VacancyListComponent},
    {path: ':id', component: VacancyDetailComponent},
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class VacancyRoutingModule {
}