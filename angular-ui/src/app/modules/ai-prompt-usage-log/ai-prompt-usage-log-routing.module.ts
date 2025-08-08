import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {
    AiPromptUsageLogListComponent
} from '../../components/ai-prompt-usage-log/ai-prompt-usage-log-list/ai-prompt-usage-log-list.component';
import {
    AiPromptUsageLogDetailComponent
} from '../../components/ai-prompt-usage-log/ai-prompt-usage-log-detail/ai-prompt-usage-log-detail.component';

const routes: Routes = [
    {path: '', component: AiPromptUsageLogListComponent},
    {path: ':id', component: AiPromptUsageLogDetailComponent}
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AiPromptUsageLogRoutingModule {
}