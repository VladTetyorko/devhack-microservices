import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AiPromptUsageLogService} from '../../../services/global/ai/ai-prompt-usage-log.service';
import {AiPromptUsageLogModel} from '../../../models/global/ai/ai-prompt-usage-log.model';

@Component({
    selector: 'app-ai-prompt-usage-log-list',
    templateUrl: './ai-prompt-usage-log-list.component.html',
    styleUrls: ['./ai-prompt-usage-log-list.component.css']
})
export class AiPromptUsageLogListComponent implements OnInit {
    logs: AiPromptUsageLogModel[] = [];
    isLoading = true;
    error = '';
    showMyLogsOnly = false;

    constructor(
        private aiPromptUsageLogService: AiPromptUsageLogService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadLogs();
    }

    loadLogs(): void {
        this.isLoading = true;
        const service = this.showMyLogsOnly ?
            this.aiPromptUsageLogService.getMyLogs() :
            this.aiPromptUsageLogService.getAll();

        service.subscribe({
            next: (data) => {
                this.logs = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load AI prompt usage logs. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    viewDetail(id: string): void {
        this.router.navigate(['/ai-prompt-usage-logs', id]);
    }

    toggleMyLogs(): void {
        this.showMyLogsOnly = !this.showMyLogsOnly;
        this.loadLogs();
    }

    deleteLog(id: string, event: Event): void {
        event.stopPropagation();
        if (confirm('Are you sure you want to delete this usage log?')) {
            this.aiPromptUsageLogService.delete(id).subscribe({
                next: () => {
                    this.loadLogs();
                },
                error: (err) => {
                    this.error = 'Failed to delete usage log. ' + err.message;
                }
            });
        }
    }
}
