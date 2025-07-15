import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';

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

// Shared components
import {NavbarComponent} from './components/shared/navbar/navbar.component';
import {FooterComponent} from './components/shared/footer/footer.component';
import {HeaderComponent} from "./components/header/header.component";

@NgModule({
    declarations: [
        AppComponent,
        // User components
        UserListComponent,
        UserDetailComponent,
        UserRegisterComponent,
        // Note components
        NoteListComponent,
        NoteByQuestionComponent,
        // VacancyResponse components
        VacancyResponseListComponent,
        VacancyResponseDetailComponent,
        // Shared components
        NavbarComponent,
        FooterComponent,
        HeaderComponent
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        FormsModule,
        ReactiveFormsModule,
        AppRoutingModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
