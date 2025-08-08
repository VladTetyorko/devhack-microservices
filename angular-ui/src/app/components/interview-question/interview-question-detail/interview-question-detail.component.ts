import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {InterviewQuestionService} from '../../../services/global/interview-question.service';
import {InterviewQuestionDTO} from '../../../models/global/interview-question.model';

@Component({
    selector: 'app-interview-question-detail',
    templateUrl: './interview-question-detail.component.html',
    styleUrls: ['./interview-question-detail.component.css']
})
export class InterviewQuestionDetailComponent implements OnInit {
    questionId!: string;
    isLoading = true;
    error = '';
    success = '';
    question?: InterviewQuestionDTO;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private questionService: InterviewQuestionService
    ) {
    }

    ngOnInit(): void {
        this.questionId = this.route.snapshot.paramMap.get('id')!;
        this.loadQuestionDetails();
    }

    loadQuestionDetails(): void {
        this.isLoading = true;
        this.error = '';
        this.questionService.getById(this.questionId).subscribe({
            next: (questionData) => {
                console.log('[DEBUG_LOG] Loaded question details:', questionData);
                this.question = {
                    ...questionData,
                    createdAt: this.convertDateFormat(questionData.createdAt),
                    updatedAt: this.convertDateFormat(questionData.updatedAt)
                };
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading question details:', err);
                this.error = 'Failed to load question details. ' + (err.error?.message || err.message || 'Unknown error');
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

    generateQuestions(): void {
        if (!this.question?.tags || this.question.tags.length === 0) {
            this.error = 'Cannot generate questions: No tags associated with this question.';
            return;
        }

        const tagName = this.question.tags[0].name;
        const difficulty = this.question.difficulty || 'MEDIUM';

        this.questionService.generateQuestions(tagName, 5, difficulty).subscribe({
            next: (result) => {
                this.success = 'Question generation started successfully!';
                setTimeout(() => this.success = '', 3000);
            },
            error: (err) => {
                this.error = 'Failed to generate questions. ' + (err.error?.message || err.message || 'Unknown error');
            }
        });
    }

    deleteQuestion(): void {
        if (!this.question?.id) return;

        const questionText = this.question.questionText?.substring(0, 50) + '...' || 'this question';
        if (confirm(`Are you sure you want to delete "${questionText}"? This action cannot be undone.`)) {
            this.questionService.delete(this.question.id).subscribe({
                next: () => {
                    this.router.navigate(['/interview-questions']);
                },
                error: (err) => {
                    this.error = 'Failed to delete question. ' + (err.error?.message || err.message || 'Unknown error');
                }
            });
        }
    }

    editQuestion(): void {
        if (this.question?.id) {
            this.router.navigate(['/interview-questions', this.question.id, 'edit']);
        }
    }

    goBack(): void {
        this.router.navigate(['/interview-questions']);
    }

    // Utility methods for the template
    getDifficultyBadgeClass(difficulty?: string): string {
        switch (difficulty?.toUpperCase()) {
            case 'EASY':
                return 'bg-success';
            case 'MEDIUM':
                return 'bg-warning text-dark';
            case 'HARD':
                return 'bg-danger';
            default:
                return 'bg-secondary';
        }
    }

    formatDate(dateString?: string): string {
        if (!dateString) return 'N/A';
        try {
            return new Date(dateString).toLocaleString();
        } catch {
            return dateString;
        }
    }

    getTagsText(): string {
        if (!this.question?.tags || this.question.tags.length === 0) {
            return 'No tags';
        }
        return this.question.tags.map(tag => tag.name).join(', ');
    }
}
