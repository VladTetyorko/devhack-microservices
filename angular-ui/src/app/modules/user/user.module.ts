import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {UserListComponent} from '../../components/user/user-list/user-list.component';
import {UserDetailComponent} from '../../components/user/user-detail/user-detail.component';
import {UserRegisterComponent} from '../../components/user/user-register/user-register.component';

import {UserRoutingModule} from './user-routing.module';
import {SharedModule} from '../shared/shared.module';

@NgModule({
    declarations: [
        UserListComponent,
        UserDetailComponent,
        UserRegisterComponent
    ],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        RouterModule,
        UserRoutingModule,
        SharedModule
    ],
    exports: [
        UserListComponent,
        UserDetailComponent,
        UserRegisterComponent
    ]
})
export class UserModule {
}
