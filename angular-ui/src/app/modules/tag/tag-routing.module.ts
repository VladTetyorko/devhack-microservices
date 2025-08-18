import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TagListComponent} from '../../components/tag/tag-list/tag-list.component';
import {TagDetailComponent} from '../../components/tag/tag-detail/tag-detail.component';
import {TagCreateComponent} from '../../components/tag/tag-create/tag-create.component';
import {TagEditComponent} from '../../components/tag/tag-edit/tag-edit.component';

const routes: Routes = [
    {
        path: '',
        component: TagListComponent
    },
    {
        path: 'create',
        component: TagCreateComponent
    },
    {
        path: ':id',
        component: TagDetailComponent
    },
    {
        path: ':id/edit',
        component: TagEditComponent
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class TagRoutingModule {
}