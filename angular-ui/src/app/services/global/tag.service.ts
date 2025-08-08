import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TagDTO} from '../../models/global/tag.model';
import {BaseService} from '../base.service';

/**
 * Service for managing tags.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class TagService extends BaseService<TagDTO> {
    protected baseUrl = '/api/tags';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get tag by name
     * @param name - Tag name
     * @returns Observable tag
     */
    getByName(name: string): Observable<TagDTO> {
        return this.http.get<TagDTO>(`${this.baseUrl}/by-name/${name}`);
    }

    /**
     * Search tags by query
     * @param query - Search query
     * @returns Observable array of tags
     */
    override search(searchParams: { [key: string]: string | undefined }): Observable<TagDTO[]>;
    override search(query: string): Observable<TagDTO[]>;
    override search(queryOrParams: string | { [key: string]: string | undefined }): Observable<TagDTO[]> {
        if (typeof queryOrParams === 'string') {
            return super.search({query: queryOrParams});
        }
        return super.search(queryOrParams);
    }

    /**
     * Get popular tags with optional limit
     * @param limit - Optional limit for number of tags
     * @returns Observable array of popular tags
     */
    getPopular(limit?: number): Observable<TagDTO[]> {
        return this.getWithParams('popular', {limit});
    }

    /**
     * Get tags with their question count
     * @returns Observable array of tags with question count
     */
    getTagsWithQuestionCount(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/with-question-count`);
    }
}
