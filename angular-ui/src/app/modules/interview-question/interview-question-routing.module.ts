import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InterviewQuestionListComponent } from '../../components/interview-question/interview-question-list/interview-question-list.component';

const routes: Routes = [
  {
    path: '',
    component: InterviewQuestionListComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InterviewQuestionRoutingModule { }