import {Component, OnInit} from '@angular/core';
import {InterviewQuestionService, QuestionStats} from '../../../services/global/interview-question.service';
import {TagService} from '../../../services/global/tag.service';
import {TagDTO} from '../../../models/global/tag.model';

/**
 * Component for displaying question generation statistics.
 * Provides comprehensive insights into question generation metrics and user progress.
 */
@Component({
    selector: 'app-question-generation-stats',
    templateUrl: './question-generation-stats.component.html',
    styleUrls: ['./question-generation-stats.component.css']
})
export class QuestionGenerationStatsComponent implements OnInit {
    stats: QuestionStats | null = null;
    tags: TagDTO[] = [];
    loading = false;
    error: string | null = null;

    // Chart data for statistics visualization
    chartData: any = null;
    chartOptions: any = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
            },
            title: {
                display: true,
                text: 'Question Statistics Overview'
            }
        }
    };

    constructor(
        private questionService: InterviewQuestionService,
        private tagService: TagService
    ) {
    }

    ngOnInit(): void {
        this.loadStats();
        this.loadTags();
    }

    /**
     * Load question statistics from the API
     */
    loadStats(): void {
        this.loading = true;
        this.error = null;

        this.questionService.getQuestionStats().subscribe({
            next: (stats) => {
                this.stats = stats;
                this.prepareChartData();
                this.loading = false;
            },
            error: (error) => {
                console.error('Error loading question stats:', error);
                this.error = 'Failed to load question statistics. Please try again.';
                this.loading = false;
            }
        });
    }

    /**
     * Load available tags for generation insights
     */
    loadTags(): void {
        this.tagService.getAll().subscribe({
            next: (tags: TagDTO[]) => {
                this.tags = tags;
            },
            error: (error: any) => {
                console.error('Error loading tags:', error);
            }
        });
    }

    /**
     * Prepare chart data for visualization
     */
    prepareChartData(): void {
        if (!this.stats) return;

        this.chartData = {
            labels: ['Total Questions', 'Your Questions', 'Answered Questions'],
            datasets: [{
                label: 'Question Statistics',
                data: [
                    this.stats.totalQuestions,
                    this.stats.userQuestions,
                    this.stats.answeredQuestions
                ],
                backgroundColor: [
                    'rgba(54, 162, 235, 0.8)',
                    'rgba(255, 99, 132, 0.8)',
                    'rgba(75, 192, 192, 0.8)'
                ],
                borderColor: [
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 99, 132, 1)',
                    'rgba(75, 192, 192, 1)'
                ],
                borderWidth: 2
            }]
        };
    }

    /**
     * Calculate completion percentage
     */
    getCompletionPercentage(): number {
        if (!this.stats || this.stats.userQuestions === 0) return 0;
        return Math.round((this.stats.answeredQuestions / this.stats.userQuestions) * 100);
    }

    /**
     * Get progress bar class based on completion percentage
     */
    getProgressBarClass(): string {
        const percentage = this.getCompletionPercentage();
        if (percentage >= 80) return 'bg-success';
        if (percentage >= 50) return 'bg-warning';
        return 'bg-danger';
    }

    /**
     * Refresh statistics
     */
    refreshStats(): void {
        this.loadStats();
    }
}