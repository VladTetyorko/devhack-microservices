import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {AiPromptListComponent} from '../../components/ai-prompt/ai-prompt-list/ai-prompt-list.component';
import {AiPromptDetailComponent} from '../../components/ai-prompt/ai-prompt-detail/ai-prompt-detail.component';

const routes: Routes = [
    {path: '', component: AiPromptListComponent},
    {path: ':id', component: AiPromptDetailComponent}
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AiPromptRoutingModule {
}