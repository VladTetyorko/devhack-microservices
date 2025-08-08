import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {SharedModule} from '../shared/shared.module';
import {
    AiPromptUsageLogListComponent
} from '../../components/ai-prompt-usage-log/ai-prompt-usage-log-list/ai-prompt-usage-log-list.component';
import {
    AiPromptUsageLogDetailComponent
} from '../../components/ai-prompt-usage-log/ai-prompt-usage-log-detail/ai-prompt-usage-log-detail.component';
import {AiPromptUsageLogRoutingModule} from './ai-prompt-usage-log-routing.module';

@NgModule({
    declarations: [
        AiPromptUsageLogListComponent,
        AiPromptUsageLogDetailComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        AiPromptUsageLogRoutingModule,
        SharedModule
    ]
})
export class AiPromptUsageLogModule {
}
