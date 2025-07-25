import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {NoteDTO} from '../models/personalized/note.model';

@Injectable({
    providedIn: 'root'
})
export class NoteService {
    private baseUrl = '/api/notes';

    constructor(private http: HttpClient) {
    }

    getAll(): Observable<NoteDTO[]> {
        return this.http.get<NoteDTO[]>(this.baseUrl);
    }

    getById(id: string): Observable<NoteDTO> {
        return this.http.get<NoteDTO>(`${this.baseUrl}/${id}`);
    }

    getMyNotes(): Observable<NoteDTO[]> {
        return this.http.get<NoteDTO[]>(`${this.baseUrl}/my-notes`);
    }

    getByQuestion(questionId: string): Observable<NoteDTO[]> {
        return this.http.get<NoteDTO[]>(`${this.baseUrl}/by-question/${questionId}`);
    }

    getByUser(userId: string): Observable<NoteDTO[]> {
        return this.http.get<NoteDTO[]>(`${this.baseUrl}/by-user/${userId}`);
    }

    create(note: NoteDTO): Observable<NoteDTO> {
        return this.http.post<NoteDTO>(this.baseUrl, note);
    }

    update(id: string, note: NoteDTO): Observable<NoteDTO> {
        return this.http.put<NoteDTO>(`${this.baseUrl}/${id}`, note);
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
}
