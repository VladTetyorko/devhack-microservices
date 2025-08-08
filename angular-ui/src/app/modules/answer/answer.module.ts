import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {AnswerListComponent} from '../../components/answer/answer-list/answer-list.component';
import {AnswerDetailComponent} from '../../components/answer/answer-detail/answer-detail.component';

import {AnswerRoutingModule} from './answer-routing.module';
import {SharedModule} from '../shared/shared.module';

@NgModule({
  declarations: [
      AnswerListComponent,
      AnswerDetailComponent
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
      AnswerListComponent,
      AnswerDetailComponent
  ]
})
export class AnswerModule { }
