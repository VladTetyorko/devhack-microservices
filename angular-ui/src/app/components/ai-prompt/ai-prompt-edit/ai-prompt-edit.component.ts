import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AiPromptService} from '../../../services/global/ai/ai-prompt.service';
import {AiPromptCategoryService} from '../../../services/global/ai/ai-prompt-category.service';
import {AiPromptModel} from '../../../models/global/ai/ai-prompt.model';
import {AiPromptCategoryModel} from '../../../models/global/ai/ai-prompt-category.model';

/**
 * Component for editing existing AI prompts.
 * Provides a form interface for AI prompt editing with validation and pre-populated data.
 */
@Component({
    selector: 'app-ai-prompt-edit',
    templateUrl: './ai-prompt-edit.component.html',
    styleUrls: ['./ai-prompt-edit.component.css']
})
export class AiPromptEditComponent implements OnInit {
    promptForm: FormGroup;
    isSubmitting = false;
    isLoading = true;
    isLoadingCategories = false;
    error = '';
    successMessage = '';
    promptId: string | null = null;
    originalPrompt: AiPromptModel | null = null;
    availableCategories: AiPromptCategoryModel[] = [];

    constructor(
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private aiPromptService: AiPromptService,
        private aiPromptCategoryService: AiPromptCategoryService
    ) {
        this.promptForm = this.createForm();
    }

    ngOnInit(): void {
        this.promptId = this.route.snapshot.paramMap.get('id');
        if (this.promptId) {
            this.loadPrompt();
            this.loadCategories();
        } else {
            this.error = 'AI Prompt ID not provided';
            this.isLoading = false;
        }
    }

    /**
     * Create the reactive form for AI prompt editing
     */
    private createForm(): FormGroup {
        return this.formBuilder.group({
            code: ['', [
                Validators.required,
                Validators.minLength(2),
                Validators.maxLength(50),
                Validators.pattern(/^[A-Z0-9_]+$/) // Uppercase letters, numbers, underscores only
            ]],
            prompt: ['', [
                Validators.required,
                Validators.minLength(10),
                Validators.maxLength(2000)
            ]],
            description: ['', [
                Validators.maxLength(500)
            ]],
            categoryId: ['', [
                Validators.required
            ]],
            language: ['en', [
                Validators.maxLength(10)
            ]],
            active: [true],
            amountOfArguments: [0, [
                Validators.min(0),
                Validators.max(20)
            ]],
            argsDescription: ['', [
                Validators.maxLength(500)
            ]]
        });
    }

