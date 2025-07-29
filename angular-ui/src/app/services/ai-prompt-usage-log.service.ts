import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AiPromptUsageLogModel} from '../models/global/ai/ai-prompt-usage-log.model';
import {Page, PageRequest} from '../models/basic/page.model';

@Injectable({
    providedIn: 'root'
})
export class AiPromptUsageLogService {
    private baseUrl = '/api/ai-prompt-usage-logs';

    constructor(private http: HttpClient) {
    }

    getAll(): Observable<AiPromptUsageLogModel[]> {
        return this.http.get<AiPromptUsageLogModel[]>(this.baseUrl);
    }

    getAllPaged(pageRequest: PageRequest): Observable<Page<AiPromptUsageLogModel>> {
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

        return this.http.get<Page<AiPromptUsageLogModel>>(`${this.baseUrl}/page`, {params});
    }

    getById(id: string): Observable<AiPromptUsageLogModel> {
        return this.http.get<AiPromptUsageLogModel>(`${this.baseUrl}/${id}`);
    }

    getByUserId(userId: string): Observable<AiPromptUsageLogModel[]> {
        return this.http.get<AiPromptUsageLogModel[]>(`${this.baseUrl}/by-user/${userId}`);
    }

    getByPromptId(promptId: string): Observable<AiPromptUsageLogModel[]> {
        return this.http.get<AiPromptUsageLogModel[]>(`${this.baseUrl}/by-prompt/${promptId}`);
    }

    getMyLogs(): Observable<AiPromptUsageLogModel[]> {
        return this.http.get<AiPromptUsageLogModel[]>(`${this.baseUrl}/my`);
    }

    create(log: AiPromptUsageLogModel): Observable<AiPromptUsageLogModel> {
        return this.http.post<AiPromptUsageLogModel>(this.baseUrl, log);
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
}