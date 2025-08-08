import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {SharedModule} from '../shared/shared.module';
import {AiPromptListComponent} from '../../components/ai-prompt/ai-prompt-list/ai-prompt-list.component';
import {AiPromptDetailComponent} from '../../components/ai-prompt/ai-prompt-detail/ai-prompt-detail.component';
import {AiPromptRoutingModule} from './ai-prompt-routing.module';

@NgModule({
    declarations: [
        AiPromptListComponent,
        AiPromptDetailComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        AiPromptRoutingModule,
        SharedModule
    ]
})
export class AiPromptModule {
}
