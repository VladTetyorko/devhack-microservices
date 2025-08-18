import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {AiPromptListComponent} from '../../components/ai-prompt/ai-prompt-list/ai-prompt-list.component';
import {AiPromptDetailComponent} from '../../components/ai-prompt/ai-prompt-detail/ai-prompt-detail.component';
import {AiPromptCreateComponent} from '../../components/ai-prompt/ai-prompt-create/ai-prompt-create.component';
import {AiPromptEditComponent} from '../../components/ai-prompt/ai-prompt-edit/ai-prompt-edit.component';

const routes: Routes = [
    {path: '', component: AiPromptListComponent},
    {path: 'create', component: AiPromptCreateComponent},
    {path: ':id/edit', component: AiPromptEditComponent},
    {path: ':id', component: AiPromptDetailComponent}
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AiPromptRoutingModule {
}