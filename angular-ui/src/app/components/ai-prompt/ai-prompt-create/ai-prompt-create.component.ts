import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AiPromptService} from '../../../services/global/ai/ai-prompt.service';
import {AiPromptCategoryService} from '../../../services/global/ai/ai-prompt-category.service';
import {AiPromptModel} from '../../../models/global/ai/ai-prompt.model';
import {AiPromptCategoryModel} from '../../../models/global/ai/ai-prompt-category.model';

/**
 * Component for creating new AI prompts.
 * Provides a form interface for AI prompt creation with validation.
 */
@Component({
    selector: 'app-ai-prompt-create',
    templateUrl: './ai-prompt-create.component.html',
    styleUrls: ['./ai-prompt-create.component.css']
})
export class AiPromptCreateComponent implements OnInit {
    promptForm: FormGroup;
    isSubmitting = false;
    isLoadingCategories = false;
    error = '';
    successMessage = '';
    availableCategories: AiPromptCategoryModel[] = [];
    preselectedCategoryId: string | null = null;

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
        // Check if category is preselected from query params
        this.preselectedCategoryId = this.route.snapshot.queryParamMap.get('categoryId');
        this.loadCategories();
    }

    /**
     * Create the reactive form for AI prompt creation
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
     * Load available categories for selection
     */
    loadCategories(): void {
        this.isLoadingCategories = true;
        this.aiPromptCategoryService.getAll().subscribe({
            next: (categories) => {
                this.availableCategories = categories;
                this.isLoadingCategories = false;

                // Set preselected category if provided
                if (this.preselectedCategoryId) {
                    this.promptForm.get('categoryId')?.setValue(this.preselectedCategoryId);
                }

                console.log('[DEBUG_LOG] Loaded categories for prompt creation:', categories.length);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading categories:', err);
                this.error = 'Failed to load categories. Please refresh the page.';
                this.isLoadingCategories = false;
            }
        });
    }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.promptForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const promptData: Partial<AiPromptModel> = {
            code: this.promptForm.get('code')?.value?.trim().toUpperCase(),
            prompt: this.promptForm.get('prompt')?.value?.trim(),
            description: this.promptForm.get('description')?.value?.trim() || undefined,
            categoryId: this.promptForm.get('categoryId')?.value,
            language: this.promptForm.get('language')?.value?.trim() || 'en',
            active: this.promptForm.get('active')?.value ?? true,
            amountOfArguments: this.promptForm.get('amountOfArguments')?.value || 0,
            argsDescription: this.promptForm.get('argsDescription')?.value?.trim() || undefined
        };

        console.log('[DEBUG_LOG] Creating AI prompt with data:', promptData);

        this.aiPromptService.create(promptData as AiPromptModel).subscribe({
            next: (createdPrompt) => {
                console.log('[DEBUG_LOG] AI prompt created successfully:', createdPrompt);
                this.successMessage = `AI Prompt "${createdPrompt.code}" has been created successfully.`;

                // Navigate to the created prompt's detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/ai-prompts', createdPrompt.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error creating AI prompt:', err);
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
        return 'An unexpected error occurred while creating the AI prompt.';
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
     * Auto-generate code from prompt text
     */
    onPromptChange(): void {
        const promptValue = this.promptForm.get('prompt')?.value;
        if (promptValue && !this.promptForm.get('code')?.dirty) {
            const generatedCode = promptValue
                .split(' ')
                .slice(0, 3) // Take first 3 words
                .join('_')
                .toUpperCase()
                .replace(/[^A-Z0-9_]/g, '') // Remove special characters
                .substring(0, 50); // Limit length

            if (generatedCode) {
                this.promptForm.get('code')?.setValue(generatedCode);
            }
        }
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
     * Cancel prompt creation and navigate back
     */
    cancel(): void {
        if (this.promptForm.dirty && !confirm('You have unsaved changes. Are you sure you want to cancel?')) {
            return;
        }

        // Navigate back to category if preselected, otherwise to prompts list
        if (this.preselectedCategoryId) {
            this.router.navigate(['/ai-prompt-categories', this.preselectedCategoryId]);
        } else {
            this.router.navigate(['/ai-prompts']);
        }
    }

    /**
     * Reset the form to its initial state
     */
    resetForm(): void {
        this.promptForm.reset({
            language: 'en',
            active: true,
            amountOfArguments: 0
        });

        // Restore preselected category if provided
        if (this.preselectedCategoryId) {
            this.promptForm.get('categoryId')?.setValue(this.preselectedCategoryId);
        }

        this.error = '';
        this.successMessage = '';
        this.isSubmitting = false;
    }
}