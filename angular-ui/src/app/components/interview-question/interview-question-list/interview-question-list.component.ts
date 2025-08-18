import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {
    InterviewQuestionService,
    QuestionSearchParams,
    QuestionStats
} from '../../../services/global/interview-question.service';
import {InterviewQuestionDTO} from '../../../models/global/interview-question.model';
import {Page, PageRequest} from '../../../models/basic/page.model';
import {TagService} from '../../../services/global/tag.service';
import {TagDTO as TagModel} from '../../../models/global/tag.model';
import {
    QuestionWebSocketMessage,
    QuestionWebSocketService
} from '../../../services/websocket/question-websocket.service';
import {Subject, Subscription} from 'rxjs';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {
    DIFFICULTY_LEVELS,
    PAGINATION_DEFAULTS,
    SEARCH_DEFAULTS,
    SKELETON_CONFIG,
    ViewMode
} from '../../../shared/constants/constraints';

@Component({
    selector: 'app-interview-question-list',
    templateUrl: './interview-question-list.component.html',
    styleUrls: ['./interview-question-list.component.css']
})
export class InterviewQuestionListComponent implements OnInit, OnDestroy {
    questionPage: Page<InterviewQuestionDTO> | null = null;
    isLoading = true;
    error = '';
    successMessage = '';

    // Statistics
    questionStats: QuestionStats | null = null;
    isLoadingStats = false;

    // Granular loading states
    isLoadingSearch = false;
    isLoadingList = false;
    isLoadingTable = false; // Separate loading state for table content only

    // WebSocket subscription management
    private webSocketSubscription: Subscription | null = null;

    // Debounced search functionality
    private searchSubject = new Subject<string>();
    private searchSubscription: Subscription | null = null;

    // Search and filter properties
    searchParams: QuestionSearchParams = {};
    searchTerm = '';
    selectedDifficulty = '';
    selectedTagId = '';
    viewMode = ViewMode.TABLE;
    showAdvancedSearch = false;

    // Available options for filters
    availableTags: TagModel[] = [];
    difficultyLevels = DIFFICULTY_LEVELS;

    // Pagination properties
    currentPageRequest: PageRequest = {
        page: PAGINATION_DEFAULTS.PAGE,
        size: PAGINATION_DEFAULTS.SIZE,
        sort: PAGINATION_DEFAULTS.DEFAULT_SORT
    };

    // Skeleton loading
    skeletonItems = SKELETON_CONFIG.ITEMS;

    constructor(
        private questionService: InterviewQuestionService,
        private tagService: TagService,
        private router: Router,
        private webSocketService: QuestionWebSocketService
    ) {
    }

    ngOnInit(): void {
        this.loadInitialData();
        this.subscribeToWebSocketUpdates();
        this.setupDebouncedSearch();
    }

    ngOnDestroy(): void {
        if (this.webSocketSubscription) {
            this.webSocketSubscription.unsubscribe();
        }
        if (this.searchSubscription) {
            this.searchSubscription.unsubscribe();
        }
    }

    loadInitialData(): void {
        this.loadTags();
        this.loadQuestionStats();
        this.loadQuestions();
    }

    setupDebouncedSearch(): void {
        this.searchSubscription = this.searchSubject.pipe(
            debounceTime(SEARCH_DEFAULTS.DEBOUNCE_TIME), // Wait for user to stop typing
            distinctUntilChanged() // Only emit if search term has changed
        ).subscribe(searchTerm => {
            console.log('[DEBUG_LOG] Debounced search triggered for:', searchTerm);
            this.performSearch();
        });
    }

    subscribeToWebSocketUpdates(): void {
        this.webSocketSubscription = this.webSocketService.getMessages().subscribe({
            next: (message: QuestionWebSocketMessage) => {
                console.log('[DEBUG_LOG] Received WebSocket message:', message);
                this.handleWebSocketMessage(message);
            },
            error: (error) => {
                console.error('[DEBUG_LOG] WebSocket error:', error);
                this.error = 'WebSocket connection error. Real-time updates may not work.';
            }
        });
    }

    private handleWebSocketMessage(message: QuestionWebSocketMessage): void {
        switch (message.type) {
            case 'QUESTION_CREATED':
                this.handleQuestionCreated(message.data);
                break;
            case 'QUESTION_UPDATED':
                this.handleQuestionUpdated(message.data);
                break;
            case 'QUESTION_DELETED':
                this.handleQuestionDeleted(message.data.id);
                break;
            default:
                console.warn('[DEBUG_LOG] Unknown WebSocket message type:', message.type);
        }
    }

