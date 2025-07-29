import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AuditModel, OperationType} from '../models/global/audit.model';
import {Page, PageRequest} from '../models/basic/page.model';

@Injectable({
    providedIn: 'root'
})
export class AuditService {
    private baseUrl = '/api/audits';

    constructor(private http: HttpClient) {
    }

    getAll(): Observable<AuditModel[]> {
        return this.http.get<AuditModel[]>(this.baseUrl);
    }

    getAllPaged(pageRequest: PageRequest): Observable<Page<AuditModel>> {
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

        return this.http.get<Page<AuditModel>>(`${this.baseUrl}/page`, {params});
    }

    getById(id: string): Observable<AuditModel> {
        return this.http.get<AuditModel>(`${this.baseUrl}/${id}`);
    }

    getByUserId(userId: string): Observable<AuditModel[]> {
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-user/${userId}`);
    }

    getByEntityType(entityType: string): Observable<AuditModel[]> {
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-entity-type/${entityType}`);
    }

    getByOperationType(operationType: OperationType): Observable<AuditModel[]> {
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-operation-type/${operationType}`);
    }

    getByEntityId(entityId: string): Observable<AuditModel[]> {
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-entity-id/${entityId}`);
    }

    getByDateRange(startDate: string, endDate: string): Observable<AuditModel[]> {
        const params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate);
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-date-range`, {params});
    }
}