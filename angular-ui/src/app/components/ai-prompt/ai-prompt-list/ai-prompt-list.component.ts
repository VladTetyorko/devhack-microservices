import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AiPromptService} from '../../../services/global/ai/ai-prompt.service';
import {AiPromptCategoryService} from '../../../services/global/ai/ai-prompt-category.service';
import {AiPromptModel} from '../../../models/global/ai/ai-prompt.model';
import {AiPromptCategoryModel} from '../../../models/global/ai/ai-prompt-category.model';
import {Page, PageRequest} from '../../../models/basic/page.model';
import {PAGINATION_DEFAULTS, SKELETON_CONFIG, ViewMode} from '../../../shared/constants/constraints';

@Component({
    selector: 'app-ai-prompt-list',
    templateUrl: './ai-prompt-list.component.html',
    styleUrls: ['./ai-prompt-list.component.css']
})
export class AiPromptListComponent implements OnInit {
    promptPage: Page<AiPromptModel> | null = null;
    allPrompts: AiPromptModel[] = [];
    isLoading = true;
    error = '';
    successMessage = '';

    // Search and filter properties
    searchTerm = '';
    selectedCategoryId = '';
    selectedActive = '';
    viewMode = ViewMode.TABLE;

    // Available options for filters
    availableCategories: AiPromptCategoryModel[] = [];

    // Pagination properties
    currentPageRequest: PageRequest = {
        page: PAGINATION_DEFAULTS.PAGE,
        size: PAGINATION_DEFAULTS.SIZE,
        sort: PAGINATION_DEFAULTS.DEFAULT_SORT
    };

    // Skeleton loading
    skeletonItems = SKELETON_CONFIG.ITEMS;

    constructor(
        private aiPromptService: AiPromptService,
        private aiPromptCategoryService: AiPromptCategoryService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.loadInitialData();
    }

    loadInitialData(): void {
        this.loadCategories();
        this.loadPrompts();
    }

    loadCategories(): void {
        this.aiPromptCategoryService.getAll().subscribe({
            next: (categories) => {
                this.availableCategories = categories;
                console.log('[DEBUG_LOG] Loaded categories for filtering:', categories.length);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading categories:', err);
            }
        });
    }

    loadPrompts(): void {
        this.isLoading = true;
        this.error = '';
        this.aiPromptService.getAllPaged(this.currentPageRequest).subscribe({
            next: (page) => {
                console.log('[DEBUG_LOG] Loaded prompts page:', page);
                this.promptPage = page;
                this.allPrompts = page.content || [];
                this.isLoading = false;

                console.log('[DEBUG_LOG] Total prompts in page:', page.content.length);
                console.log('[DEBUG_LOG] Total prompts overall:', page.totalElements);
                console.log('[DEBUG_LOG] Current page:', page.number + 1);
                console.log('[DEBUG_LOG] Total pages:', page.totalPages);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading prompts:', err);
                this.error = 'Failed to load AI prompts. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    onSearch(): void {
        this.applyFilters();
    }

    onCategoryFilter(): void {
        this.applyFilters();
    }

    onActiveFilter(): void {
        this.applyFilters();
    }

    applyFilters(): void {
        // For now, we'll reload the data when filters change
        // In a more advanced implementation, we could send filter parameters to the backend
        this.currentPageRequest.page = 0; // Reset to first page when filtering
        this.loadPrompts();
    }

    clearSearch(): void {
        this.searchTerm = '';
        this.applyFilters();
    }

    clearCategoryFilter(): void {
        this.selectedCategoryId = '';
        this.applyFilters();
    }

    clearActiveFilter(): void {
        this.selectedActive = '';
        this.applyFilters();
    }

    viewDetail(id: string | number): void {
        this.router.navigate(['/ai-prompts', id]);
    }

    editPrompt(id: string | number): void {
        this.router.navigate(['/ai-prompts', id, 'edit']);
    }

    createNew(): void {
        this.router.navigate(['/ai-prompts/create']);
    }

    deletePrompt(deleteEvent: { id: string | number, event: MouseEvent }): void {
        deleteEvent.event.stopPropagation();

        const prompt = this.allPrompts.find(p => p.id === deleteEvent.id);
        const promptCode = prompt?.key || prompt?.code || 'this prompt';

        if (confirm(`Are you sure you want to delete "${promptCode}"? This action cannot be undone.`)) {
            this.aiPromptService.delete(deleteEvent.id.toString()).subscribe({
                next: () => {
                    this.successMessage = `AI Prompt "${promptCode}" has been successfully deleted.`;
                    this.loadPrompts();
                    // Clear success message after 5 seconds
                    setTimeout(() => this.successMessage = '', 5000);
                },
                error: (err) => {
                    this.error = 'Failed to delete AI prompt. ' + err.message;
                }
            });
        }
    }

    toggleActive(prompt: AiPromptModel): void {
        const isEnabled = (prompt.enabled ?? prompt.active) ?? false;
        const promptLabel = prompt.key || prompt.code || 'prompt';
        if (isEnabled) {
            this.aiPromptService.deactivate(prompt.id!).subscribe({
                next: () => {
                    this.successMessage = `AI Prompt "${promptLabel}" has been deactivated.`;
                    this.loadPrompts();
                    setTimeout(() => this.successMessage = '', 3000);
                },
                error: (err) => this.error = 'Failed to deactivate prompt. ' + (err.error?.message || err.message)
            });
        } else {
            this.aiPromptService.activate(prompt.id!).subscribe({
                next: () => {
                    this.successMessage = `AI Prompt "${promptLabel}" has been activated.`;
                    this.loadPrompts();
                    setTimeout(() => this.successMessage = '', 3000);
                },
                error: (err) => this.error = 'Failed to activate prompt. ' + (err.error?.message || err.message)
            });
        }
    }

    // Pagination event handlers
    onPageChange(page: number): void {
        this.currentPageRequest.page = page;
        this.loadPrompts();
    }

    onPageSizeChange(size: number): void {
        this.currentPageRequest.size = size;
        this.currentPageRequest.page = 0; // Reset to first page when changing size
        this.loadPrompts();
    }

    // Getter for filtered prompts (for template compatibility)
    get filteredPrompts(): AiPromptModel[] {
        if (!this.promptPage) return [];

        let filtered = [...this.promptPage.content];

        // Apply search filter
        if (this.searchTerm.trim()) {
            const searchLower = this.searchTerm.toLowerCase().trim();
            filtered = filtered.filter(prompt =>
                prompt.key?.toLowerCase().includes(searchLower) ||
                prompt.code?.toLowerCase().includes(searchLower) ||
                prompt.userTemplate?.toLowerCase().includes(searchLower) ||
                prompt.prompt?.toLowerCase().includes(searchLower) ||
                prompt.description?.toLowerCase().includes(searchLower) ||
                prompt.categoryName?.toLowerCase().includes(searchLower)
            );
        }

        // Apply category filter
        if (this.selectedCategoryId) {
            filtered = filtered.filter(prompt => prompt.categoryId === this.selectedCategoryId);
        }

        // Apply active/enabled filter
        if (this.selectedActive !== '') {
            const isActive = this.selectedActive === 'true';
            filtered = filtered.filter(prompt => (prompt.enabled ?? prompt.active) === isActive);
        }

        return filtered;
    }

    // Utility methods for the template
    getActiveBadgeClass(active?: boolean): string {
        return active ? 'bg-success' : 'bg-secondary';
    }

    truncateText(text: string, maxLength: number = 100): string {
        if (!text) return '';
        return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    }

    trackByPromptId(index: number, prompt: AiPromptModel): string {
        return prompt.id || index.toString();
    }
}
