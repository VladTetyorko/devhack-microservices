import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

// Guards
import {AuthGuard} from './guards/auth.guard';
import {HomeComponent} from "./home/home.component";

const routes: Routes = [
    // Auth routes (public)
    {
        path: 'auth',
        loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule)
    },
    {
        path: 'login',
        loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule)
    },
    {
        path: 'register',
        loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule)
    },

    // User routes (protected)
    {
        path: 'users',
        loadChildren: () => import('./modules/user/user.module').then(m => m.UserModule),
        canActivate: [AuthGuard]
    },

    // Note routes (protected)
    {
        path: 'notes',
        loadChildren: () => import('./modules/note/note.module').then(m => m.NoteModule),
        canActivate: [AuthGuard]
    },

    // Category routes (protected)
    {
        path: 'categories',
        loadChildren: () => import('./modules/category/category.module').then(m => m.CategoryModule),
        canActivate: [AuthGuard]
    },

    // Topic routes (protected)
    {
        path: 'topics',
        loadChildren: () => import('./modules/tag/tag.module').then(m => m.TagModule),
        canActivate: [AuthGuard]
    },

    // Interview Question routes (protected)
    {
        path: 'interview-questions',
        loadChildren: () => import('./modules/interview-question/interview-question.module').then(m => m.InterviewQuestionModule),
        canActivate: [AuthGuard]
    },

    // Answer routes (protected)
    {
        path: 'answers',
        loadChildren: () => import('./modules/answer/answer.module').then(m => m.AnswerModule),
        canActivate: [AuthGuard]
    },

    // Interview Stage routes (protected)
    {
        path: 'interview-stages',
        loadChildren: () => import('./modules/interview-stage/interview-stage.module').then(m => m.InterviewStageModule),
        canActivate: [AuthGuard]
    },

    // Interview Stage Category routes (protected)
    {
        path: 'interview-stage-categories',
        loadChildren: () => import('./modules/interview-stage-category/interview-stage-category.module').then(m => m.InterviewStageCategoryModule),
        canActivate: [AuthGuard]
    },

    // Vacancy routes (protected)
    {
        path: 'vacancies',
        loadChildren: () => import('./modules/vacancy/vacancy.module').then(m => m.VacancyModule),
        canActivate: [AuthGuard]
    },

    // Vacancy Response routes (protected)
    {
        path: 'vacancy-responses',
        loadChildren: () => import('./modules/vacancy-response/vacancy-response.module').then(m => m.VacancyResponseModule),
        canActivate: [AuthGuard]
    },

    // Audit routes (protected)
    {
        path: 'audit',
        loadChildren: () => import('./modules/audit/audit.module').then(m => m.AuditModule),
        canActivate: [AuthGuard]
    },

    // AI Prompt routes (protected)
    {
        path: 'ai-prompts',
        loadChildren: () => import('./modules/ai-prompt/ai-prompt.module').then(m => m.AiPromptModule),
        canActivate: [AuthGuard]
    },

    // AI Prompt Category routes (protected)
    {
        path: 'ai-prompt-categories',
        loadChildren: () => import('./modules/ai-prompt-category/ai-prompt-category.module').then(m => m.AiPromptCategoryModule),
        canActivate: [AuthGuard]
    },

    // AI Prompt Usage Log routes (protected)
    {
        path: 'ai-prompt-usage-logs',
        loadChildren: () => import('./modules/ai-prompt-usage-log/ai-prompt-usage-log.module').then(m => m.AiPromptUsageLogModule),
        canActivate: [AuthGuard]
    },

    {path: 'home', component: HomeComponent},
    {path: '', redirectTo: '/home', pathMatch: 'full'},
    {path: '**', redirectTo: '/home'}
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