    /**
     * Load existing AI prompt data
     */
    loadPrompt(): void {
        if (!this.promptId) return;

        this.isLoading = true;
        this.error = '';

        this.aiPromptService.getById(this.promptId).subscribe({
            next: (prompt) => {
                console.log('[DEBUG_LOG] Loaded AI prompt for editing:', prompt);
                this.originalPrompt = prompt;
                this.populateForm(prompt);
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading AI prompt for editing:', err);
                this.error = 'Failed to load AI prompt data. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    /**
     * Load available categories for selection
     */
    loadCategories(): void {
        this.isLoadingCategories = true;
        this.aiPromptCategoryService.getAll().subscribe({
            next: (categories) => {
                this.availableCategories = categories;
                this.isLoadingCategories = false;
                console.log('[DEBUG_LOG] Loaded categories for prompt editing:', categories.length);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading categories:', err);
                this.isLoadingCategories = false;
            }
        });
    }

    /**
     * Populate form with existing AI prompt data
     */
    private populateForm(prompt: AiPromptModel): void {
        this.promptForm.patchValue({
            code: prompt.code || '',
            prompt: prompt.prompt || '',
            description: prompt.description || '',
            categoryId: prompt.categoryId || '',
            language: prompt.language || 'en',
            active: prompt.active ?? true,
            amountOfArguments: prompt.amountOfArguments || 0,
            argsDescription: prompt.argsDescription || ''
        });
    }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.promptForm.invalid || !this.promptId) {
            this.markFormGroupTouched();
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const promptData: Partial<AiPromptModel> = {
            id: this.promptId,
            code: this.promptForm.get('code')?.value?.trim().toUpperCase(),
            prompt: this.promptForm.get('prompt')?.value?.trim(),
            description: this.promptForm.get('description')?.value?.trim() || undefined,
            categoryId: this.promptForm.get('categoryId')?.value,
            language: this.promptForm.get('language')?.value?.trim() || 'en',
            active: this.promptForm.get('active')?.value ?? true,
            amountOfArguments: this.promptForm.get('amountOfArguments')?.value || 0,
            argsDescription: this.promptForm.get('argsDescription')?.value?.trim() || undefined
        };

        console.log('[DEBUG_LOG] Updating AI prompt with data:', promptData);

        this.aiPromptService.update(this.promptId, promptData as AiPromptModel).subscribe({
            next: (updatedPrompt) => {
                console.log('[DEBUG_LOG] AI prompt updated successfully:', updatedPrompt);
                this.successMessage = `AI Prompt "${updatedPrompt.code}" has been updated successfully.`;

                // Navigate to the updated prompt's detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/ai-prompts', updatedPrompt.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error updating AI prompt:', err);
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
        return 'An unexpected error occurred while updating the AI prompt.';
    }

    /**
     * Mark all form fields as touched to trigger validation display
     */
    private markFormGroupTouched(): void {
        Object.keys(this.promptForm.controls).forEach(key => {
            const control = this.promptForm.get(key);
            if (control) {
                control.markAsTouched();
            }
        });
    }

    /**
     * Check if a specific field has validation errors and has been touched
     */
    hasFieldError(fieldName: string): boolean {
        const field = this.promptForm.get(fieldName);
        return !!(field && field.invalid && field.touched);
    }

    /**
     * Get the validation error message for a specific field
     */
    getFieldError(fieldName: string): string {
        const field = this.promptForm.get(fieldName);
        if (!field || !field.errors || !field.touched) {
            return '';
        }

        const errors = field.errors;
        const fieldLabel = this.getFieldLabel(fieldName);

        if (errors['required']) {
            return `${fieldLabel} is required.`;
        }
        if (errors['minlength']) {
            return `${fieldLabel} must be at least ${errors['minlength'].requiredLength} characters long.`;
        }
        if (errors['maxlength']) {
            return `${fieldLabel} cannot exceed ${errors['maxlength'].requiredLength} characters.`;
        }
        if (errors['min']) {
            return `${fieldLabel} must be at least ${errors['min'].min}.`;
        }
        if (errors['max']) {
            return `${fieldLabel} cannot exceed ${errors['max'].max}.`;
        }
        if (errors['pattern']) {
            if (fieldName === 'code') {
                return `${fieldLabel} must contain only uppercase letters, numbers, and underscores.`;
            }
            return `${fieldLabel} has invalid format.`;
        }

        return `${fieldLabel} is invalid.`;
    }

    /**
     * Get user-friendly field label
     */
    private getFieldLabel(fieldName: string): string {
        const labels: { [key: string]: string } = {
            'code': 'Prompt code',
            'prompt': 'Prompt text',
            'description': 'Description',
            'categoryId': 'Category',
            'language': 'Language',
            'active': 'Active status',
            'amountOfArguments': 'Number of arguments',
            'argsDescription': 'Arguments description'
        };
        return labels[fieldName] || fieldName;
    }

    /**
     * Get selected category name for display
     */
    getSelectedCategoryName(): string {
        const categoryId = this.promptForm.get('categoryId')?.value;
        if (!categoryId) return '';

        const category = this.availableCategories.find(c => c.id === categoryId);
        return category ? category.name : '';
    }

    /**
     * Cancel prompt editing and navigate back
     */
    cancel(): void {
        if (this.hasChanges() && !confirm('You have unsaved changes. Are you sure you want to cancel?')) {
            return;
        }
        this.router.navigate(['/ai-prompts', this.promptId]);
    }

    /**
     * Reset the form to its original state
     */
    resetForm(): void {
        if (this.originalPrompt) {
            this.populateForm(this.originalPrompt);
        }
        this.error = '';
        this.successMessage = '';
        this.isSubmitting = false;
    }

    /**
     * Check if the form has changes compared to original data
     */
    hasChanges(): boolean {
        if (!this.originalPrompt) return false;

        const currentValues = this.promptForm.value;
        return (
            currentValues.code?.trim().toUpperCase() !== this.originalPrompt.code ||
            currentValues.prompt?.trim() !== this.originalPrompt.prompt ||
            (currentValues.description?.trim() || '') !== (this.originalPrompt.description || '') ||
            currentValues.categoryId !== this.originalPrompt.categoryId ||
            (currentValues.language?.trim() || 'en') !== (this.originalPrompt.language || 'en') ||
            (currentValues.active ?? true) !== (this.originalPrompt.active ?? true) ||
            (currentValues.amountOfArguments || 0) !== (this.originalPrompt.amountOfArguments || 0) ||
            (currentValues.argsDescription?.trim() || '') !== (this.originalPrompt.argsDescription || '')
        );
    }
}