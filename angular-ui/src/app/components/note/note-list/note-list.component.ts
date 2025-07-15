import {Component, OnInit} from '@angular/core';
import {NoteService} from '../../../services/note.service';
import {NoteDTO} from '../../../models/note.model';

@Component({
    selector: 'app-note-list',
    templateUrl: './note-list.component.html',
    styleUrls: ['./note-list.component.css']
})
export class NoteListComponent implements OnInit {
    notes: NoteDTO[] = [];
    isLoading = true;
    error = '';

    constructor(private noteService: NoteService) {
    }

    ngOnInit(): void {
        this.loadMyNotes();
    }

    loadMyNotes(): void {
        this.isLoading = true;
        this.noteService.getMyNotes().subscribe({
            next: (data) => {
                this.notes = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load notes. ' + err.message;
                this.isLoading = false;
            }
        });
    }
}
