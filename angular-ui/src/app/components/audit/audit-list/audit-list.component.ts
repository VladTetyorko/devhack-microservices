import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AuditService} from '../../../services/global/audit.service';
import {AuditModel, OperationType} from '../../../models/global/audit.model';

@Component({
    selector: 'app-audit-list',
    templateUrl: './audit-list.component.html',
    styleUrls: ['./audit-list.component.css']
})
export class AuditListComponent implements OnInit {
    audits: AuditModel[] = [];
    isLoading = true;
    error = '';
    selectedOperationType: OperationType | null = null;
    selectedEntityType: string = '';
    operationTypes = Object.values(OperationType);

    constructor(
        private auditService: AuditService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadAudits();
    }

    loadAudits(): void {
        this.isLoading = true;
        this.auditService.getAll().subscribe({
            next: (data) => {
                this.audits = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load audit records. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    viewDetail(id: string): void {
        this.router.navigate(['/audits', id]);
    }

    filterByOperationType(): void {
        if (!this.selectedOperationType) {
            this.loadAudits();
            return;
        }

        this.isLoading = true;
        this.auditService.getByOperationType(this.selectedOperationType).subscribe({
            next: (data) => {
                this.audits = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to filter audit records. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    filterByEntityType(): void {
        if (!this.selectedEntityType.trim()) {
            this.loadAudits();
            return;
        }

        this.isLoading = true;
        this.auditService.getByEntityType(this.selectedEntityType).subscribe({
            next: (data) => {
                this.audits = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to filter audit records. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    clearFilters(): void {
        this.selectedOperationType = null;
        this.selectedEntityType = '';
        this.loadAudits();
    }

    getOperationTypeClass(operationType: OperationType): string {
        switch (operationType) {
            case OperationType.CREATE:
                return 'badge bg-success';
            case OperationType.UPDATE:
                return 'badge bg-warning text-dark';
            case OperationType.DELETE:
                return 'badge bg-danger';
            case OperationType.READ:
                return 'badge bg-info';
            default:
                return 'badge bg-secondary';
        }
    }
}
