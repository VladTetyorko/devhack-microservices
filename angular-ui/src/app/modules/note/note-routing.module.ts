import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {NoteListComponent} from '../../components/note/note-list/note-list.component';
import {NoteByQuestionComponent} from '../../components/note/note-by-question/note-by-question.component';

const routes: Routes = [
    {path: 'my-notes', component: NoteListComponent},
    {path: 'question/:questionId', component: NoteByQuestionComponent},
    {path: '', redirectTo: 'my-notes', pathMatch: 'full'}
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class NoteRoutingModule {
}
