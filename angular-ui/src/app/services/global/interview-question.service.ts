import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {InterviewQuestionDTO} from '../../models/global/interview-question.model';
import {BaseService} from '../base.service';
import {Page, PageRequest} from '../../models/basic/page.model';

/**
 * Interface for question search parameters
 */
export interface QuestionSearchParams {
    query?: string;
    difficulty?: string;
    tagId?: string;
}

/**
 * Interface for question statistics
 */
export interface QuestionStats {
    totalQuestions: number;
    userQuestions: number;
    answeredQuestions: number;
}

/**
 * Interface for AI question generation request
 */
export interface GenerateQuestionsRequest {
    topic: string;
    count: number;
    difficulty: string;
    type?: string;
    experience?: string;
}

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
     * Search questions with comprehensive filtering and pagination
     * @param searchParams - Search parameters object
     * @param pageRequest - Pagination parameters
     * @returns Observable page of questions
     */
    searchWithPagination(searchParams: QuestionSearchParams, pageRequest: PageRequest): Observable<Page<InterviewQuestionDTO>> {
        let params = new HttpParams();

        // Add pagination parameters
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

        // Add search parameters
        if (searchParams.query) {
            params = params.set('query', searchParams.query);
        }
        if (searchParams.difficulty) {
            params = params.set('difficulty', searchParams.difficulty);
        }
        if (searchParams.tagId) {
            params = params.set('tagId', searchParams.tagId);
        }

        return this.http.get<Page<InterviewQuestionDTO>>(`${this.baseUrl}/search`, {params});
    }

    /**
     * Get questions by tag slug with pagination
     * @param tagSlug - Tag slug to filter by
     * @param pageRequest - Pagination parameters
     * @returns Observable page of questions
     */
    getByTagSlug(tagSlug: string, pageRequest: PageRequest): Observable<Page<InterviewQuestionDTO>> {
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

        return this.http.get<Page<InterviewQuestionDTO>>(`${this.baseUrl}/by-tag/${tagSlug}`, {params});
    }

    /**
     * Get question statistics
     * @returns Observable with question statistics
     */
    getQuestionStats(): Observable<QuestionStats> {
        return this.http.get<QuestionStats>(`${this.baseUrl}/stats`);
    }

    /**
     * Get current user's questions
     * @returns Observable array of user's questions
     */
    getMyQuestions(): Observable<InterviewQuestionDTO[]> {
        return this.http.get<InterviewQuestionDTO[]>(`${this.baseUrl}/my-questions`);
    }

    /**
     * Generate new questions using AI with comprehensive parameters
     * @param request - Generation request with all parameters
     * @returns Observable generation result
     */
    generateQuestions(request: GenerateQuestionsRequest): Observable<any> {
        return this.http.post<any>(`${this.baseUrl}/generate`, request);
    }

    /**
     * Auto-generate easy questions for a specific topic
     * @param tagName - Tag name to generate questions for
     * @returns Observable generation result
     */
    autoGenerateEasyQuestions(tagName: string): Observable<any> {
        const params = new HttpParams().set('tagName', tagName);
        return this.http.post<any>(`${this.baseUrl}/generate/auto`, null, {params});
    }

    /**
     * Auto-generate easy questions for multiple tags
     * @param tagIds - Array of tag IDs to generate questions for
     * @returns Observable generation result
     */
    autoGenerateEasyQuestionsForMultipleTags(tagIds: string[]): Observable<any> {
        const params = new HttpParams().set('tagIds', tagIds.join(','));
        return this.http.post<any>(`${this.baseUrl}/generate/multi`, null, {params});
    }

    /**
     * Search questions with multiple criteria (legacy method for backward compatibility)
     * @param searchParams - Search parameters object
     * @returns Observable array of questions
     */
    override search(searchParams: { [key: string]: string | undefined }): Observable<InterviewQuestionDTO[]> {
        return super.search(searchParams);
    }
}
