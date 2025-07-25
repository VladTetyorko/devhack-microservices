import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InterviewQuestionDTO } from '../models/global/interview-question.model';
import { Page, PageRequest } from '../models/basic/page.model';

@Injectable({
  providedIn: 'root'
})
export class InterviewQuestionService {
  private baseUrl = '/api/questions';

  constructor(private http: HttpClient) {}

  getAll(): Observable<InterviewQuestionDTO[]> {
    return this.http.get<InterviewQuestionDTO[]>(this.baseUrl);
  }

  getAllPaged(pageRequest: PageRequest): Observable<Page<InterviewQuestionDTO>> {
    let params = new HttpParams();

    if (pageRequest.page !== undefined) {
      params = params.set('page', pageRequest.page.toString());
    }
    if (pageRequest.size !== undefined) {
      params = params.set('size', pageRequest.size.toString());
    }
    if (pageRequest.sort && pageRequest.sort.length > 0) {
      pageRequest.sort.forEach((sortParam: string) => {
        params = params.append('sort', sortParam);
      });
    }

    return this.http.get<Page<InterviewQuestionDTO>>(`${this.baseUrl}/page`, { params });
  }

  getById(id: string): Observable<InterviewQuestionDTO> {
    return this.http.get<InterviewQuestionDTO>(`${this.baseUrl}/${id}`);
  }

  getMyQuestions(): Observable<InterviewQuestionDTO[]> {
    return this.http.get<InterviewQuestionDTO[]>(`${this.baseUrl}/my-questions`);
  }

  getByTag(tagName: string): Observable<InterviewQuestionDTO[]> {
    return this.http.get<InterviewQuestionDTO[]>(`${this.baseUrl}/by-tag/${tagName}`);
  }

  getByDifficulty(difficulty: string): Observable<InterviewQuestionDTO[]> {
    return this.http.get<InterviewQuestionDTO[]>(`${this.baseUrl}/by-difficulty/${difficulty}`);
  }

  search(query?: string, tagName?: string, difficulty?: string): Observable<InterviewQuestionDTO[]> {
    let params = new HttpParams();
    if (query) params = params.set('query', query);
    if (tagName) params = params.set('tagName', tagName);
    if (difficulty) params = params.set('difficulty', difficulty);

    return this.http.get<InterviewQuestionDTO[]>(`${this.baseUrl}/search`, { params });
  }

  create(question: InterviewQuestionDTO): Observable<InterviewQuestionDTO> {
    return this.http.post<InterviewQuestionDTO>(this.baseUrl, question);
  }

  update(id: string, question: InterviewQuestionDTO): Observable<InterviewQuestionDTO> {
    return this.http.put<InterviewQuestionDTO>(`${this.baseUrl}/${id}`, question);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  generateQuestions(tagName: string, count: number, difficulty: string): Observable<any> {
    const body = { tagName, count, difficulty };
    return this.http.post<any>(`${this.baseUrl}/generate`, body);
  }

  getRandomQuestion(tagName?: string, difficulty?: string): Observable<InterviewQuestionDTO> {
    let params = new HttpParams();
    if (tagName) params = params.set('tagName', tagName);
    if (difficulty) params = params.set('difficulty', difficulty);

    return this.http.get<InterviewQuestionDTO>(`${this.baseUrl}/random`, { params });
  }
}
