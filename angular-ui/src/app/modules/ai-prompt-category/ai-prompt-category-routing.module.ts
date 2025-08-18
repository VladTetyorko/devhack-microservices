import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

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

const routes: Routes = [
    {path: '', component: AiPromptCategoryListComponent},
    {path: 'create', component: AiPromptCategoryCreateComponent},
    {path: ':id/edit', component: AiPromptCategoryEditComponent},
    {path: ':id', component: AiPromptCategoryDetailComponent}
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AiPromptCategoryRoutingModule {
}