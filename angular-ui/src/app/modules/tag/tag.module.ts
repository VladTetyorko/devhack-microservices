import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { TagListComponent } from '../../components/tag/tag-list/tag-list.component';

import { TagRoutingModule } from './tag-routing.module';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    TagListComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    TagRoutingModule,
    SharedModule
  ],
  exports: [
    TagListComponent
  ]
})
export class TagModule { }