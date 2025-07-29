import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AiPromptCategoryModel} from '../models/global/ai/ai-prompt-category.model';
import {Page, PageRequest} from '../models/basic/page.model';

@Injectable({
    providedIn: 'root'
})
export class AiPromptCategoryService {
    private baseUrl = '/api/ai-prompt-categories';

    constructor(private http: HttpClient) {
    }

    getAll(): Observable<AiPromptCategoryModel[]> {
        return this.http.get<AiPromptCategoryModel[]>(this.baseUrl);
    }

    getAllPaged(pageRequest: PageRequest): Observable<Page<AiPromptCategoryModel>> {
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

        return this.http.get<Page<AiPromptCategoryModel>>(`${this.baseUrl}/page`, {params});
    }

    getById(id: string): Observable<AiPromptCategoryModel> {
        return this.http.get<AiPromptCategoryModel>(`${this.baseUrl}/${id}`);
    }

    getByCode(code: string): Observable<AiPromptCategoryModel> {
        return this.http.get<AiPromptCategoryModel>(`${this.baseUrl}/by-code/${code}`);
    }

    getWithPrompts(): Observable<AiPromptCategoryModel[]> {
        return this.http.get<AiPromptCategoryModel[]>(`${this.baseUrl}/with-prompts`);
    }

    create(category: AiPromptCategoryModel): Observable<AiPromptCategoryModel> {
        return this.http.post<AiPromptCategoryModel>(this.baseUrl, category);
    }

    update(id: string, category: AiPromptCategoryModel): Observable<AiPromptCategoryModel> {
        return this.http.put<AiPromptCategoryModel>(`${this.baseUrl}/${id}`, category);
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
}