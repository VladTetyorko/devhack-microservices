import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {NoteService} from '../../../services/note.service';
import {NoteDTO} from '../../../models/personalized/note.model';

@Component({
    selector: 'app-note-list',
    templateUrl: './note-list.component.html',
    styleUrls: ['./note-list.component.css']
})
export class NoteListComponent implements OnInit {
    notes: NoteDTO[] = [];
    filteredNotes: NoteDTO[] = [];
    isLoading = true;
    error = '';
    successMessage = '';

    // Search and filter properties
    searchTerm = '';
    viewMode = 'cards'; // 'cards' or 'list'

    // Create/Edit note properties
    showCreateForm = false;
    editingNote: NoteDTO | null = null;
    noteForm = {
        content: '',
        linkedQuestionId: ''
    };

    constructor(
        private noteService: NoteService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadMyNotes();
    }

    loadMyNotes(): void {
        this.isLoading = true;
        this.error = '';
        this.noteService.getMyNotes().subscribe({
            next: (data) => {
                this.notes = data;
                this.filteredNotes = [...data];
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load notes. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    onSearch(): void {
        this.applyFilters();
    }

    applyFilters(): void {
        let filtered = [...this.notes];

        // Apply search filter
        if (this.searchTerm.trim()) {
            const searchLower = this.searchTerm.toLowerCase().trim();
            filtered = filtered.filter(note =>
                note.noteText?.toLowerCase().includes(searchLower)
            );
        }

        this.filteredNotes = filtered;
    }

    clearSearch(): void {
        this.searchTerm = '';
        this.applyFilters();
    }

    showCreateNoteForm(): void {
        this.showCreateForm = true;
        this.editingNote = null;
        this.resetForm();
    }

    hideCreateForm(): void {
        this.showCreateForm = false;
        this.editingNote = null;
        this.resetForm();
    }

    editNote(note: NoteDTO): void {
        this.editingNote = note;
        this.showCreateForm = true;
        this.noteForm = {
            content: note.noteText || '',
            linkedQuestionId: note.questionId || ''
        };
    }

    saveNote(): void {
        if (!this.noteForm.content.trim()) {
            this.error = 'Note content is required';
            return;
        }

        const noteData: NoteDTO = {
            noteText: this.noteForm.content.trim(),
            questionId: this.noteForm.linkedQuestionId,
            questionText: this.editingNote?.questionText || '',
        };

        if (this.editingNote) {
            // Update existing note
            this.noteService.update(this.editingNote.id!, noteData).subscribe({
                next: () => {
                    this.successMessage = 'Note updated successfully!';
                    this.hideCreateForm();
                    this.loadMyNotes();
                    setTimeout(() => this.successMessage = '', 3000);
                },
                error: (err) => {
                    this.error = 'Failed to update note. ' + err.message;
                }
            });
        } else {
            // Create new note
            this.noteService.create(noteData).subscribe({
                next: () => {
                    this.successMessage = 'Note created successfully!';
                    this.hideCreateForm();
                    this.loadMyNotes();
                    setTimeout(() => this.successMessage = '', 3000);
                },
                error: (err) => {
                    this.error = 'Failed to create note. ' + err.message;
                }
            });
        }
    }

    deleteNote(note: NoteDTO, event: Event): void {
        event.stopPropagation();

        if (confirm('Are you sure you want to delete this note? This action cannot be undone.')) {
            this.noteService.delete(note.id!).subscribe({
                next: () => {
                    this.successMessage = 'Note deleted successfully!';
                    this.loadMyNotes();
                    setTimeout(() => this.successMessage = '', 3000);
                },
                error: (err) => {
                    this.error = 'Failed to delete note. ' + err.message;
                }
            });
        }
    }

    viewQuestionNotes(questionId: string): void {
        this.router.navigate(['/notes/question', questionId]);
    }

    resetForm(): void {
        this.noteForm = {
            content: '',
            linkedQuestionId: ''
        };
    }

    // Utility methods
    formatDate(dateString?: string): string {
        if (!dateString) return '';
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    truncateContent(content: string, maxLength: number = 150): string {
        if (!content) return '';
        return content.length > maxLength ? content.substring(0, maxLength) + '...' : content;
    }

    trackByNoteId(index: number, note: NoteDTO): string {
        return note.id || index.toString();
    }
}
