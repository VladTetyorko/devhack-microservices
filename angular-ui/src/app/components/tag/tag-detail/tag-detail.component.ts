import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TagService} from '../../../services/global/tag.service';
import {TagDTO} from '../../../models/global/tag.model';

/**
 * Component for displaying detailed information about a topic (child tag).
 * Shows topic details, associated questions, and progress statistics.
 */
@Component({
    selector: 'app-tag-detail',
    templateUrl: './tag-detail.component.html',
    styleUrls: ['./tag-detail.component.css']
})
export class TagDetailComponent implements OnInit {
    topic: TagDTO | null = null;
    isLoading = true;
    error = '';
    topicId: string | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private tagService: TagService
    ) {
    }

    ngOnInit(): void {
        this.topicId = this.route.snapshot.paramMap.get('id');
        if (this.topicId) {
            this.loadTopic();
        } else {
            this.error = 'Topic ID not provided';
            this.isLoading = false;
        }
    }

    /**
     * Load topic details from the API
     */
    loadTopic(): void {
        if (!this.topicId) return;

        this.isLoading = true;
        this.error = '';

        this.tagService.getById(this.topicId).subscribe({
            next: (topic) => {
                console.log('[DEBUG_LOG] Loaded topic details:', topic);
                this.topic = topic;
                this.isLoading = false;
            },
            error: (err) => {
                console.error('[DEBUG_LOG] Error loading topic details:', err);
                this.error = 'Failed to load topic details. ' + (err.error?.message || err.message || 'Unknown error');
                this.isLoading = false;
            }
        });
    }

    /**
     * Navigate to edit page
     */
    editTopic(): void {
        if (this.topicId) {
            this.router.navigate(['/topics', this.topicId, 'edit']);
        }
    }

    /**
     * Delete the topic with confirmation
     */
    deleteTopic(): void {
        if (!this.topic || !this.topicId) return;

        const topicName = this.topic.name || 'this topic';
        if (confirm(`Are you sure you want to delete ${topicName}? This action cannot be undone.`)) {
            this.tagService.delete(this.topicId).subscribe({
                next: () => {
                    console.log('[DEBUG_LOG] Topic deleted successfully');
                    this.router.navigate(['/topics'], {
                        queryParams: {message: `Topic ${topicName} has been successfully deleted.`}
                    });
                },
                error: (err) => {
                    console.error('[DEBUG_LOG] Error deleting topic:', err);
                    this.error = 'Failed to delete topic. ' + (err.error?.message || err.message || 'Unknown error');
                }
            });
        }
    }

    /**
     * Navigate back to topics list
     */
    goBack(): void {
        this.router.navigate(['/topics']);
    }

    /**
     * Navigate to category detail
     */
    viewCategory(): void {
        if (this.topic?.parent?.id) {
            this.router.navigate(['/categories', this.topic.parent.id]);
        }
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
}