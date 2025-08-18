import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {
    InterviewStageListComponent
} from '../../components/interview-stage/interview-stage-list/interview-stage-list.component';

const routes: Routes = [
    {
        path: '',
        component: InterviewStageListComponent
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class InterviewStageRoutingModule {
}