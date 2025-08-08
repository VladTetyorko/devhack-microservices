import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InterviewStageCategoryListComponent } from '../../components/interview-stage-category/interview-stage-category-list/interview-stage-category-list.component';

const routes: Routes = [
  {
    path: '',
    component: InterviewStageCategoryListComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InterviewStageCategoryRoutingModule { }