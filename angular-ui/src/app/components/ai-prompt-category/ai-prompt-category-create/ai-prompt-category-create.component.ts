import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {AiPromptCategoryService} from '../../../services/global/ai/ai-prompt-category.service';
import {AiPromptCategoryModel} from '../../../models/global/ai/ai-prompt-category.model';

/**
 * Component for creating new AI prompt categories.
 * Provides a form interface for AI prompt category creation with validation.
 */
@Component({
    selector: 'app-ai-prompt-category-create',
    templateUrl: './ai-prompt-category-create.component.html',
    styleUrls: ['./ai-prompt-category-create.component.css']
})
export class AiPromptCategoryCreateComponent implements OnInit {
    categoryForm: FormGroup;
    isSubmitting = false;
    error = '';
    successMessage = '';

    constructor(
        private formBuilder: FormBuilder,
        private router: Router,
        private aiPromptCategoryService: AiPromptCategoryService
    ) {
        this.categoryForm = this.createForm();
    }

    ngOnInit(): void {
        // No additional initialization needed for AI prompt categories
    }

    /**
     * Create the reactive form for AI prompt category creation
     */
    private createForm(): FormGroup {
        return this.formBuilder.group({
            name: ['', [
                Validators.required,
                Validators.minLength(2),
                Validators.maxLength(100),
                Validators.pattern(/^[a-zA-Z0-9\s\-_]+$/) // Allow alphanumeric, spaces, hyphens, underscores
            ]],
            code: ['', [
                Validators.required,
                Validators.minLength(2),
                Validators.maxLength(50),
                Validators.pattern(/^[A-Za-z0-9_]+$/) // Letters (uppercase/lowercase), numbers, underscores only
            ]],
            description: ['', [
                Validators.maxLength(500)
            ]]
        });
    }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.categoryForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const categoryData: Partial<AiPromptCategoryModel> = {
            name: this.categoryForm.get('name')?.value?.trim(),
            code: this.categoryForm.get('code')?.value?.trim(),
            description: this.categoryForm.get('description')?.value?.trim() || undefined
        };

        console.log('[DEBUG_LOG] Creating AI prompt category with data:', categoryData);

        this.aiPromptCategoryService.create(categoryData as AiPromptCategoryModel).subscribe({
            next: (createdCategory) => {
                console.log('[DEBUG_LOG] AI prompt category created successfully:', createdCategory);
                this.successMessage = `AI Prompt Category "${createdCategory.name}" has been created successfully.`;

                // Navigate to the created category's detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/ai-prompt-categories', createdCategory.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error creating AI prompt category:', err);
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
        return 'An unexpected error occurred while creating the AI prompt category.';
    }

    /**
     * Mark all form fields as touched to trigger validation display
     */
    private markFormGroupTouched(): void {
        Object.keys(this.categoryForm.controls).forEach(key => {
            const control = this.categoryForm.get(key);
            if (control) {
                control.markAsTouched();
            }
        });
    }

    /**
     * Check if a specific field has validation errors and has been touched
     */
    hasFieldError(fieldName: string): boolean {
        const field = this.categoryForm.get(fieldName);
        return !!(field && field.invalid && field.touched);
    }

    /**
     * Get the validation error message for a specific field
     */
    getFieldError(fieldName: string): string {
        const field = this.categoryForm.get(fieldName);
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
        if (errors['pattern']) {
            if (fieldName === 'code') {
                return `${fieldLabel} must contain only letters, numbers, and underscores.`;
            }
            return `${fieldLabel} contains invalid characters. Only letters, numbers, spaces, hyphens, and underscores are allowed.`;
        }

        return `${fieldLabel} is invalid.`;
    }

    /**
     * Get user-friendly field label
     */
    private getFieldLabel(fieldName: string): string {
        const labels: { [key: string]: string } = {
            'name': 'Category name',
            'code': 'Category code',
            'description': 'Description'
        };
        return labels[fieldName] || fieldName;
    }

    /**
     * Auto-generate code from name
     */
    onNameChange(): void {
        const nameValue = this.categoryForm.get('name')?.value;
        if (nameValue && !this.categoryForm.get('code')?.dirty) {
            const generatedCode = nameValue
                .toUpperCase()
                .replace(/[^A-Z0-9\s]/g, '') // Remove special characters except spaces
                .replace(/\s+/g, '_') // Replace spaces with underscores
                .substring(0, 50); // Limit length

            this.categoryForm.get('code')?.setValue(generatedCode);
        }
    }

    /**
     * Cancel category creation and navigate back
     */
    cancel(): void {
        if (this.categoryForm.dirty && !confirm('You have unsaved changes. Are you sure you want to cancel?')) {
            return;
        }
        this.router.navigate(['/ai-prompt-categories']);
    }

    /**
     * Reset the form to its initial state
     */
    resetForm(): void {
        this.categoryForm.reset();
        this.error = '';
        this.successMessage = '';
        this.isSubmitting = false;
    }
}