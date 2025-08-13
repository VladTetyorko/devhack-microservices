import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {CategoryListComponent} from '../../components/category/category-list/category-list.component';
import {CategoryDetailComponent} from '../../components/category/category-detail/category-detail.component';
import {CategoryCreateComponent} from '../../components/category/category-create/category-create.component';
import {CategoryEditComponent} from '../../components/category/category-edit/category-edit.component';
import {CategoryRoutingModule} from './category-routing.module';

@NgModule({
    declarations: [
        CategoryListComponent,
        CategoryDetailComponent,
        CategoryCreateComponent,
        CategoryEditComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        CategoryRoutingModule
    ],
    exports: [
        CategoryListComponent,
        CategoryDetailComponent,
        CategoryCreateComponent,
        CategoryEditComponent
    ]
})
export class CategoryModule {
}