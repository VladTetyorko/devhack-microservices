import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TagService} from '../../../services/global/tag.service';
import {TagDTO, TagUpdateRequest} from '../../../models/global/tag.model';

/**
 * Component for editing existing categories (root tags).
 * Provides a form interface for category editing with validation.
 */
@Component({
    selector: 'app-category-edit',
    templateUrl: './category-edit.component.html',
    styleUrls: ['./category-edit.component.css']
})
export class CategoryEditComponent implements OnInit {
    categoryForm: FormGroup;
    isSubmitting = false;
    isLoading = true;
    error = '';
    successMessage = '';
    categoryId: string | null = null;
    originalCategory: TagDTO | null = null;

    constructor(
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private tagService: TagService
    ) {
        this.categoryForm = this.createForm();
    }

    ngOnInit(): void {
        this.categoryId = this.route.snapshot.paramMap.get('id');
        if (this.categoryId) {
            this.loadCategory();
        } else {
            this.error = 'Category ID not found';
            this.isLoading = false;
        }
    }

    /**
     * Create the reactive form for category editing
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
     * Load the category data from the server
     */
    private loadCategory(): void {
        if (!this.categoryId) return;

        this.isLoading = true;
        this.error = '';

        this.tagService.getById(this.categoryId).subscribe({
            next: (category) => {
                this.originalCategory = category;
                this.populateForm(category);
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading category:', err);
                this.error = 'Failed to load category. ' + err.message;
                this.isLoading = false;
            }
        });
    }

    /**
     * Populate the form with category data
     */
    private populateForm(category: TagDTO): void {
        this.categoryForm.patchValue({
            name: category.name,
            description: category.description || ''
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

        if (!this.categoryId || !this.originalCategory) {
            this.error = 'Category data not available';
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const formValues = this.categoryForm.value;
        const categoryUpdateRequest: TagUpdateRequest = {
            id: this.categoryId,
            name: formValues.name?.trim(),
            description: formValues.description?.trim() || undefined
        };

        console.log('[DEBUG_LOG] Updating category with data:', categoryUpdateRequest);

        this.tagService.update(this.categoryId, categoryUpdateRequest).subscribe({
            next: (updatedCategory) => {
                console.log('[DEBUG_LOG] Category updated successfully:', updatedCategory);
                this.successMessage = `Category "${updatedCategory.name}" has been updated successfully.`;
                this.originalCategory = updatedCategory;

                // Navigate to the category detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/categories', updatedCategory.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error updating category:', err);
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
        return 'An unexpected error occurred while updating the category.';
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
     * Cancel category editing and navigate back
     */
    cancel(): void {
        if (this.hasChanges() && !confirm('You have unsaved changes. Are you sure you want to cancel?')) {
            return;
        }
        this.router.navigate(['/categories', this.categoryId]);
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
     * Check if the form has unsaved changes
     */
    hasChanges(): boolean {
        if (!this.originalCategory) return false;

        const formValues = this.categoryForm.value;
        return (
            formValues.name?.trim() !== this.originalCategory.name ||
            (formValues.description?.trim() || '') !== (this.originalCategory.description || '')
        );
    }
}