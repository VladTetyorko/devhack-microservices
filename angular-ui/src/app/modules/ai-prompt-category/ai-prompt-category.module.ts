import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {SharedModule} from '../shared/shared.module';
import {
    AiPromptCategoryListComponent
} from '../../components/ai-prompt-category/ai-prompt-category-list/ai-prompt-category-list.component';
import {
    AiPromptCategoryDetailComponent
} from '../../components/ai-prompt-category/ai-prompt-category-detail/ai-prompt-category-detail.component';
import {AiPromptCategoryRoutingModule} from './ai-prompt-category-routing.module';

@NgModule({
    declarations: [
        AiPromptCategoryListComponent,
        AiPromptCategoryDetailComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        AiPromptCategoryRoutingModule,
        SharedModule
    ]
})
export class AiPromptCategoryModule {
}
