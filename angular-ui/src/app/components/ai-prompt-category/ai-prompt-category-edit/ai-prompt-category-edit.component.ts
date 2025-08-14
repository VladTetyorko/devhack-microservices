import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AiPromptCategoryService} from '../../../services/global/ai/ai-prompt-category.service';
import {AiPromptCategoryModel} from '../../../models/global/ai/ai-prompt-category.model';

/**
 * Component for editing existing AI prompt categories.
 * Provides a form interface for AI prompt category editing with validation and pre-populated data.
 */
@Component({
    selector: 'app-ai-prompt-category-edit',
    templateUrl: './ai-prompt-category-edit.component.html',
    styleUrls: ['./ai-prompt-category-edit.component.css']
})
export class AiPromptCategoryEditComponent implements OnInit {
    categoryForm: FormGroup;
    isSubmitting = false;
    isLoading = true;
    error = '';
    successMessage = '';
    categoryId: string | null = null;
    originalCategory: AiPromptCategoryModel | null = null;

    constructor(
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private aiPromptCategoryService: AiPromptCategoryService
    ) {
        this.categoryForm = this.createForm();
    }

    ngOnInit(): void {
        this.categoryId = this.route.snapshot.paramMap.get('id');
        if (this.categoryId) {
            this.loadCategory();
        } else {
            this.error = 'AI Prompt Category ID not provided';
            this.isLoading = false;
        }
    }

    /**
     * Create the reactive form for AI prompt category editing
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
                Validators.pattern(/^[A-Z0-9_]+$/) // Uppercase letters, numbers, underscores only
            ]],
            description: ['', [
                Validators.maxLength(500)
            ]]
        });
    }

    /**
     * Load existing AI prompt category data
     */
    loadCategory(): void {
        if (!this.categoryId) return;

        this.isLoading = true;
        this.error = '';

        this.aiPromptCategoryService.getById(this.categoryId).subscribe({
            next: (category) => {
                console.log('[DEBUG_LOG] Loaded AI prompt category for editing:', category);
                this.originalCategory = category;
                this.populateForm(category);
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading AI prompt category for editing:', err);
                this.error = 'Failed to load AI prompt category data. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    /**
     * Populate form with existing AI prompt category data
     */
    private populateForm(category: AiPromptCategoryModel): void {
        this.categoryForm.patchValue({
            name: category.name || '',
            code: category.code || '',
            description: category.description || ''
        });
    }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.categoryForm.invalid || !this.categoryId) {
            this.markFormGroupTouched();
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const categoryData: Partial<AiPromptCategoryModel> = {
            id: this.categoryId,
            name: this.categoryForm.get('name')?.value?.trim(),
            code: this.categoryForm.get('code')?.value?.trim().toUpperCase(),
            description: this.categoryForm.get('description')?.value?.trim() || undefined
        };

        console.log('[DEBUG_LOG] Updating AI prompt category with data:', categoryData);

        this.aiPromptCategoryService.update(this.categoryId, categoryData as AiPromptCategoryModel).subscribe({
            next: (updatedCategory) => {
                console.log('[DEBUG_LOG] AI prompt category updated successfully:', updatedCategory);
                this.successMessage = `AI Prompt Category "${updatedCategory.name}" has been updated successfully.`;

                // Navigate to the updated category's detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/ai-prompt-categories', updatedCategory.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error updating AI prompt category:', err);
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
        return 'An unexpected error occurred while updating the AI prompt category.';
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
                return `${fieldLabel} must contain only uppercase letters, numbers, and underscores.`;
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
     * Cancel category editing and navigate back
     */
    cancel(): void {
        if (this.hasChanges() && !confirm('You have unsaved changes. Are you sure you want to cancel?')) {
            return;
        }
        this.router.navigate(['/ai-prompt-categories', this.categoryId]);
    }

    /**
     * Reset the form to its original state
     */
    resetForm(): void {
        if (this.originalCategory) {
            this.populateForm(this.originalCategory);
        }
        this.error = '';
        this.successMessage = '';
        this.isSubmitting = false;
    }

    /**
     * Check if the form has changes compared to original data
     */
    hasChanges(): boolean {
        if (!this.originalCategory) return false;

        const currentValues = this.categoryForm.value;
        return (
            currentValues.name?.trim() !== this.originalCategory.name ||
            currentValues.code?.trim().toUpperCase() !== this.originalCategory.code ||
            (currentValues.description?.trim() || '') !== (this.originalCategory.description || '')
        );
    }
}