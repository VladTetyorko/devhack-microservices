import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {SharedModule} from '../shared/shared.module';
import {
    AiPromptCategoryListComponent
} from '../../components/ai-prompt-category/ai-prompt-category-list/ai-prompt-category-list.component';
import {
    AiPromptCategoryDetailComponent
} from '../../components/ai-prompt-category/ai-prompt-category-detail/ai-prompt-category-detail.component';
import {
    AiPromptCategoryCreateComponent
} from '../../components/ai-prompt-category/ai-prompt-category-create/ai-prompt-category-create.component';
import {
    AiPromptCategoryEditComponent
} from '../../components/ai-prompt-category/ai-prompt-category-edit/ai-prompt-category-edit.component';
import {AiPromptCategoryRoutingModule} from './ai-prompt-category-routing.module';

@NgModule({
    declarations: [
        AiPromptCategoryListComponent,
        AiPromptCategoryDetailComponent,
        AiPromptCategoryCreateComponent,
        AiPromptCategoryEditComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        AiPromptCategoryRoutingModule,
        SharedModule
    ]
})
export class AiPromptCategoryModule {
}
