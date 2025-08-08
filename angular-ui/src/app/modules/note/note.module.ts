import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {NoteListComponent} from '../../components/note/note-list/note-list.component';
import {NoteByQuestionComponent} from '../../components/note/note-by-question/note-by-question.component';

import {NoteRoutingModule} from './note-routing.module';
import {SharedModule} from '../shared/shared.module';

@NgModule({
  declarations: [
    NoteListComponent,
    NoteByQuestionComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
      NoteRoutingModule,
      SharedModule
  ],
  exports: [
    NoteListComponent,
    NoteByQuestionComponent
  ]
})
export class NoteModule { }
