import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AnswerService} from '../../../services/personalized/answer.service';
import {AnswerDTO} from '../../../models/personalized/answer.model';

@Component({
    selector: 'app-answer-detail',
    templateUrl: './answer-detail.component.html',
    styleUrls: ['./answer-detail.component.css']
})
export class AnswerDetailComponent implements OnInit {
    answerId!: string;
    isLoading = true;
    error = '';
    success = '';
    answer?: AnswerDTO;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private answerService: AnswerService
    ) {
    }

    ngOnInit(): void {
        this.answerId = this.route.snapshot.paramMap.get('id')!;
        this.loadAnswerDetails();
    }

    loadAnswerDetails(): void {
        this.isLoading = true;
        this.error = '';
        this.answerService.getById(this.answerId).subscribe({
            next: (answerData) => {
                console.log('[DEBUG_LOG] Loaded answer details:', answerData);
                this.answer = {
                    ...answerData,
                    createdAt: this.convertDateFormat(answerData.createdAt),
                    updatedAt: this.convertDateFormat(answerData.updatedAt)
                };
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading answer details:', err);
                this.error = 'Failed to load answer details. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    private convertDateFormat(dateString?: string): string | undefined {
        if (!dateString) return dateString;

        // Check if the date is in comma-separated format (e.g., "2025,7,7,15,36,51,426621000")
        if (dateString.includes(',')) {
            try {
                const parts = dateString.split(',').map(part => parseInt(part, 10));
                if (parts.length >= 6) {
                    // parts: [year, month, day, hour, minute, second, nanoseconds]
                    // Note: month is 1-based in the input, but Date constructor expects 0-based
                    const date = new Date(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
                    return date.toISOString();
                }
            } catch (error) {
                console.warn('Failed to parse date:', dateString, error);
            }
        }

        return dateString;
    }

    evaluateWithAI(): void {
        if (!this.answer?.id) return;

        this.answerService.evaluateWithAI(this.answer.id).subscribe({
            next: (evaluatedAnswer) => {
                this.answer = evaluatedAnswer;
                this.success = 'Answer evaluated successfully with AI!';
                setTimeout(() => this.success = '', 3000);
            },
            error: (err) => {
                this.error = 'Failed to evaluate answer with AI. ' + (err.error?.message || err.message || 'Unknown error');
            }
        });
    }

    deleteAnswer(): void {
        if (!this.answer?.id) return;

        const answerText = this.answer.text?.substring(0, 50) + '...' || 'this answer';
        if (confirm(`Are you sure you want to delete "${answerText}"? This action cannot be undone.`)) {
            this.answerService.delete(this.answer.id).subscribe({
                next: () => {
                    this.router.navigate(['/answers']);
                },
                error: (err) => {
                    this.error = 'Failed to delete answer. ' + (err.error?.message || err.message || 'Unknown error');
                }
            });
        }
    }

    editAnswer(): void {
        if (this.answer?.id) {
            this.router.navigate(['/answers', this.answer.id, 'edit']);
        }
    }

    goBack(): void {
        this.router.navigate(['/answers']);
    }

    // Utility methods for the template
    getScoreBadgeClass(score?: number): string {
        if (!score) return 'bg-secondary';
        if (score >= 80) return 'bg-success';
        if (score >= 60) return 'bg-warning text-dark';
        return 'bg-danger';
    }

    getConfidenceBadgeClass(confidence?: number): string {
        if (!confidence) return 'bg-secondary';
        if (confidence >= 80) return 'bg-success';
        if (confidence >= 60) return 'bg-info';
        return 'bg-warning text-dark';
    }

    formatDate(dateString?: string): string {
        if (!dateString) return 'N/A';
        try {
            return new Date(dateString).toLocaleString();
        } catch {
            return dateString;
        }
    }
}
