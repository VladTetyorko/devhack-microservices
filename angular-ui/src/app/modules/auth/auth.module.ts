import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {LoginComponent} from '../../components/login/login.component';
import {RegisterComponent} from '../../components/register/register.component';

import {AuthRoutingModule} from './auth-routing.module';

@NgModule({
    declarations: [
        LoginComponent,
        RegisterComponent
    ],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterModule,
        AuthRoutingModule
    ],
    exports: [
        LoginComponent,
        RegisterComponent
    ]
})
export class AuthModule {
}