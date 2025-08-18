import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {
    InterviewStageCategoryListComponent
} from '../../components/interview-stage-category/interview-stage-category-list/interview-stage-category-list.component';

import {InterviewStageCategoryRoutingModule} from './interview-stage-category-routing.module';
import {SharedModule} from '../shared/shared.module';

@NgModule({
    declarations: [
        InterviewStageCategoryListComponent
    ],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        RouterModule,
        InterviewStageCategoryRoutingModule,
        SharedModule
    ],
    exports: [
        InterviewStageCategoryListComponent
    ]
})
export class InterviewStageCategoryModule {
}