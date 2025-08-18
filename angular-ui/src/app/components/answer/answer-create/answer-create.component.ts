import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AnswerService} from '../../../services/personalized/answer.service';
import {InterviewQuestionService} from '../../../services/global/interview-question.service';
import {AnswerDTO} from '../../../models/personalized/answer.model';
import {InterviewQuestionDTO} from '../../../models/global/interview-question.model';

@Component({
    selector: 'app-answer-create',
    templateUrl: './answer-create.component.html',
    styleUrls: ['./answer-create.component.css']
})
export class AnswerCreateComponent implements OnInit {
    answerForm!: FormGroup;
    isLoading = false;
    isSubmitting = false;
    error = '';
    success = '';
    questionId?: string;
    question?: InterviewQuestionDTO;
    characterCount = 0;
    maxCharacters = 5000;
    minCharacters = 5;

    constructor(
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private answerService: AnswerService,
        private questionService: InterviewQuestionService
    ) {
    }

    ngOnInit(): void {
        this.initializeForm();
        this.loadQuestionFromRoute();
    }

    private initializeForm(): void {
        this.answerForm = this.fb.group({
            text: ['', [
                Validators.required,
                Validators.minLength(this.minCharacters),
                Validators.maxLength(this.maxCharacters)
            ]],
            confidenceLevel: [5, [
                Validators.required,
                Validators.min(1),
                Validators.max(10)
            ]]
        });

        // Subscribe to text changes for character count
        this.answerForm.get('text')?.valueChanges.subscribe(value => {
            this.characterCount = value ? value.length : 0;
        });
    }

    private loadQuestionFromRoute(): void {
        this.questionId = this.route.snapshot.queryParamMap.get('questionId') || undefined;

        if (this.questionId) {
            this.loadQuestionDetails();
        }
    }

    private loadQuestionDetails(): void {
        if (!this.questionId) return;

        this.isLoading = true;
        this.questionService.getById(this.questionId).subscribe({
            next: (question) => {
                this.question = question;
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading question:', err);
                this.error = 'Failed to load question details. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    onSubmit(): void {
        if (this.answerForm.invalid || !this.questionId) {
            this.markFormGroupTouched();
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const answerData: AnswerDTO = {
            text: this.answerForm.get('text')?.value.trim(),
            confidenceLevel: this.answerForm.get('confidenceLevel')?.value,
            questionId: this.questionId
        };

        this.answerService.createForQuestion(this.questionId, answerData).subscribe({
            next: (createdAnswer) => {
                console.log('[DEBUG_LOG] Answer created successfully:', createdAnswer);
                this.success = 'Answer created successfully!';

                // Redirect to answer detail or back to question
                setTimeout(() => {
                    if (createdAnswer.id) {
                        this.router.navigate(['/answers', createdAnswer.id]);
                    } else {
                        this.router.navigate(['/interview-questions', this.questionId]);
                    }
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error creating answer:', err);
                this.error = 'Failed to create answer. ' + (err.error?.message || err.message || 'Unknown error');
                this.isSubmitting = false;
            }
        });
    }

    private markFormGroupTouched(): void {
        Object.keys(this.answerForm.controls).forEach(key => {
            const control = this.answerForm.get(key);
            control?.markAsTouched();
        });
    }

    onCancel(): void {
        if (this.questionId) {
            this.router.navigate(['/interview-questions', this.questionId]);
        } else {
            this.router.navigate(['/answers']);
        }
    }

    // Utility methods for template
    getCharacterCountClass(): string {
        if (this.characterCount < this.minCharacters) {
            return 'text-danger';
        } else if (this.characterCount > 4500) {
            return 'text-warning';
        }
        return 'text-muted';
    }

    getConfidenceText(value: number): string {
        if (value <= 3) return 'Low Confidence';
        if (value <= 6) return 'Moderate Confidence';
        if (value <= 8) return 'Good Confidence';
        return 'High Confidence';
    }

    getConfidenceBadgeClass(value: number): string {
        if (value <= 3) return 'badge bg-danger';
        if (value <= 6) return 'badge bg-warning';
        if (value <= 8) return 'badge bg-info';
        return 'badge bg-success';
    }

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
}