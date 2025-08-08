import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AuditModel, OperationType} from '../../models/global/audit.model';
import {BaseService} from '../base.service';

/**
 * Service for managing audit logs.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class AuditService extends BaseService<AuditModel> {
    protected baseUrl = '/api/audits';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get audit logs by user ID
     * @param userId - User ID to filter by
     * @returns Observable array of audit logs
     */
    getByUserId(userId: string): Observable<AuditModel[]> {
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-user/${userId}`);
    }

    /**
     * Get audit logs by entity type
     * @param entityType - Entity type to filter by
     * @returns Observable array of audit logs
     */
    getByEntityType(entityType: string): Observable<AuditModel[]> {
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-entity-type/${entityType}`);
    }

    /**
     * Get audit logs by operation type
     * @param operationType - Operation type to filter by
     * @returns Observable array of audit logs
     */
    getByOperationType(operationType: OperationType): Observable<AuditModel[]> {
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-operation-type/${operationType}`);
    }

    /**
     * Get audit logs by entity ID
     * @param entityId - Entity ID to filter by
     * @returns Observable array of audit logs
     */
    getByEntityId(entityId: string): Observable<AuditModel[]> {
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-entity-id/${entityId}`);
    }

    /**
     * Get audit logs by date range
     * @param startDate - Start date for filtering
     * @param endDate - End date for filtering
     * @returns Observable array of audit logs
     */
    getByDateRange(startDate: string, endDate: string): Observable<AuditModel[]> {
        const params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate);
        return this.http.get<AuditModel[]>(`${this.baseUrl}/by-date-range`, {params});
    }
}
