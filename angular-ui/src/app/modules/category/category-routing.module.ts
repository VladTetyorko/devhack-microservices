import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {CategoryListComponent} from '../../components/category/category-list/category-list.component';
import {CategoryDetailComponent} from '../../components/category/category-detail/category-detail.component';
import {CategoryCreateComponent} from '../../components/category/category-create/category-create.component';
import {CategoryEditComponent} from '../../components/category/category-edit/category-edit.component';

const routes: Routes = [
    {
        path: '',
        component: CategoryListComponent
    },
    {
        path: 'create',
        component: CategoryCreateComponent
    },
    {
        path: ':id',
        component: CategoryDetailComponent
    },
    {
        path: ':id/edit',
        component: CategoryEditComponent
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class CategoryRoutingModule {
}