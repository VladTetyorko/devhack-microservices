import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

// User components
import {UserListComponent} from './components/user/user-list/user-list.component';
import {UserDetailComponent} from './components/user/user-detail/user-detail.component';
import {UserRegisterComponent} from './components/user/user-register/user-register.component';

// Note components
import {NoteListComponent} from './components/note/note-list/note-list.component';
import {NoteByQuestionComponent} from './components/note/note-by-question/note-by-question.component';

// VacancyResponse components
import {
  VacancyResponseListComponent
} from './components/vacancy-response/vacancy-response-list/vacancy-response-list.component';
import {
  VacancyResponseDetailComponent
} from './components/vacancy-response/vacancy-response-detail/vacancy-response-detail.component';

const routes: Routes = [
    // User routes
    {path: 'users', component: UserListComponent},
    {path: 'users/register', component: UserRegisterComponent},
    {path: 'users/:id', component: UserDetailComponent},

    // Note routes
    {path: 'notes/my-notes', component: NoteListComponent},
    {path: 'notes/question/:questionId', component: NoteByQuestionComponent},

    // Vacancy Response routes
    {path: 'vacancies/my-responses', component: VacancyResponseListComponent},
    {path: 'vacancies/my-responses/:id', component: VacancyResponseDetailComponent},

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