    private handleQuestionCreated(question: InterviewQuestionDTO): void {
        console.log('[DEBUG_LOG] Handling question created:', question);

        // Check if the new question matches current filters
        if (this.questionMatchesFilters(question)) {
            // Add the question to the current page if there's space, otherwise reload
            if (this.questionPage && this.questionPage.content && this.currentPageRequest.size &&
                this.questionPage.content.length < this.currentPageRequest.size) {
                // Add to current page without full reload
                this.questionPage.content.unshift(question);
                this.questionPage.totalElements++;
                console.log('[DEBUG_LOG] Added question to current page without reload');
            } else {
                // Page is full, need to reload to maintain pagination
                this.loadQuestions();
            }

            // Update statistics without full reload
            this.updateStatsAfterCreate();
            this.successMessage = 'New question added!';
            setTimeout(() => this.successMessage = '', 3000);
        }
    }

    private handleQuestionUpdated(question: InterviewQuestionDTO): void {
        console.log('[DEBUG_LOG] Handling question updated:', question);

        if (this.questionPage && this.questionPage.content) {
            const index = this.questionPage.content.findIndex(q => q.id === question.id);
            if (index !== -1) {
                // Check if updated question still matches filters
                if (this.questionMatchesFilters(question)) {
                    this.questionPage.content[index] = question;
                    this.successMessage = 'Question updated!';
                } else {
                    // Question no longer matches filters, reload page
                    this.loadQuestions();
                }
            } else if (this.questionMatchesFilters(question)) {
                // Question now matches filters but wasn't in current page, reload
                this.loadQuestions();
            }
            setTimeout(() => this.successMessage = '', 3000);
        }
    }

    private handleQuestionDeleted(questionId: string): void {
        console.log('[DEBUG_LOG] Handling question deleted:', questionId);

        if (this.questionPage && this.questionPage.content) {
            const index = this.questionPage.content.findIndex(q => q.id === questionId);
            if (index !== -1) {
                this.questionPage.content.splice(index, 1);
                this.questionPage.totalElements--;

                // Update statistics without full reload
                this.updateStatsAfterDelete();
                this.successMessage = 'Question deleted!';
                setTimeout(() => this.successMessage = '', 3000);

                // If current page is empty and not the first page, go to previous page
                if (this.questionPage.content.length === 0 && this.currentPageRequest.page && this.currentPageRequest.page > 0) {
                    this.currentPageRequest.page--;
                    this.loadQuestions();
                }
            }
        }
    }

    private questionMatchesFilters(question: InterviewQuestionDTO): boolean {
        // Check search term
        if (this.searchTerm.trim()) {
            const searchLower = this.searchTerm.toLowerCase();
            const questionText = (question.questionText || '').toLowerCase();
            if (!questionText.includes(searchLower)) {
                return false;
            }
        }

        // Check difficulty filter
        if (this.selectedDifficulty && question.difficulty !== this.selectedDifficulty) {
            return false;
        }

        // Check tag filter
        if (this.selectedTagId) {
            const hasMatchingTag = question.tagIds?.includes(this.selectedTagId);
            if (!hasMatchingTag) {
                return false;
            }
        }

        return true;
    }

    private updateStatsAfterCreate(): void {
        if (this.questionStats) {
            this.questionStats.totalQuestions++;
            // Note: We don't increment userQuestions here as we don't know if the current user created it
            console.log('[DEBUG_LOG] Updated stats after question creation');
        }
    }

    private updateStatsAfterDelete(): void {
        if (this.questionStats) {
            this.questionStats.totalQuestions--;
            // Note: We don't decrement userQuestions here as we don't know if the current user owned it
            console.log('[DEBUG_LOG] Updated stats after question deletion');
        }
    }

    loadTags(): void {
        this.tagService.getAll().subscribe({
            next: (tags) => {
                this.availableTags = tags;
                console.log('[DEBUG_LOG] Loaded tags for filtering:', tags.length);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading tags:', err);
            }
        });
    }

    loadQuestionStats(): void {
        this.isLoadingStats = true;
        this.questionService.getQuestionStats().subscribe({
            next: (stats) => {
                this.questionStats = stats;
                this.isLoadingStats = false;
                console.log('[DEBUG_LOG] Loaded question statistics:', stats);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading question statistics:', err);
                this.isLoadingStats = false;
            }
        });
    }

