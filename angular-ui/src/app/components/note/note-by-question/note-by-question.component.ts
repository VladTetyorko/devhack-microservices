import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {NoteService} from '../../../services/personalized/note.service';
import {NoteDTO} from '../../../models/personalized/note.model';

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
            next: (data: NoteDTO[]) => {
                this.notes = data.map((note: NoteDTO) => ({
                    ...note,
                    createdAt: this.convertDateFormat(note.createdAt),
                    updatedAt: this.convertDateFormat(note.updatedAt)
                }));
                this.isLoading = false;
            },
            error: (err: any) => {
                this.error = 'Failed to load question notes. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    private convertDateFormat(dateString?: string): string | undefined {
        if (!dateString) return dateString;

        // Check if the date is in comma-separated format (e.g., "2025,7,7,15,36,51,426621000")
        if (dateString.includes(',')) {
            try {
                const parts = dateString.split(',').map(part => parseInt(part, 10));
                if (parts.length >= 6) {
                    // parts: [year, month, day, hour, minute, second, nanoseconds]
                    // Note: month is 1-based in the input, but Date constructor expects 0-based
                    const date = new Date(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
                    return date.toISOString();
                }
            } catch (error) {
                console.warn('Failed to parse date:', dateString, error);
            }
        }

        return dateString;
    }
}
