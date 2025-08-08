import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AnswerDTO} from '../../models/personalized/answer.model';
import {PersonalizedService} from '../personalized.service';

/**
 * Service for managing answers.
 * Extends PersonalizedService to inherit common CRUD operations for user-owned entities and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class AnswerService extends PersonalizedService<AnswerDTO> {
    protected baseUrl = '/api/answers';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get current user's answers
     * @returns Observable array of user's answers
     */
    getMyAnswers(): Observable<AnswerDTO[]> {
        return this.http.get<AnswerDTO[]>(`${this.baseUrl}/my-answers`);
    }

    /**
     * Get answers by question ID
     * @param questionId - Question ID to filter by
     * @returns Observable array of answers
     */
    getByQuestion(questionId: string): Observable<AnswerDTO[]> {
        return this.http.get<AnswerDTO[]>(`${this.baseUrl}/by-question/${questionId}`);
    }

    /**
     * Get answers by user ID
     * @param userId - User ID to filter by
     * @returns Observable array of answers
     */
    override getByUser(userId: string): Observable<AnswerDTO[]> {
        return this.http.get<AnswerDTO[]>(`${this.baseUrl}/by-user/${userId}`);
    }

    /**
     * Search answers with multiple criteria
     * @returns Observable array of answers
     * @param searchParams
     */
    override search(searchParams: { [key: string]: string | undefined }): Observable<AnswerDTO[]>;
    override search(query?: string, userId?: string, questionId?: string): Observable<AnswerDTO[]>;
    override search(queryOrParams?: string | {
        [key: string]: string | undefined
    }, userId?: string, questionId?: string): Observable<AnswerDTO[]> {
        if (typeof queryOrParams === 'object') {
            return super.search(queryOrParams);
        }
        return super.search({query: queryOrParams, userId, questionId});
    }

    /**
     * Evaluate answer using AI
     * @param id - Answer ID to evaluate
     * @returns Observable evaluated answer
     */
    evaluateWithAI(id: string): Observable<AnswerDTO> {
        return this.http.post<AnswerDTO>(`${this.baseUrl}/${id}/evaluate`, {});
    }
}
