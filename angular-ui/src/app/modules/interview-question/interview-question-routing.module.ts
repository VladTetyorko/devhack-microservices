import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {
    InterviewQuestionListComponent
} from '../../components/interview-question/interview-question-list/interview-question-list.component';
import {
    InterviewQuestionDetailComponent
} from '../../components/interview-question/interview-question-detail/interview-question-detail.component';

const routes: Routes = [
    {path: '', component: InterviewQuestionListComponent},
    {path: ':id', component: InterviewQuestionDetailComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InterviewQuestionRoutingModule { }
