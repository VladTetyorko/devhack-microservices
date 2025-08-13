import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TagService} from '../../../services/global/tag.service';
import {TagDTO, TagMoveRequest, TagUpdateRequest} from '../../../models/global/tag.model';

/**
 * Component for editing existing tags.
 * Provides a form interface for tag editing with validation and pre-populated data.
 */
@Component({
    selector: 'app-tag-edit',
    templateUrl: './tag-edit.component.html',
    styleUrls: ['./tag-edit.component.css']
})
export class TagEditComponent implements OnInit {
    tagForm: FormGroup;
    isSubmitting = false;
    isLoading = true;
    error = '';
    successMessage = '';
    tagId: string | null = null;
    originalTag: TagDTO | null = null;
    availableParentTags: TagDTO[] = [];
    loadingParents = false;

    constructor(
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private tagService: TagService
    ) {
        this.tagForm = this.createForm();
    }

    ngOnInit(): void {
        this.tagId = this.route.snapshot.paramMap.get('id');
        if (this.tagId) {
            this.loadAvailableParentTags();
            this.loadTag();
        } else {
            this.error = 'Tag ID not provided';
            this.isLoading = false;
        }
    }

    /**
     * Create the reactive form for tag editing
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
            parentId: [''] // Optional parent tag selection
        });
    }

    /**
     * Load existing tag data
     */
    loadTag(): void {
        if (!this.tagId) return;

        this.isLoading = true;
        this.error = '';

        this.tagService.getById(this.tagId).subscribe({
            next: (tag) => {
                console.log('[DEBUG_LOG] Loaded tag for editing:', tag);
                this.originalTag = tag;
                this.populateForm(tag);
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading tag for editing:', err);
                this.error = 'Failed to load tag data. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    /**
     * Populate form with existing tag data
     */
    private populateForm(tag: TagDTO): void {
        this.tagForm.patchValue({
            name: tag.name || '',
            description: tag.description || '',
            parentId: tag.parent?.id || ''
        });
    }

    /**
     * Load available parent tags for selection (excluding current tag and its descendants)
     */
    private loadAvailableParentTags(): void {
        this.loadingParents = true;
        this.tagService.getAll().subscribe({
            next: (tags) => {
                // Filter out current tag and its descendants to prevent cycles
                this.availableParentTags = tags.filter(tag => {
                    if (!this.tagId) return true;
                    if (tag.id === this.tagId) return false;
                    // Check if tag is a descendant of current tag
                    if (tag.path && this.originalTag?.path) {
                        return !tag.path.startsWith(this.originalTag.path + '.');
                    }
                    return true;
                });
                this.loadingParents = false;
                console.log('[DEBUG_LOG] Loaded available parent tags:', this.availableParentTags.length);
            },
            error: (error) => {
                console.error('[DEBUG_LOG] Error loading parent tags:', error);
                this.loadingParents = false;
                // Don't show error to user as parent selection is optional
            }
        });
    }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.tagForm.invalid || !this.tagId || !this.originalTag) {
            this.markFormGroupTouched();
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const formValues = this.tagForm.value;
        const newParentId = formValues.parentId || null;
        const originalParentId = this.originalTag.parent?.id || null;

        // Check if parent has changed
        const parentChanged = newParentId !== originalParentId;

        if (parentChanged) {
            // Handle parent change using moveTag
            this.handleParentChange(newParentId, formValues);
        } else {
            // Handle basic tag update
            this.handleBasicUpdate(formValues);
        }
    }

    /**
     * Handle parent change using moveTag
     */
    private handleParentChange(newParentId: string | null, formValues: any): void {
        const moveRequest: TagMoveRequest = {
            tagId: this.tagId!,
            newParentId: newParentId || undefined
        };

        console.log('[DEBUG_LOG] Moving tag to new parent:', moveRequest);

        this.tagService.moveTag(moveRequest).subscribe({
            next: (updatedTag) => {
                console.log('[DEBUG_LOG] Tag moved successfully:', updatedTag);
                // After moving, update other fields if they changed
                this.handleBasicUpdate(formValues, updatedTag);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error moving tag:', err);
                this.error = this.getErrorMessage(err);
                this.isSubmitting = false;
            }
        });
    }

    /**
     * Handle basic tag update (name, description)
     */
    private handleBasicUpdate(formValues: any, existingTag?: TagDTO): void {
        const tagUpdateRequest: TagUpdateRequest = {
            id: this.tagId!,
            name: formValues.name?.trim(),
            description: formValues.description?.trim() || undefined
        };

        console.log('[DEBUG_LOG] Updating tag with data:', tagUpdateRequest);

        this.tagService.update(this.tagId!, tagUpdateRequest as TagDTO).subscribe({
            next: (updatedTag) => {
                console.log('[DEBUG_LOG] Tag updated successfully:', updatedTag);
                this.successMessage = `Tag "${updatedTag.name}" has been updated successfully.`;
                this.originalTag = updatedTag; // Update original tag reference

                // Navigate to the updated tag's detail page after a short delay
                setTimeout(() => {
                    this.router.navigate(['/tags', updatedTag.id]);
                }, 1500);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error updating tag:', err);
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
        return 'An unexpected error occurred while updating the tag.';
    }

    /**
     * Mark all form controls as touched to show validation errors
     */
    private markFormGroupTouched(): void {
        Object.keys(this.tagForm.controls).forEach(key => {
            const control = this.tagForm.get(key);
            control?.markAsTouched();
        });
    }

    /**
     * Check if a form field has validation errors and is touched
     */
    hasFieldError(fieldName: string): boolean {
        const field = this.tagForm.get(fieldName);
        return !!(field && field.invalid && field.touched);
    }

    /**
     * Get validation error message for a specific field
     */
    getFieldError(fieldName: string): string {
        const field = this.tagForm.get(fieldName);
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
            name: 'Tag name',
            description: 'Description',
            parentId: 'Parent tag'
        };
        return labels[fieldName] || fieldName;
    }

    /**
     * Cancel editing and navigate back to tag detail or tags list
     */
    cancel(): void {
        if (this.tagId) {
            this.router.navigate(['/tags', this.tagId]);
        } else {
            this.router.navigate(['/tags']);
        }
    }

    /**
     * Reset the form to original values
     */
    resetForm(): void {
        if (this.originalTag) {
            this.populateForm(this.originalTag);
        } else {
            this.tagForm.reset();
        }
        this.error = '';
        this.successMessage = '';
    }

    /**
     * Check if form has been modified
     */
    get hasChanges(): boolean {
        if (!this.originalTag) return false;

        const currentName = this.tagForm.get('name')?.value?.trim() || '';
        const currentDescription = this.tagForm.get('description')?.value?.trim() || '';
        const currentParentId = this.tagForm.get('parentId')?.value || '';

        return currentName !== (this.originalTag.name || '') ||
            currentDescription !== (this.originalTag.description || '') ||
            currentParentId !== (this.originalTag.parent?.id || '');
    }
}