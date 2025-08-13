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
import {
    InterviewQuestionCreateComponent
} from '../../components/interview-question/interview-question-create/interview-question-create.component';
import {
    InterviewQuestionEditComponent
} from '../../components/interview-question/interview-question-edit/interview-question-edit.component';
import {
    QuestionGenerationStatsComponent
} from '../../components/interview-question/question-generation-stats/question-generation-stats.component';
import {
    QuestionGenerationFormComponent
} from '../../components/interview-question/question-generation-form/question-generation-form.component';

import {InterviewQuestionRoutingModule} from './interview-question-routing.module';
import {SharedModule} from '../shared/shared.module';

@NgModule({
  declarations: [
      InterviewQuestionListComponent,
      InterviewQuestionDetailComponent,
      InterviewQuestionCreateComponent,
      InterviewQuestionEditComponent,
      QuestionGenerationStatsComponent,
      QuestionGenerationFormComponent
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
      InterviewQuestionDetailComponent,
      InterviewQuestionCreateComponent,
      InterviewQuestionEditComponent,
      QuestionGenerationStatsComponent,
      QuestionGenerationFormComponent
  ]
})
export class InterviewQuestionModule { }
