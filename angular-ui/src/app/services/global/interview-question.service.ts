import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {InterviewQuestionDTO} from '../../models/global/interview-question.model';
import {BaseService} from '../base.service';

/**
 * Service for managing interview questions.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class InterviewQuestionService extends BaseService<InterviewQuestionDTO> {
    protected baseUrl = '/api/questions';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get current user's questions
     * @returns Observable array of user's questions
     */
    getMyQuestions(): Observable<InterviewQuestionDTO[]> {
        return this.http.get<InterviewQuestionDTO[]>(`${this.baseUrl}/my-questions`);
    }

    /**
     * Get questions by tag name
     * @param tagName - Tag name to filter by
     * @returns Observable array of questions
     */
    getByTag(tagName: string): Observable<InterviewQuestionDTO[]> {
        return this.http.get<InterviewQuestionDTO[]>(`${this.baseUrl}/by-tag/${tagName}`);
    }

    /**
     * Get questions by difficulty level
     * @param difficulty - Difficulty level to filter by
     * @returns Observable array of questions
     */
    getByDifficulty(difficulty: string): Observable<InterviewQuestionDTO[]> {
        return this.http.get<InterviewQuestionDTO[]>(`${this.baseUrl}/by-difficulty/${difficulty}`);
    }

    /**
     * Search questions with multiple criteria
     * @param query - Search query
     * @param tagName - Tag name filter
     * @param difficulty - Difficulty filter
     * @returns Observable array of questions
     */
    override search(searchParams: { [key: string]: string | undefined }): Observable<InterviewQuestionDTO[]>;
    override search(query?: string, tagName?: string, difficulty?: string): Observable<InterviewQuestionDTO[]>;
    override search(queryOrParams?: string | {
        [key: string]: string | undefined
    }, tagName?: string, difficulty?: string): Observable<InterviewQuestionDTO[]> {
        if (typeof queryOrParams === 'object') {
            return super.search(queryOrParams);
        }
        return super.search({query: queryOrParams, tagName, difficulty});
    }

    /**
     * Generate new questions using AI
     * @param tagName - Tag name for questions
     * @param count - Number of questions to generate
     * @param difficulty - Difficulty level
     * @returns Observable generation result
     */
    generateQuestions(tagName: string, count: number, difficulty: string): Observable<any> {
        const body = {tagName, count, difficulty};
        return this.postToEndpoint('generate', body);
    }

    /**
     * Get a random question with optional filters
     * @param tagName - Optional tag name filter
     * @param difficulty - Optional difficulty filter
     * @returns Observable random question
     */
    getRandomQuestion(tagName?: string, difficulty?: string): Observable<InterviewQuestionDTO> {
        return this.getSingleWithParams('random', {tagName, difficulty});
    }
}
