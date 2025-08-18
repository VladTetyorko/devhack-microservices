import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TagService} from '../../../services/global/tag.service';
import {TagCreateRequest, TagDTO} from '../../../models/global/tag.model';

/**
 * Component for creating new topics (child tags).
 * Provides a form interface for topic creation with validation.
 */
@Component({
    selector: 'app-tag-create',
    templateUrl: './tag-create.component.html',
    styleUrls: ['./tag-create.component.css']
})
export class TagCreateComponent implements OnInit {
    topicForm: FormGroup;
    isSubmitting = false;
    error = '';
    successMessage = '';
    availableCategories: TagDTO[] = [];
    loadingCategories = false;
    preselectedCategoryId: string | null = null;

    constructor(
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private tagService: TagService
    ) {
        this.topicForm = this.createForm();
    }

    ngOnInit(): void {
        // Check for preselected category from query parameters
        this.route.queryParams.subscribe(params => {
            this.preselectedCategoryId = params['parent'] || null;
            this.loadAvailableCategories();
        });
    }

    /**
     * Create the reactive form for topic creation
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
            ]],
            categoryId: ['', Validators.required] // Required parent category selection for topics
        });
    }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.topicForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const categoryId = this.topicForm.get('categoryId')?.value;
        const topicCreateRequest: TagCreateRequest = {
            name: this.topicForm.get('name')?.value?.trim(),
            description: this.topicForm.get('description')?.value?.trim() || undefined,
            parentId: categoryId
        };

        console.log('[DEBUG_LOG] Creating topic with data:', topicCreateRequest);

        this.tagService.createWithParent(topicCreateRequest).subscribe({
            next: (createdTopic) => {
                console.log('[DEBUG_LOG] Topic created successfully:', createdTopic);
                this.successMessage = `Topic "${createdTopic.name}" has been created successfully.`;

                // Navigate to the created topic's detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/topics', createdTopic.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error creating topic:', err);
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
        return 'An unexpected error occurred while creating the topic.';
    }

    /**
     * Mark all form controls as touched to show validation errors
     */
    private markFormGroupTouched(): void {
        Object.keys(this.topicForm.controls).forEach(key => {
            const control = this.topicForm.get(key);
            control?.markAsTouched();
        });
    }

    /**
     * Check if a form field has validation errors and is touched
     */
    hasFieldError(fieldName: string): boolean {
        const field = this.topicForm.get(fieldName);
        return !!(field && field.invalid && field.touched);
    }

    /**
     * Get validation error message for a specific field
     */
    getFieldError(fieldName: string): string {
        const field = this.topicForm.get(fieldName);
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
        if (errors['pattern']) {
            return `${this.getFieldLabel(fieldName)} contains invalid characters. Only letters, numbers, spaces, hyphens, and underscores are allowed.`;
        }

        return `${this.getFieldLabel(fieldName)} is invalid.`;
    }

    /**
     * Get user-friendly field label
     */
    private getFieldLabel(fieldName: string): string {
        const labels: { [key: string]: string } = {
            name: 'Topic name',
            description: 'Description',
            categoryId: 'Category'
        };
        return labels[fieldName] || fieldName;
    }

    /**
     * Load available categories (root tags) for selection
     */
    private loadAvailableCategories(): void {
        this.loadingCategories = true;
        this.tagService.getRootTags().subscribe({
            next: (categories) => {
                this.availableCategories = categories;
                this.loadingCategories = false;
                console.log('[DEBUG_LOG] Loaded available categories:', categories.length);

                // Set preselected category if provided
                if (this.preselectedCategoryId) {
                    this.topicForm.patchValue({
                        categoryId: this.preselectedCategoryId
                    });
                }
            },
            error: (error) => {
                console.error('[DEBUG_LOG] Error loading categories:', error);
                this.loadingCategories = false;
                // Show error to user as category selection is required for topics
                this.error = 'Failed to load categories. Please try again.';
            }
        });
    }

    /**
     * Cancel creation and navigate back to topics list
     */
    cancel(): void {
        if (this.topicForm.dirty && !confirm('You have unsaved changes. Are you sure you want to cancel?')) {
            return;
        }
        this.router.navigate(['/topics']);
    }

    /**
     * Reset the form to initial state
     */
    resetForm(): void {
        this.topicForm.reset();
        this.error = '';
        this.successMessage = '';
        this.isSubmitting = false;

        // Reset preselected category if provided
        if (this.preselectedCategoryId) {
            this.topicForm.patchValue({
                categoryId: this.preselectedCategoryId
            });
        }
    }
}