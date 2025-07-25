import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { InterviewStageListComponent } from '../../components/interview-stage/interview-stage-list/interview-stage-list.component';

import { InterviewStageRoutingModule } from './interview-stage-routing.module';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    InterviewStageListComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    InterviewStageRoutingModule,
    SharedModule
  ],
  exports: [
    InterviewStageListComponent
  ]
})
export class InterviewStageModule { }