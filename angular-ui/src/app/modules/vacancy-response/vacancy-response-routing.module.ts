import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { VacancyResponseListComponent } from '../../components/vacancy-response/vacancy-response-list/vacancy-response-list.component';
import { VacancyResponseDetailComponent } from '../../components/vacancy-response/vacancy-response-detail/vacancy-response-detail.component';

const routes: Routes = [
  { path: 'my-responses', component: VacancyResponseListComponent },
  { path: 'my-responses/:id', component: VacancyResponseDetailComponent },
  { path: '', redirectTo: 'my-responses', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class VacancyResponseRoutingModule { }
