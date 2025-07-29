import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AiPromptModel} from '../models/global/ai/ai-prompt.model';
import {Page, PageRequest} from '../models/basic/page.model';

@Injectable({
    providedIn: 'root'
})
export class AiPromptService {
    private baseUrl = '/api/ai-prompts';

    constructor(private http: HttpClient) {
    }

    getAll(): Observable<AiPromptModel[]> {
        return this.http.get<AiPromptModel[]>(this.baseUrl);
    }

    getAllPaged(pageRequest: PageRequest): Observable<Page<AiPromptModel>> {
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

        return this.http.get<Page<AiPromptModel>>(`${this.baseUrl}/page`, {params});
    }

    getById(id: string): Observable<AiPromptModel> {
        return this.http.get<AiPromptModel>(`${this.baseUrl}/${id}`);
    }

    getByCode(code: string): Observable<AiPromptModel> {
        return this.http.get<AiPromptModel>(`${this.baseUrl}/by-code/${code}`);
    }

    getByCategoryId(categoryId: string): Observable<AiPromptModel[]> {
        return this.http.get<AiPromptModel[]>(`${this.baseUrl}/by-category/${categoryId}`);
    }

    getActive(): Observable<AiPromptModel[]> {
        return this.http.get<AiPromptModel[]>(`${this.baseUrl}/active`);
    }

    create(prompt: AiPromptModel): Observable<AiPromptModel> {
        return this.http.post<AiPromptModel>(this.baseUrl, prompt);
    }

    update(id: string, prompt: AiPromptModel): Observable<AiPromptModel> {
        return this.http.put<AiPromptModel>(`${this.baseUrl}/${id}`, prompt);
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }

    activate(id: string): Observable<AiPromptModel> {
        return this.http.patch<AiPromptModel>(`${this.baseUrl}/${id}/activate`, {});
    }

    deactivate(id: string): Observable<AiPromptModel> {
        return this.http.patch<AiPromptModel>(`${this.baseUrl}/${id}/deactivate`, {});
    }
}