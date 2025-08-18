import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {InterviewQuestionService} from '../../../services/global/interview-question.service';
import {TagService} from '../../../services/global/tag.service';
import {InterviewQuestionDTO} from '../../../models/global/interview-question.model';
import {TagDTO} from '../../../models/global/tag.model';
import {DIFFICULTY_LEVELS, QUESTION_CONSTRAINTS} from '../../../shared/constants/constraints';

/**
 * Component for creating new interview questions.
 * Provides a form interface for question creation with validation.
 */
@Component({
    selector: 'app-interview-question-create',
    templateUrl: './interview-question-create.component.html',
    styleUrls: ['./interview-question-create.component.css']
})
export class InterviewQuestionCreateComponent implements OnInit {
    questionForm: FormGroup;
    isSubmitting = false;
    error = '';
    successMessage = '';
    availableTags: TagDTO[] = [];
    selectedTags: string[] = [];

    difficultyLevels = DIFFICULTY_LEVELS;

    constructor(
        private formBuilder: FormBuilder,
        private router: Router,
        private questionService: InterviewQuestionService,
        private tagService: TagService
    ) {
        this.questionForm = this.createForm();
    }

    ngOnInit(): void {
        this.loadTags();
    }

    /**
     * Create the reactive form for question creation
     */
    private createForm(): FormGroup {
        return this.formBuilder.group({
            questionText: ['', [
                Validators.required,
                Validators.minLength(QUESTION_CONSTRAINTS.MIN_QUESTION_LENGTH),
                Validators.maxLength(QUESTION_CONSTRAINTS.MAX_QUESTION_LENGTH)
            ]],
            difficulty: ['MEDIUM', [Validators.required]],
            source: ['', [Validators.maxLength(QUESTION_CONSTRAINTS.MAX_SOURCE_LENGTH)]],
            expectedAnswer: ['', [Validators.maxLength(QUESTION_CONSTRAINTS.MAX_EXPECTED_ANSWER_LENGTH)]],
            hints: ['', [Validators.maxLength(QUESTION_CONSTRAINTS.MAX_HINTS_LENGTH)]],
            tagNames: [[]]
        });
    }

    /**
     * Load available tags for selection
     */
    loadTags(): void {
        this.tagService.getAll().subscribe({
            next: (tags) => {
                this.availableTags = tags;
                console.log('[DEBUG_LOG] Loaded tags for question creation:', tags.length);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading tags:', err);
            }
        });
    }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.questionForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const questionData: Partial<InterviewQuestionDTO> = {
            questionText: this.questionForm.get('questionText')?.value?.trim(),
            difficulty: this.questionForm.get('difficulty')?.value,
            source: this.questionForm.get('source')?.value?.trim() || undefined,
            expectedAnswer: this.questionForm.get('expectedAnswer')?.value?.trim() || undefined,
            hints: this.questionForm.get('hints')?.value?.trim() || undefined,
            tagNames: this.selectedTags.length > 0 ? this.selectedTags : undefined
        };

        console.log('[DEBUG_LOG] Creating question with data:', questionData);

        this.questionService.create(questionData as InterviewQuestionDTO).subscribe({
            next: (createdQuestion) => {
                console.log('[DEBUG_LOG] Question created successfully:', createdQuestion);
                this.successMessage = `Question has been created successfully.`;

                // Navigate to the created question's detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/interview-questions', createdQuestion.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error creating question:', err);
                this.error = this.getErrorMessage(err);
                this.isSubmitting = false;
            }
        });
    }

    /**
     * Extract user-friendly error message from error response
     */
    private getErrorMessage(error: any): string {
        if (error.error?.message) {
            return error.error.message;
        }
        if (error.error?.errors) {
            // Handle validation errors
            const validationErrors = Object.values(error.error.errors).flat();
            return validationErrors.join(', ');
        }
        if (error.message) {
            return error.message;
        }
        return 'An unexpected error occurred while creating the question.';
    }

    /**
     * Mark all form controls as touched to show validation errors
     */
    private markFormGroupTouched(): void {
        Object.keys(this.questionForm.controls).forEach(key => {
            const control = this.questionForm.get(key);
            control?.markAsTouched();
        });
    }

    /**
     * Check if a form field has validation errors and is touched
     */
    hasFieldError(fieldName: string): boolean {
        const field = this.questionForm.get(fieldName);
        return !!(field && field.invalid && field.touched);
    }

    /**
     * Get validation error message for a specific field
     */
    getFieldError(fieldName: string): string {
        const field = this.questionForm.get(fieldName);
        if (!field || !field.errors || !field.touched) {
            return '';
        }

        const errors = field.errors;
        if (errors['required']) {
            return `${this.getFieldLabel(fieldName)} is required.`;
        }
        if (errors['minlength']) {
            return `${this.getFieldLabel(fieldName)} must be at least ${errors['minlength'].requiredLength} characters.`;
        }
        if (errors['maxlength']) {
            return `${this.getFieldLabel(fieldName)} cannot exceed ${errors['maxlength'].requiredLength} characters.`;
        }

        return `${this.getFieldLabel(fieldName)} is invalid.`;
    }

    /**
     * Get user-friendly field label
     */
    private getFieldLabel(fieldName: string): string {
        const labels: { [key: string]: string } = {
            questionText: 'Question text',
            difficulty: 'Difficulty',
            source: 'Source',
            expectedAnswer: 'Expected answer',
            hints: 'Hints'
        };
        return labels[fieldName] || fieldName;
    }

    /**
     * Handle tag selection
     */
    onTagToggle(tagName: string): void {
        const index = this.selectedTags.indexOf(tagName);
        if (index > -1) {
            this.selectedTags.splice(index, 1);
        } else {
            this.selectedTags.push(tagName);
        }
        this.questionForm.get('tagNames')?.setValue(this.selectedTags);
    }

    /**
     * Check if tag is selected
     */
    isTagSelected(tagName: string): boolean {
        return this.selectedTags.includes(tagName);
    }

    /**
     * Cancel creation and navigate back to questions list
     */
    cancel(): void {
        this.router.navigate(['/interview-questions']);
    }

    /**
     * Reset the form to initial state
     */
    resetForm(): void {
        this.questionForm.reset({
            difficulty: 'MEDIUM',
            tagNames: []
        });
        this.selectedTags = [];
        this.error = '';
        this.successMessage = '';
    }
}