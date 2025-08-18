import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {UserListComponent} from '../../components/user/user-list/user-list.component';
import {UserDetailComponent} from '../../components/user/user-detail/user-detail.component';
import {UserRegisterComponent} from '../../components/user/user-register/user-register.component';

const routes: Routes = [
    {path: '', component: UserListComponent},
    {path: 'register', component: UserRegisterComponent},
    {path: ':id', component: UserDetailComponent}
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class UserRoutingModule {
}
