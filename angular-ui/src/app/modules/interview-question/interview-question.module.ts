import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { InterviewQuestionListComponent } from '../../components/interview-question/interview-question-list/interview-question-list.component';

import { InterviewQuestionRoutingModule } from './interview-question-routing.module';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    InterviewQuestionListComponent
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
    InterviewQuestionListComponent
  ]
})
export class InterviewQuestionModule { }