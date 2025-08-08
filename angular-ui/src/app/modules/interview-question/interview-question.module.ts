import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {
    InterviewQuestionListComponent
} from '../../components/interview-question/interview-question-list/interview-question-list.component';
import {
    InterviewQuestionDetailComponent
} from '../../components/interview-question/interview-question-detail/interview-question-detail.component';

import {InterviewQuestionRoutingModule} from './interview-question-routing.module';
import {SharedModule} from '../shared/shared.module';

@NgModule({
  declarations: [
      InterviewQuestionListComponent,
      InterviewQuestionDetailComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    InterviewQuestionRoutingModule,
    SharedModule
  ],
  exports: [
      InterviewQuestionListComponent,
      InterviewQuestionDetailComponent
  ]
})
export class InterviewQuestionModule { }
