import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {TagService} from '../../../services/global/tag.service';
import {TagCreateRequest} from '../../../models/global/tag.model';

/**
 * Component for creating new categories (root tags).
 * Provides a form interface for category creation with validation.
 */
@Component({
    selector: 'app-category-create',
    templateUrl: './category-create.component.html',
    styleUrls: ['./category-create.component.css']
})
export class CategoryCreateComponent implements OnInit {
    categoryForm: FormGroup;
    isSubmitting = false;
    error = '';
    successMessage = '';

    constructor(
        private formBuilder: FormBuilder,
        private router: Router,
        private tagService: TagService
    ) {
        this.categoryForm = this.createForm();
    }

    ngOnInit(): void {
        // No additional initialization needed for categories
    }

    /**
     * Create the reactive form for category creation
     */
    private createForm(): FormGroup {
        return this.formBuilder.group({
            name: ['', [
                Validators.required,
                Validators.minLength(2),
                Validators.maxLength(50),
                Validators.pattern(/^[a-zA-Z0-9\s\-_]+$/) // Allow alphanumeric, spaces, hyphens, underscores
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

        const categoryCreateRequest: TagCreateRequest = {
            name: this.categoryForm.get('name')?.value?.trim(),
            description: this.categoryForm.get('description')?.value?.trim() || undefined,
            // No parentId for categories since they are root tags
        };

        console.log('[DEBUG_LOG] Creating category with data:', categoryCreateRequest);

        this.tagService.createWithParent(categoryCreateRequest).subscribe({
            next: (createdCategory) => {
                console.log('[DEBUG_LOG] Category created successfully:', createdCategory);
                this.successMessage = `Category "${createdCategory.name}" has been created successfully.`;

                // Navigate to the created category's detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/categories', createdCategory.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error creating category:', err);
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
        return 'An unexpected error occurred while creating the category.';
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
            'description': 'Description'
        };
        return labels[fieldName] || fieldName;
    }

    /**
     * Cancel category creation and navigate back
     */
    cancel(): void {
        if (this.categoryForm.dirty && !confirm('You have unsaved changes. Are you sure you want to cancel?')) {
            return;
        }
        this.router.navigate(['/categories']);
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