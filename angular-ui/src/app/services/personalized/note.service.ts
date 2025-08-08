import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {NoteDTO} from '../../models/personalized/note.model';
import {PersonalizedService} from '../personalized.service';

/**
 * Service for managing notes.
 * Extends PersonalizedService to inherit common CRUD operations for user-owned entities and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class NoteService extends PersonalizedService<NoteDTO> {
    protected baseUrl = '/api/notes';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get current user's notes
     * @returns Observable array of user's notes
     */
    getMyNotes(): Observable<NoteDTO[]> {
        return this.http.get<NoteDTO[]>(`${this.baseUrl}/my-notes`);
    }

    /**
     * Get notes by question ID
     * @param questionId - Question ID to filter by
     * @returns Observable array of notes
     */
    getByQuestion(questionId: string): Observable<NoteDTO[]> {
        return this.http.get<NoteDTO[]>(`${this.baseUrl}/by-question/${questionId}`);
    }

    /**
     * Get notes by user ID
     * @param userId - User ID to filter by
     * @returns Observable array of notes
     */
    override getByUser(userId: string): Observable<NoteDTO[]> {
        return this.http.get<NoteDTO[]>(`${this.baseUrl}/by-user/${userId}`);
    }
}
