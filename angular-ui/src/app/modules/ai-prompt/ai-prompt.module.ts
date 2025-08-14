import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {SharedModule} from '../shared/shared.module';
import {AiPromptListComponent} from '../../components/ai-prompt/ai-prompt-list/ai-prompt-list.component';
import {AiPromptDetailComponent} from '../../components/ai-prompt/ai-prompt-detail/ai-prompt-detail.component';
import {AiPromptCreateComponent} from '../../components/ai-prompt/ai-prompt-create/ai-prompt-create.component';
import {AiPromptEditComponent} from '../../components/ai-prompt/ai-prompt-edit/ai-prompt-edit.component';
import {AiPromptRoutingModule} from './ai-prompt-routing.module';

@NgModule({
    declarations: [
        AiPromptListComponent,
        AiPromptDetailComponent,
        AiPromptCreateComponent,
        AiPromptEditComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        AiPromptRoutingModule,
        SharedModule
    ]
})
export class AiPromptModule {
}
