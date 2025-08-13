import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TagService} from '../../../services/global/tag.service';
import {TagDTO} from '../../../models/global/tag.model';

/**
 * Component for displaying detailed information about a category (root tag).
 * Shows category details, associated topics, and statistics.
 */
@Component({
    selector: 'app-category-detail',
    templateUrl: './category-detail.component.html',
    styleUrls: ['./category-detail.component.css']
})
export class CategoryDetailComponent implements OnInit {
    category: TagDTO | null = null;
    topics: TagDTO[] = [];
    isLoading = true;
    isLoadingTopics = false;
    error = '';
    categoryId: string | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private tagService: TagService
    ) {
    }

    ngOnInit(): void {
        this.categoryId = this.route.snapshot.paramMap.get('id');
        if (this.categoryId) {
            this.loadCategory();
            this.loadTopics();
        } else {
            this.error = 'Category ID not provided';
            this.isLoading = false;
        }
    }

    /**
     * Load category details from the API
     */
    loadCategory(): void {
        if (!this.categoryId) return;

        this.isLoading = true;
        this.error = '';

        this.tagService.getById(this.categoryId).subscribe({
            next: (category) => {
                console.log('[DEBUG_LOG] Loaded category details:', category);
                this.category = category;
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading category details:', err);
                this.error = 'Failed to load category details. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    /**
     * Load topics (child tags) for this category
     */
    loadTopics(): void {
        if (!this.categoryId) return;

        this.isLoadingTopics = true;

        this.tagService.getChildren(this.categoryId).subscribe({
            next: (topics) => {
                console.log('[DEBUG_LOG] Loaded topics for category:', topics);
                this.topics = topics;
                this.isLoadingTopics = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading topics:', err);
                // Don't set main error for topics loading failure
                this.isLoadingTopics = false;
            }
        });
    }

    /**
     * Navigate to edit page
     */
    editCategory(): void {
        if (this.categoryId) {
            this.router.navigate(['/categories', this.categoryId, 'edit']);
        }
    }

    /**
     * Delete the category with confirmation
     */
    deleteCategory(): void {
        if (!this.category || !this.categoryId) return;

        const categoryName = this.category.name || 'this category';
        const hasTopics = this.topics.length > 0;

        let confirmMessage = `Are you sure you want to delete ${categoryName}?`;
        if (hasTopics) {
            confirmMessage += ` This will also delete all ${this.topics.length} topics within this category.`;
        }
        confirmMessage += ' This action cannot be undone.';

        if (confirm(confirmMessage)) {
            this.tagService.deleteWithCascade(this.categoryId, true).subscribe({
                next: () => {
                    console.log('[DEBUG_LOG] Category deleted successfully');
                    this.router.navigate(['/categories'], {
                        queryParams: {message: `Category ${categoryName} has been successfully deleted.`}
                    });
                },
                error: (err) => {
                    console.error('[DEBUG_LOG] Error deleting category:', err);
                    this.error = 'Failed to delete category. ' + (err.error?.message || err.message || 'Unknown error');
                }
            });
        }
    }

    /**
     * Navigate back to categories list
     */
    goBack(): void {
        this.router.navigate(['/categories']);
    }

    /**
     * Navigate to topics page filtered by this category
     */
    viewTopics(): void {
        this.router.navigate(['/topics'], {queryParams: {category: this.categoryId}});
    }

    /**
     * Navigate to create new topic in this category
     */
    createTopic(): void {
        this.router.navigate(['/topics/create'], {queryParams: {parent: this.categoryId}});
    }

    /**
     * Navigate to specific topic detail
     */
    viewTopic(topicId: string): void {
        this.router.navigate(['/topics', topicId]);
    }

    /**
     * Get progress bar CSS class based on percentage
     */
    getProgressBarClass(percentage: number): string {
        if (percentage >= 80) return 'bg-success';
        if (percentage >= 60) return 'bg-warning';
        if (percentage >= 40) return 'bg-info';
        return 'bg-danger';
    }

    /**
     * Get progress status text
     */
    getProgressStatus(percentage: number): string {
        if (percentage >= 80) return 'Excellent';
        if (percentage >= 60) return 'Good';
        if (percentage >= 40) return 'Fair';
        return 'Needs Improvement';
    }

    /**
     * Get category statistics
     */
    getCategoryStats() {
        if (!this.category) return null;

        return {
            totalQuestions: this.category.questionCount || 0,
            answeredQuestions: this.category.answeredQuestions || 0,
            progressPercentage: this.category.progressPercentage || 0,
            topicsCount: this.topics.length
        };
    }
}