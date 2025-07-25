import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AnswerDTO } from '../models/answer.model';

@Injectable({
  providedIn: 'root'
})
export class AnswerService {
  private baseUrl = '/api/answers';

  constructor(private http: HttpClient) {}

  getAll(): Observable<AnswerDTO[]> {
    return this.http.get<AnswerDTO[]>(this.baseUrl);
  }

  getById(id: string): Observable<AnswerDTO> {
    return this.http.get<AnswerDTO>(`${this.baseUrl}/${id}`);
  }

  getMyAnswers(): Observable<AnswerDTO[]> {
    return this.http.get<AnswerDTO[]>(`${this.baseUrl}/my-answers`);
  }

  getByQuestion(questionId: string): Observable<AnswerDTO[]> {
    return this.http.get<AnswerDTO[]>(`${this.baseUrl}/by-question/${questionId}`);
  }

  getByUser(userId: string): Observable<AnswerDTO[]> {
    return this.http.get<AnswerDTO[]>(`${this.baseUrl}/by-user/${userId}`);
  }

  search(query?: string, userId?: string, questionId?: string): Observable<AnswerDTO[]> {
    let params = new HttpParams();
    if (query) params = params.set('query', query);
    if (userId) params = params.set('userId', userId);
    if (questionId) params = params.set('questionId', questionId);
    
    return this.http.get<AnswerDTO[]>(`${this.baseUrl}/search`, { params });
  }

  create(answer: AnswerDTO): Observable<AnswerDTO> {
    return this.http.post<AnswerDTO>(this.baseUrl, answer);
  }

  update(id: string, answer: AnswerDTO): Observable<AnswerDTO> {
    return this.http.put<AnswerDTO>(`${this.baseUrl}/${id}`, answer);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  evaluateWithAI(id: string): Observable<AnswerDTO> {
    return this.http.post<AnswerDTO>(`${this.baseUrl}/${id}/evaluate`, {});
  }
}