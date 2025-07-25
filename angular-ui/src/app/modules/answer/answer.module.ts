import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { AnswerListComponent } from '../../components/answer/answer-list/answer-list.component';

import { AnswerRoutingModule } from './answer-routing.module';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    AnswerListComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    AnswerRoutingModule,
    SharedModule
  ],
  exports: [
    AnswerListComponent
  ]
})
export class AnswerModule { }