import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AiPromptUsageLogService} from '../../../services/ai-prompt-usage-log.service';
import {AiPromptUsageLogModel} from '../../../models/global/ai/ai-prompt-usage-log.model';

@Component({
    selector: 'app-ai-prompt-usage-log-detail',
    templateUrl: './ai-prompt-usage-log-detail.component.html',
    styleUrls: ['./ai-prompt-usage-log-detail.component.css']
})
export class AiPromptUsageLogDetailComponent implements OnInit {
    log: AiPromptUsageLogModel | null = null;
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private aiPromptUsageLogService: AiPromptUsageLogService
    ) {
    }

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadLog(id);
        } else {
            this.error = 'No usage log ID provided';
            this.isLoading = false;
        }
    }

    loadLog(id: string): void {
        this.isLoading = true;
        this.aiPromptUsageLogService.getById(id).subscribe({
            next: (data) => {
                this.log = data;
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load AI prompt usage log. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    goBack(): void {
        this.router.navigate(['/ai-prompt-usage-logs']);
    }

    deleteLog(): void {
        if (!this.log) return;

        if (confirm('Are you sure you want to delete this usage log?')) {
            this.aiPromptUsageLogService.delete(this.log.id!).subscribe({
                next: () => {
                    this.router.navigate(['/ai-prompt-usage-logs']);
                },
                error: (err) => {
                    this.error = 'Failed to delete usage log. ' + err.message;
                }
            });
        }
    }

    viewPrompt(): void {
        if (this.log?.promptId) {
            this.router.navigate(['/ai-prompts', this.log.promptId]);
        }
    }
}