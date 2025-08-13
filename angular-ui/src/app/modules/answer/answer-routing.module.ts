import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {AnswerListComponent} from '../../components/answer/answer-list/answer-list.component';
import {AnswerDetailComponent} from '../../components/answer/answer-detail/answer-detail.component';
import {AnswerCreateComponent} from '../../components/answer/answer-create/answer-create.component';

const routes: Routes = [
    {path: '', component: AnswerListComponent},
    {path: 'create', component: AnswerCreateComponent},
    {path: ':id', component: AnswerDetailComponent}
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AnswerRoutingModule {
}