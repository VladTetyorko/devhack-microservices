import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {TagListComponent} from '../../components/tag/tag-list/tag-list.component';
import {TagDetailComponent} from '../../components/tag/tag-detail/tag-detail.component';
import {TagCreateComponent} from '../../components/tag/tag-create/tag-create.component';
import {TagEditComponent} from '../../components/tag/tag-edit/tag-edit.component';
import {TagHierarchyTreeComponent} from '../../components/tag/tag-hierarchy-tree/tag-hierarchy-tree.component';

import {TagRoutingModule} from './tag-routing.module';
import {SharedModule} from '../shared/shared.module';

@NgModule({
    declarations: [
        TagListComponent,
        TagDetailComponent,
        TagCreateComponent,
        TagEditComponent,
        TagHierarchyTreeComponent
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
        TagListComponent,
        TagDetailComponent,
        TagCreateComponent,
        TagEditComponent,
        TagHierarchyTreeComponent
    ]
})
export class TagModule {
}