import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AiPromptService} from '../../../services/global/ai/ai-prompt.service';
import {AiPromptCategoryService} from '../../../services/global/ai/ai-prompt-category.service';
import {AiPromptModel} from '../../../models/global/ai/ai-prompt.model';
import {AiPromptCategoryModel} from '../../../models/global/ai/ai-prompt-category.model';

/**
 * Component for displaying detailed information about an AI prompt.
 * Shows prompt details, category information, and provides edit/delete actions.
 */
@Component({
    selector: 'app-ai-prompt-detail',
    templateUrl: './ai-prompt-detail.component.html',
    styleUrls: ['./ai-prompt-detail.component.css']
})
export class AiPromptDetailComponent implements OnInit {
    prompt: AiPromptModel | null = null;
    category: AiPromptCategoryModel | null = null;
    isLoading = true;
    isLoadingCategory = false;
    error = '';
    successMessage = '';
    promptId: string | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private aiPromptService: AiPromptService,
        private aiPromptCategoryService: AiPromptCategoryService
    ) {
    }

    ngOnInit(): void {
        this.promptId = this.route.snapshot.paramMap.get('id');
        if (this.promptId) {
            this.loadPrompt(this.promptId);
        } else {
            this.error = 'AI Prompt ID not provided';
            this.isLoading = false;
        }
    }

    /**
     * Load AI prompt details from the API
     */
    loadPrompt(id: string): void {
        this.isLoading = true;
        this.error = '';

        this.aiPromptService.getById(id).subscribe({
            next: (prompt) => {
                console.log('[DEBUG_LOG] Loaded AI prompt details:', prompt);
                // Normalize to support legacy template bindings
                const normalized: AiPromptModel = {
                    ...prompt,
                    code: prompt.key || prompt.code,
                    prompt: (prompt as any).userTemplate || prompt.prompt,
                    active: (prompt.enabled ?? (prompt as any).active) ?? false,
                    language: (prompt as any).language || 'en'
                };
                this.prompt = normalized;
                this.isLoading = false;

                // Load category information if available
                if (normalized.categoryId) {
                    this.loadCategory(normalized.categoryId);
                }
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading AI prompt details:', err);
                this.error = 'Failed to load AI prompt details. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    /**
     * Load category information for the prompt
     */
    loadCategory(categoryId: string): void {
        this.isLoadingCategory = true;
        this.aiPromptCategoryService.getById(categoryId).subscribe({
            next: (category) => {
                console.log('[DEBUG_LOG] Loaded category details:', category);
                this.category = category;
                this.isLoadingCategory = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading category details:', err);
                this.isLoadingCategory = false;
            }
        });
    }

    /**
     * Navigate to edit page
     */
    editPrompt(): void {
        if (this.promptId) {
            this.router.navigate(['/ai-prompts', this.promptId, 'edit']);
        }
    }

    /**
     * Delete the AI prompt with confirmation
     */
    deletePrompt(): void {
        if (!this.prompt || !this.promptId) return;

        const promptCode = this.prompt.code || 'this prompt';

        if (confirm(`Are you sure you want to delete "${promptCode}"? This action cannot be undone.`)) {
            this.aiPromptService.delete(this.promptId).subscribe({
                next: () => {
                    console.log('[DEBUG_LOG] AI prompt deleted successfully');
                    this.router.navigate(['/ai-prompts'], {
                        queryParams: {message: `AI Prompt "${promptCode}" has been successfully deleted.`}
                    });
                },
                error: (err) => {
                    console.error('[DEBUG_LOG] Error deleting AI prompt:', err);
                    this.error = 'Failed to delete AI prompt. ' + (err.error?.message || err.message || 'Unknown error');
                }
            });
        }
    }

    /**
     * Toggle active status of the prompt
     */
    toggleActive(): void {
        if (!this.prompt || !this.promptId) return;

        const isEnabled = (this.prompt.enabled ?? this.prompt.active) ?? false;
        const action = isEnabled ? 'deactivate' : 'activate';
        const service = isEnabled ?
            this.aiPromptService.deactivate(this.promptId) :
            this.aiPromptService.activate(this.promptId);

        service.subscribe({
            next: (updatedPrompt) => {
                console.log(`[DEBUG_LOG] AI prompt ${action}d successfully:`, updatedPrompt);
                const normalized: AiPromptModel = {
                    ...updatedPrompt,
                    code: updatedPrompt.key || (updatedPrompt as any).code,
                    prompt: (updatedPrompt as any).userTemplate || (updatedPrompt as any).prompt,
                    active: (updatedPrompt.enabled ?? (updatedPrompt as any).active) ?? false
                };
                this.prompt = normalized;
                const label = normalized.code || normalized.key || 'prompt';
                this.successMessage = `AI Prompt "${label}" has been ${action}d successfully.`;
                setTimeout(() => this.successMessage = '', 3000);
            },
            error: (err) => {
                console.error(`[DEBUG_LOG] Error ${action}ing AI prompt:`, err);
                this.error = `Failed to ${action} AI prompt. ` + (err.error?.message || err.message || 'Unknown error');
            }
        });
    }

    /**
     * Navigate back to prompts list
     */
    goBack(): void {
        this.router.navigate(['/ai-prompts']);
    }

    /**
     * Get badge class for active status
     */
    getActiveBadgeClass(): string {
        return this.prompt?.active ? 'bg-success' : 'bg-secondary';
    }

    /**
     * Get language display name
     */
    getLanguageDisplayName(): string {
        const languageMap: { [key: string]: string } = {
            'en': 'English',
            'es': 'Spanish',
            'fr': 'French',
            'de': 'German',
            'it': 'Italian',
            'pt': 'Portuguese',
            'ru': 'Russian',
            'zh': 'Chinese',
            'ja': 'Japanese',
            'ko': 'Korean'
        };

        return languageMap[(this.prompt as any)?.language || 'en'] || (this.prompt as any)?.language || 'English';
    }

    prettyPrintJson(value: any): string {
        try {
            if (!value) return '';
            return JSON.stringify(value, null, 2);
        } catch {
            return '';
        }
    }

    /**
     * Format arguments description for display
     */
    getFormattedArgsDescription(): string {
        if (!this.prompt?.argsDescription) return 'No arguments description provided';

        // Split by common delimiters and format as list
        const lines = this.prompt.argsDescription.split(/[,;]|\n/).map(line => line.trim()).filter(line => line);
        return lines.length > 1 ? lines.join('\n• ') : this.prompt.argsDescription;
    }
}
