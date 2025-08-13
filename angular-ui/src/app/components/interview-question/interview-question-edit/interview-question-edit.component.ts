import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {InterviewQuestionService} from '../../../services/global/interview-question.service';
import {TagService} from '../../../services/global/tag.service';
import {InterviewQuestionDTO} from '../../../models/global/interview-question.model';
import {TagDTO} from '../../../models/global/tag.model';

/**
 * Component for editing existing interview questions.
 * Provides a form interface for question editing with validation and pre-populated data.
 */
@Component({
    selector: 'app-interview-question-edit',
    templateUrl: './interview-question-edit.component.html',
    styleUrls: ['./interview-question-edit.component.css']
})
export class InterviewQuestionEditComponent implements OnInit {
    questionForm: FormGroup;
    isSubmitting = false;
    isLoading = true;
    error = '';
    successMessage = '';
    questionId: string | null = null;
    originalQuestion: InterviewQuestionDTO | null = null;
    availableTags: TagDTO[] = [];
    selectedTags: string[] = [];

    difficultyLevels = [
        {value: 'EASY', label: 'Easy'},
        {value: 'MEDIUM', label: 'Medium'},
        {value: 'HARD', label: 'Hard'}
    ];

    constructor(
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private questionService: InterviewQuestionService,
        private tagService: TagService
    ) {
        this.questionForm = this.createForm();
    }

    ngOnInit(): void {
        this.questionId = this.route.snapshot.paramMap.get('id');
        if (this.questionId) {
            this.loadQuestion();
            this.loadTags();
        } else {
            this.error = 'Question ID not provided';
            this.isLoading = false;
        }
    }

    /**
     * Create the reactive form for question editing
     */
    private createForm(): FormGroup {
        return this.formBuilder.group({
            questionText: ['', [
                Validators.required,
                Validators.minLength(10),
                Validators.maxLength(2000)
            ]],
            difficulty: ['MEDIUM', [Validators.required]],
            source: ['', [Validators.maxLength(255)]],
            tags: [[]]
        });
    }

    /**
     * Load existing question data
     */
    loadQuestion(): void {
        if (!this.questionId) return;

        this.isLoading = true;
        this.error = '';

        this.questionService.getById(this.questionId).subscribe({
            next: (question) => {
                console.log('[DEBUG_LOG] Loaded question for editing:', question);
                this.originalQuestion = question;
                this.populateForm(question);
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading question for editing:', err);
                this.error = 'Failed to load question data. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    /**
     * Load available tags for selection
     */
    loadTags(): void {
        this.tagService.getAll().subscribe({
            next: (tags) => {
                this.availableTags = tags;
                console.log('[DEBUG_LOG] Loaded tags for question editing:', tags.length);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading tags:', err);
            }
        });
    }

    /**
     * Populate form with existing question data
     */
    private populateForm(question: InterviewQuestionDTO): void {
        this.questionForm.patchValue({
            questionText: question.questionText || '',
            difficulty: question.difficulty || 'MEDIUM',
            source: question.source || ''
        });

        // Set selected tags - since we only have tag IDs now, we'll start with empty selection
        // In a full implementation, we'd fetch tag details to get names for the UI
        this.selectedTags = [];
        this.questionForm.get('tags')?.setValue(this.selectedTags);
    }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.questionForm.invalid || !this.questionId) {
            this.markFormGroupTouched();
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const questionData: Partial<InterviewQuestionDTO> = {
            questionText: this.questionForm.get('questionText')?.value?.trim(),
            difficulty: this.questionForm.get('difficulty')?.value,
            source: this.questionForm.get('source')?.value?.trim() || undefined
        };

        console.log('[DEBUG_LOG] Updating question with data:', questionData);

        this.questionService.update(this.questionId, questionData as InterviewQuestionDTO).subscribe({
            next: (updatedQuestion) => {
                console.log('[DEBUG_LOG] Question updated successfully:', updatedQuestion);
                this.successMessage = `Question has been updated successfully.`;

                // Navigate to the updated question's detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/interview-questions', updatedQuestion.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error updating question:', err);
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
        return 'An unexpected error occurred while updating the question.';
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
     * Cancel editing and navigate back to question detail or questions list
     */
    cancel(): void {
        if (this.questionId) {
            this.router.navigate(['/interview-questions', this.questionId]);
        } else {
            this.router.navigate(['/interview-questions']);
        }
    }

    /**
     * Reset the form to original values
     */
    resetForm(): void {
        if (this.originalQuestion) {
            this.populateForm(this.originalQuestion);
        } else {
            this.questionForm.reset({
                difficulty: 'MEDIUM',
                tagNames: []
            });
            this.selectedTags = [];
        }
        this.error = '';
        this.successMessage = '';
    }

    /**
     * Check if form has been modified
     */
    get hasChanges(): boolean {
        if (!this.originalQuestion) return false;

        const currentQuestionText = this.questionForm.get('questionText')?.value?.trim() || '';
        const currentDifficulty = this.questionForm.get('difficulty')?.value || '';
        const currentSource = this.questionForm.get('source')?.value?.trim() || '';
        const currentExpectedAnswer = this.questionForm.get('expectedAnswer')?.value?.trim() || '';
        const currentHints = this.questionForm.get('hints')?.value?.trim() || '';

        const originalTagNames = this.originalQuestion.tagNames || [];
        const tagsChanged = this.selectedTags.length !== originalTagNames.length ||
            !this.selectedTags.every(tag => originalTagNames.includes(tag));

        return currentQuestionText !== (this.originalQuestion.questionText || '') ||
            currentDifficulty !== (this.originalQuestion.difficulty || '') ||
            currentSource !== (this.originalQuestion.source || '') ||
            currentExpectedAnswer !== (this.originalQuestion.expectedAnswer || '') ||
            currentHints !== (this.originalQuestion.hints || '') ||
            tagsChanged;
    }
}