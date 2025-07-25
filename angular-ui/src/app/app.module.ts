import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';

// Feature modules
import {AuthModule} from './modules/auth/auth.module';
import {UserModule} from './modules/user/user.module';
import {NoteModule} from './modules/note/note.module';
import {VacancyResponseModule} from './modules/vacancy-response/vacancy-response.module';
import {SharedModule} from './modules/shared/shared.module';

// Interceptors
import {AuthInterceptor} from './interceptors/auth.interceptor';

@NgModule({
    declarations: [
        AppComponent
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        AppRoutingModule,

        // Feature modules
        AuthModule,
        UserModule,
        NoteModule,
        VacancyResponseModule,
        SharedModule
    ],
    providers: [
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptor,
            multi: true
        }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
