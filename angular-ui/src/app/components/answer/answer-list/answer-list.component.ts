import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AnswerService} from '../../../services/personalized/answer.service';
import {AnswerDTO} from '../../../models/personalized/answer.model';
import {Page, PageRequest} from '../../../models/basic/page.model';

@Component({
  selector: 'app-answer-list',
  templateUrl: './answer-list.component.html',
  styleUrls: ['./answer-list.component.css']
})
export class AnswerListComponent implements OnInit {
  answerPage: Page<AnswerDTO> | null = null;
  allAnswers: AnswerDTO[] = []; // Keep for filtering
  isLoading = true;
  error = '';
  successMessage = '';

  // Search and filter properties
  searchTerm = '';
  viewMode = 'table'; // 'table' or 'cards'

  // Pagination properties
  currentPageRequest: PageRequest = {
    page: 0,
    size: 10,
    sort: ['createdAt,desc']
  };

  // Skeleton loading
  skeletonItems = Array(6).fill(0); // Show 6 skeleton items while loading

  constructor(
    private answerService: AnswerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAnswers();
  }

  loadAnswers(): void {
    this.isLoading = true;
    this.error = '';
    this.answerService.getAllPaged(this.currentPageRequest).subscribe({
      next: (page) => {
        console.log('[DEBUG_LOG] Loaded answers page:', page);
          // Transform date formats in the page content
          const transformedContent = (page.content || []).map(answer => ({
              ...answer,
              createdAt: this.convertDateFormat(answer.createdAt),
              updatedAt: this.convertDateFormat(answer.updatedAt)
          }));

          this.answerPage = {
              ...page,
              content: transformedContent
          };
          this.allAnswers = transformedContent;
        this.isLoading = false;

        console.log('[DEBUG_LOG] Total answers in page:', page.content.length);
        console.log('[DEBUG_LOG] Total answers overall:', page.totalElements);
        console.log('[DEBUG_LOG] Current page:', page.number + 1);
        console.log('[DEBUG_LOG] Total pages:', page.totalPages);
      },
      error: (err) => {
        console.error('[DEBUG_LOG] Error loading answers:', err);
        this.error = 'Failed to load answers. ' + (err.error?.message || err.message || 'Unknown error');
        this.isLoading = false;
      }
    });
  }

    private convertDateFormat(dateString?: string): string | undefined {
        if (!dateString) return dateString;

        // Check if the date is in comma-separated format (e.g., "2025,7,7,15,36,51,426621000")
        if (dateString.includes(',')) {
            try {
                const parts = dateString.split(',').map(part => parseInt(part, 10));
                if (parts.length >= 6) {
                    // parts: [year, month, day, hour, minute, second, nanoseconds]
                    // Note: month is 1-based in the input, but Date constructor expects 0-based
                    const date = new Date(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
                    return date.toISOString();
                }
            } catch (error) {
                console.warn('Failed to parse date:', dateString, error);
            }
        }

        return dateString;
    }

    onSearch(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    // For now, we'll reload the data when filters change
    // In a more advanced implementation, we could send filter parameters to the backend
    this.currentPageRequest.page = 0; // Reset to first page when filtering
    this.loadAnswers();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

    viewDetail(id: string | number): void {
    this.router.navigate(['/answers', id]);
  }

    editAnswer(id: string | number): void {
    this.router.navigate(['/answers', id, 'edit']);
  }

  createNew(): void {
    this.router.navigate(['/answers/create']);
  }

    deleteAnswer(deleteEvent: { id: string | number, event: MouseEvent }): void {
        deleteEvent.event.stopPropagation();

        const answer = this.allAnswers.find(a => a.id === deleteEvent.id);
    const answerText = answer?.text?.substring(0, 50) + '...' || 'this answer';

    if (confirm(`Are you sure you want to delete "${answerText}"? This action cannot be undone.`)) {
        this.answerService.delete(deleteEvent.id.toString()).subscribe({
        next: () => {
          this.successMessage = `Answer has been successfully deleted.`;
          this.loadAnswers();
          // Clear success message after 5 seconds
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          this.error = 'Failed to delete answer. ' + err.message;
        }
      });
    }
  }

  // Pagination event handlers
  onPageChange(page: number): void {
    this.currentPageRequest.page = page;
    this.loadAnswers();
  }

  onPageSizeChange(size: number): void {
    this.currentPageRequest.size = size;
    this.currentPageRequest.page = 0; // Reset to first page when changing size
    this.loadAnswers();
  }

  // Getter for filtered answers (for template compatibility)
  get filteredAnswers(): AnswerDTO[] {
    if (!this.answerPage) return [];

    let filtered = [...this.answerPage.content];

    // Apply search filter
    if (this.searchTerm.trim()) {
      const searchLower = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(answer =>
        answer.text?.toLowerCase().includes(searchLower) ||
        answer.questionText?.toLowerCase().includes(searchLower)
      );
    }

    return filtered;
  }

  // Utility methods for the template
  getScoreBadgeClass(score?: number): string {
    if (!score) return 'bg-secondary';
    if (score >= 80) return 'bg-success';
    if (score >= 60) return 'bg-warning text-dark';
    return 'bg-danger';
  }

  getConfidenceBadgeClass(confidence?: number): string {
    if (!confidence) return 'bg-secondary';
    if (confidence >= 80) return 'bg-success';
    if (confidence >= 60) return 'bg-info';
    return 'bg-warning text-dark';
  }

  truncateText(text: string, maxLength: number = 100): string {
    if (!text) return '';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  }

  trackByAnswerId(index: number, answer: AnswerDTO): string {
    return answer.id || index.toString();
  }
}
