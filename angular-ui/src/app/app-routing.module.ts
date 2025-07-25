import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

// Guards
import {AuthGuard} from './guards/auth.guard';

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

    // Tag routes (protected)
    {
        path: 'tags',
        loadChildren: () => import('./modules/tag/tag.module').then(m => m.TagModule),
        canActivate: [AuthGuard]
    },

    // Interview Question routes (protected)
    {
        path: 'interview-questions',
        loadChildren: () => import('./modules/interview-question/interview-question.module').then(m => m.InterviewQuestionModule),
        canActivate: [AuthGuard]
    },

    // Vacancy Response routes (protected)
    {
        path: 'vacancies',
        loadChildren: () => import('./modules/vacancy-response/vacancy-response.module').then(m => m.VacancyResponseModule),
        canActivate: [AuthGuard]
    },

    // Default routes
    {path: '', redirectTo: '/users', pathMatch: 'full'},
    {path: '**', redirectTo: '/users'}
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
