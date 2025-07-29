import {BasisDtoEntityModel} from '../basis-dto-entity.model';

export enum OperationType {
    CREATE = 'CREATE',
    READ = 'READ',
    UPDATE = 'UPDATE',
    DELETE = 'DELETE'
}

export interface AuditModel extends BasisDtoEntityModel {
    operationType: OperationType;
    entityType: string;
    entityId?: string;
    userId?: string;
    userName?: string;
    timestamp: Date;
    details?: string;
}