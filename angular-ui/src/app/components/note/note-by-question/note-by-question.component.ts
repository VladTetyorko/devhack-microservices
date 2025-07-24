import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {NoteService} from '../../../services/note.service';
import {NoteDTO} from '../../../models/note.model';

@Component({
    selector: 'app-note-by-question',
    templateUrl: './note-by-question.component.html',
    styleUrls: ['./note-by-question.component.css']
})
export class NoteByQuestionComponent implements OnInit {
    notes: NoteDTO[] = [];
    questionId!: string;
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private noteService: NoteService
    ) {
    }

    ngOnInit(): void {
        this.questionId = this.route.snapshot.paramMap.get('questionId')!;
        this.loadQuestionNotes();
    }

    loadQuestionNotes(): void {
        this.isLoading = true;
        this.noteService.getByQuestion(this.questionId).subscribe({
            next: (data) => {
                this.notes = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load question notes. ' + err.message;
                this.isLoading = false;
            }
        });
    }
}