    loadQuestions(): void {
        // Use granular loading states - separate initial load from table-only updates
        const isInitialLoad = !this.questionPage; // First time loading

        if (isInitialLoad) {
            this.isLoadingList = true; // Full page loading for initial load
        } else {
            this.isLoadingTable = true; // Only table loading for filtering/pagination
        }

        this.error = '';

        // Build search parameters
        this.searchParams = {
            query: this.searchTerm.trim() || undefined,
            difficulty: this.selectedDifficulty || undefined,
            tagId: this.selectedTagId || undefined
        };

        console.log('[DEBUG_LOG] Searching with parameters:', this.searchParams);

        this.questionService.searchWithPagination(this.searchParams, this.currentPageRequest).subscribe({
            next: (page) => {
                console.log('[DEBUG_LOG] Loaded questions page:', page);
                this.questionPage = page;

                // Reset appropriate loading states
                if (isInitialLoad) {
                    this.isLoadingList = false;
                } else {
                    this.isLoadingTable = false;
                }
                this.isLoadingSearch = false; // Reset search loading state as well

                console.log('[DEBUG_LOG] Total questions in page:', page.content.length);
                console.log('[DEBUG_LOG] Total questions overall:', page.totalElements);
                console.log('[DEBUG_LOG] Current page:', page.number + 1);
                console.log('[DEBUG_LOG] Total pages:', page.totalPages);
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading questions:', err);
                this.error = 'Failed to load questions. ' + (err.error?.message || err.message || 'Unknown error');

                // Reset appropriate loading states
                if (isInitialLoad) {
                    this.isLoadingList = false;
                } else {
                    this.isLoadingTable = false;
                }
                this.isLoadingSearch = false; // Reset search loading state as well
            }
        });
    }

    onSearch(): void {
        // Trigger debounced search instead of immediate filtering
        this.searchSubject.next(this.searchTerm);
    }

    performSearch(): void {
        // This method is called after debounce delay
        console.log('[DEBUG_LOG] Performing debounced search');
        this.isLoadingSearch = true;
        this.applyFilters();
    }

    onDifficultyFilter(): void {
        this.applyFilters();
    }

    onTagFilter(): void {
        this.applyFilters();
    }

    applyFilters(): void {
        this.currentPageRequest.page = 0; // Reset to first page when filtering
        this.loadQuestions();
    }

    clearSearch(): void {
        this.searchTerm = '';
        this.applyFilters();
    }

    clearDifficultyFilter(): void {
        this.selectedDifficulty = '';
        this.applyFilters();
    }

    clearTagFilter(): void {
        this.selectedTagId = '';
        this.applyFilters();
    }

    clearAllFilters(): void {
        this.searchTerm = '';
        this.selectedDifficulty = '';
        this.selectedTagId = '';
        this.applyFilters();
    }

    toggleAdvancedSearch(): void {
        this.showAdvancedSearch = !this.showAdvancedSearch;
    }

    viewDetail(id: string): void {
        this.router.navigate(['/interview-questions', id]);
    }

    editQuestion(id: string): void {
        this.router.navigate(['/interview-questions', id, 'edit']);
    }

    createNew(): void {
        this.router.navigate(['/interview-questions/create']);
    }

    /**
     * Navigate to the statistics dashboard
     */
    viewStatistics(): void {
        this.router.navigate(['/interview-questions/stats']);
    }

    /**
     * Navigate to the question generation form
     */
    generateQuestions(): void {
        this.router.navigate(['/interview-questions/generate']);
    }

    deleteQuestion(id: string, event: Event): void {
        event.stopPropagation();

        const question = this.questionPage?.content.find(q => q.id === id);
        const questionText = question?.questionText?.substring(0, 50) + '...' || 'this question';

        if (confirm(`Are you sure you want to delete "${questionText}"? This action cannot be undone.`)) {
            this.questionService.delete(id).subscribe({
                next: () => {
                    this.successMessage = `Question has been successfully deleted.`;
                    this.loadQuestions();
                    this.loadQuestionStats(); // Refresh stats after deletion
                    // Clear success message after 5 seconds
                    setTimeout(() => this.successMessage = '', 5000);
                },
                error: (err) => {
                    this.error = 'Failed to delete question. ' + err.message;
                }
            });
        }
    }

    // Pagination event handlers
    onPageChange(page: number): void {
        this.currentPageRequest.page = page;
        this.loadQuestions();
    }

    onPageSizeChange(size: number): void {
        this.currentPageRequest.size = size;
        this.currentPageRequest.page = 0; // Reset to first page when changing size
        this.loadQuestions();
    }

    // Getter for questions (replaces old filteredQuestions)
    get questions(): InterviewQuestionDTO[] {
        return this.questionPage?.content || [];
    }

    // Check if any filters are active
    get hasActiveFilters(): boolean {
        return !!(this.searchTerm.trim() || this.selectedDifficulty || this.selectedTagId);
    }

    // Get selected tag name for display
    get selectedTagName(): string {
        if (!this.selectedTagId) return '';
        const tag = this.availableTags.find(t => t.id === this.selectedTagId);
        return tag?.name || '';
    }

    // Utility methods for the template
    getDifficultyBadgeClass(difficulty: string): string {
        switch (difficulty?.toUpperCase()) {
            case 'EASY':
                return 'bg-success';
            case 'MEDIUM':
                return 'bg-warning text-dark';
            case 'HARD':
                return 'bg-danger';
            default:
                return 'bg-secondary';
        }
    }

    truncateText(text: string, maxLength: number = 100): string {
        if (!text) return '';
        return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    }

    trackByQuestionId(index: number, question: InterviewQuestionDTO): string {
        return question.id || index.toString();
    }
}
