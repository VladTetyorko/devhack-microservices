import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {SharedModule} from '../shared/shared.module';
import {AuditListComponent} from '../../components/audit/audit-list/audit-list.component';
import {AuditRoutingModule} from './audit-routing.module';

@NgModule({
    declarations: [
        AuditListComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        AuditRoutingModule,
        SharedModule
    ]
})
export class AuditModule {
}
