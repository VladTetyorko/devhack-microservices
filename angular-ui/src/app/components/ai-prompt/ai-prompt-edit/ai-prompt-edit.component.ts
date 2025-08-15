import {Component, OnInit} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
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

    // Highlighting and schema validation state
    highlightedUserTemplate?: SafeHtml;
    highlightedSystemTemplate?: SafeHtml;
    templateVariables: Set<string> = new Set<string>();
    schemaVariables: Set<string> = new Set<string>();
    missingVariables: string[] = [];

    constructor(
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private aiPromptService: AiPromptService,
        private aiPromptCategoryService: AiPromptCategoryService,
        private sanitizer: DomSanitizer
    ) {
        this.promptForm = this.createForm();
    }

    ngOnInit(): void {
        // initialize highlighting & schema check listeners
        this.setupLivePreviewAndSchemaChecks();
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
                Validators.pattern(/^[A-Za-z0-9_]+$/) // Letters (uppercase/lowercase), numbers, underscores only
            ]],
            prompt: ['', [
                Validators.required,
                Validators.minLength(10),
                Validators.maxLength(20000)
            ]],
            systemTemplate: ['', [
                Validators.maxLength(20000)
            ]],
            description: ['', [
                Validators.maxLength(5000)
            ]],
            categoryId: ['', [
                Validators.required
            ]],
            // UI-level fields mapped to DTO
            model: ['gpt-3.5-turbo', [
                Validators.maxLength(50)
            ]],
            version: [1],
            enabled: [true],
            // Legacy UI fields kept for compatibility
            language: ['en', [
                Validators.maxLength(10)
            ]],
            active: [true],
            amountOfArguments: [0, [
                Validators.min(0),
                Validators.max(20)
            ]],
            argsDescription: ['', [
                Validators.maxLength(5000)
            ]],
            // Advanced JSON configuration as string textareas
            argsSchemaJson: [''],
            defaultsJson: [''],
            parametersJson: [''],
            responseContractJson: ['']
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
        // ensure initial previews reflect loaded data after patchValue
        const normalized: AiPromptModel = {
            ...prompt,
            code: prompt.key || prompt.code || '',
            prompt: (prompt as any).userTemplate || prompt.prompt || '',
            active: (prompt.enabled ?? (prompt as any).active) ?? true,
            language: (prompt as any).language || 'en'
        };
        this.promptForm.patchValue({
            code: normalized.code,
            prompt: normalized.prompt,
            systemTemplate: (prompt as any).systemTemplate || '',
            description: normalized.description || '',
            categoryId: normalized.categoryId || '',
            model: (prompt as any).model || 'gpt-3.5-turbo',
            version: (prompt as any).version || 1,
            enabled: (prompt as any).enabled ?? normalized.active ?? true,
            language: normalized.language || 'en',
            active: normalized.active ?? true,
            amountOfArguments: normalized.amountOfArguments || 0,
            argsDescription: normalized.argsDescription || '',
            argsSchemaJson: this.safeStringify((prompt as any).argsSchema),
            defaultsJson: this.safeStringify((prompt as any).defaults),
            parametersJson: this.safeStringify((prompt as any).parameters),
            responseContractJson: this.safeStringify((prompt as any).responseContract)
        });
        this.recomputeTemplateAndSchemaState();
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

        // Transform to new DTO and preserve server-managed fields where possible
        const key = this.promptForm.get('code')?.value?.trim();
        const userTemplate = this.promptForm.get('prompt')?.value?.trim();
        const enabled = this.promptForm.get('active')?.value ?? true;
        const description = this.promptForm.get('description')?.value?.trim() || undefined;
        const categoryId = this.promptForm.get('categoryId')?.value;

        // Parse JSON configuration fields (fail-fast on invalid input)
        const argsSchema = this.parseJsonControl('argsSchemaJson', this.originalPrompt?.argsSchema ?? {});
        if (argsSchema === null) return; // error already set
        const defaults = this.parseJsonControl('defaultsJson', this.originalPrompt?.defaults ?? {});
        if (defaults === null) return;
        const parameters = this.parseJsonControl('parametersJson', this.originalPrompt?.parameters ?? {});
        if (parameters === null) return;
        const responseContract = this.parseJsonControl('responseContractJson', this.originalPrompt?.responseContract ?? undefined);
        if (responseContract === null) return;

        const promptData: Partial<AiPromptModel> = {
            id: this.promptId!,
            key,
            userTemplate,
            systemTemplate: this.promptForm.get('systemTemplate')?.value?.trim() || undefined,
            enabled: this.promptForm.get('enabled')?.value ?? enabled,
            description,
            categoryId,
            model: this.promptForm.get('model')?.value || this.originalPrompt?.model || 'gpt-3.5-turbo',
            version: this.promptForm.get('version')?.value || this.originalPrompt?.version || 1,
            argsSchema: argsSchema as any,
            defaults: defaults as any,
            parameters: parameters as any,
            responseContract: responseContract as any
        };

        console.log('[DEBUG_LOG] Updating AI prompt with data:', promptData);

        this.aiPromptService.update(this.promptId, promptData as AiPromptModel).subscribe({
            next: (updatedPrompt) => {
                console.log('[DEBUG_LOG] AI prompt updated successfully:', updatedPrompt);
                const label = (updatedPrompt as any).key || (updatedPrompt as any).code || 'prompt';
                this.successMessage = `AI Prompt "${label}" has been updated successfully.`;

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

    private safeStringify(value: any): string {
        try {
            if (value === undefined || value === null) return '';
            return JSON.stringify(value, null, 2);
        } catch {
            return '';
        }
    }

    // Setup listeners for live preview and schema checks
    private setupLivePreviewAndSchemaChecks(): void {
        const update = () => this.recomputeTemplateAndSchemaState();
        this.promptForm.get('prompt')?.valueChanges.subscribe(update);
        this.promptForm.get('systemTemplate')?.valueChanges.subscribe(update);
        this.promptForm.get('argsSchemaJson')?.valueChanges.subscribe(update);
        // Run once initially in case form has defaults
        setTimeout(update, 0);
    }

    private recomputeTemplateAndSchemaState(): void {
        const userT = this.promptForm.get('prompt')?.value || '';
        const sysT = this.promptForm.get('systemTemplate')?.value || '';

        const vars = new Set<string>();
        this.extractVariables(userT).forEach(v => vars.add(v));
        this.extractVariables(sysT).forEach(v => vars.add(v));
        this.templateVariables = vars;

        // Build highlighted HTML
        this.highlightedUserTemplate = this.buildHighlightedHtml(userT, vars);
        this.highlightedSystemTemplate = this.buildHighlightedHtml(sysT, vars);

        // Parse schema and compute allowed variable names
        const schemaObj = this.tryParseSchema(this.promptForm.get('argsSchemaJson')?.value);
        const schemaVars = new Set<string>(schemaObj);
        this.schemaVariables = schemaVars;

        // Missing vars: used in templates but absent in schema
        this.missingVariables = Array.from(vars).filter(v => !schemaVars.has(v)).sort();

        // Auto-update amountOfArguments to number of unique variables
        const count = vars.size;
        const ctrl = this.promptForm.get('amountOfArguments');
        if (ctrl && ctrl.value !== count) {
            ctrl.setValue(count, {emitEvent: false});
        }
    }

    private tryParseSchema(raw: string): string[] {
        if (!raw || (raw.trim() === '')) return [];
        try {
            const obj = JSON.parse(raw);
            // Prefer JSON Schema style: { properties: { var1: {}, var2: {} } }
            if (obj && typeof obj === 'object') {
                if (obj.properties && typeof obj.properties === 'object') {
                    return Object.keys(obj.properties);
                }
                // Fallback: use top-level keys if it's a plain object map
                return Object.keys(obj);
            }
            return [];
        } catch {
            // Don't set global error here; live preview should be lenient
            return [];
        }
    }

    private extractVariables(text: string): string[] {
        if (!text) return [];
        const re = /\{\{\s*([a-zA-Z_][a-zA-Z0-9_\.]*)\s*\}\}/g;
        const set = new Set<string>();
        let m: RegExpExecArray | null;
        while ((m = re.exec(text)) !== null) {
            // keep full var name (including dot paths), but count by first segment for args names
            const full = m[1];
            const base = full.split('.')[0];
            set.add(base);
        }
        return Array.from(set);
    }

    private buildHighlightedHtml(text: string, knownVars: Set<string>): SafeHtml {
        if (!text) return '';
        // Escape HTML first
        const escape = (s: string) => s
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
        const escaped = escape(text);
        // Now replace variable tokens
        const pattern = /\{\{\s*([a-zA-Z_][a-zA-Z0-9_\.]*)\s*\}\}/g;
        const html = escaped.replace(pattern, (_match, p1) => {
            const base = String(p1).split('.')[0];
            const cls = knownVars.has(base) ? 'var-token' : 'var-token var-invalid';
            return `<span class="${cls}">{{${p1}}}</span>`;
        });
        // Wrap in <pre> to preserve formatting
        return this.sanitizer.bypassSecurityTrustHtml(`<pre class="template-preview">${html}</pre>`);
    }

    private parseJsonControl(controlName: string, fallback: any): any | null {
        const raw = this.promptForm.get(controlName)?.value;
        if (!raw || (typeof raw === 'string' && raw.trim() === '')) {
            return fallback;
        }
        try {
            return JSON.parse(raw);
        } catch (e: any) {
            const label = this.getFieldLabel(controlName);
            this.error = `${label} contains invalid JSON. Please fix it. ${e?.message ? '(' + e.message + ')' : ''}`;
            this.isSubmitting = false;
            return null;
        }
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
                return `${fieldLabel} must contain only letters, numbers, and underscores.`;
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
            'code': 'Prompt key',
            'prompt': 'User template',
            'systemTemplate': 'System template',
            'description': 'Description',
            'categoryId': 'Category',
            'model': 'Model',
            'version': 'Version',
            'enabled': 'Enabled',
            'language': 'Language',
            'active': 'Active status',
            'amountOfArguments': 'Number of arguments',
            'argsDescription': 'Arguments description',
            'argsSchemaJson': 'Arguments schema (JSON)',
            'defaultsJson': 'Defaults (JSON)',
            'parametersJson': 'Parameters (JSON)',
            'responseContractJson': 'Response contract (JSON)'
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
            (currentValues.code?.trim() || '') !== ((this.originalPrompt as any).code || (this.originalPrompt as any).key || '') ||
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