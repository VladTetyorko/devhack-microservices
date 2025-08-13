import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {
    InterviewQuestionListComponent
} from '../../components/interview-question/interview-question-list/interview-question-list.component';
import {
    InterviewQuestionDetailComponent
} from '../../components/interview-question/interview-question-detail/interview-question-detail.component';
import {
    InterviewQuestionCreateComponent
} from '../../components/interview-question/interview-question-create/interview-question-create.component';
import {
    InterviewQuestionEditComponent
} from '../../components/interview-question/interview-question-edit/interview-question-edit.component';
import {
    QuestionGenerationStatsComponent
} from '../../components/interview-question/question-generation-stats/question-generation-stats.component';
import {
    QuestionGenerationFormComponent
} from '../../components/interview-question/question-generation-form/question-generation-form.component';

const routes: Routes = [
    {path: '', component: InterviewQuestionListComponent},
    {path: 'stats', component: QuestionGenerationStatsComponent},
    {path: 'generate', component: QuestionGenerationFormComponent},
    {path: 'create', component: InterviewQuestionCreateComponent},
    {path: ':id', component: InterviewQuestionDetailComponent},
    {path: ':id/edit', component: InterviewQuestionEditComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InterviewQuestionRoutingModule { }
