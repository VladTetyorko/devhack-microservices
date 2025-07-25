import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { InterviewQuestionService } from '../../../services/interview-question.service';
import { InterviewQuestionDTO, TagDTO } from '../../../models/global/interview-question.model';
import { Page, PageRequest } from '../../../models/basic/page.model';

@Component({
  selector: 'app-interview-question-list',
  templateUrl: './interview-question-list.component.html',
  styleUrls: ['./interview-question-list.component.css']
})
export class InterviewQuestionListComponent implements OnInit {
  questionPage: Page<InterviewQuestionDTO> | null = null;
  allQuestions: InterviewQuestionDTO[] = []; // Keep for filtering
  isLoading = true;
  error = '';
  successMessage = '';

  // Search and filter properties
  searchTerm = '';
  selectedDifficulty = '';
  selectedTag = '';
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
    private questionService: InterviewQuestionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadQuestions();
  }

  loadQuestions(): void {
    this.isLoading = true;
    this.error = '';
    this.questionService.getAllPaged(this.currentPageRequest).subscribe({
      next: (page) => {
        console.log('[DEBUG_LOG] Loaded questions page:', page);
        this.questionPage = page;
        this.allQuestions = page.content || [];
        this.isLoading = false;

        console.log('[DEBUG_LOG] Total questions in page:', page.content.length);
        console.log('[DEBUG_LOG] Total questions overall:', page.totalElements);
        console.log('[DEBUG_LOG] Current page:', page.number + 1);
        console.log('[DEBUG_LOG] Total pages:', page.totalPages);
      },
      error: (err) => {
        console.error('[DEBUG_LOG] Error loading questions:', err);
        this.error = 'Failed to load questions. ' + (err.error?.message || err.message || 'Unknown error');
        this.isLoading = false;
      }
    });
  }

  onSearch(): void {
    this.applyFilters();
  }

  onDifficultyFilter(): void {
    this.applyFilters();
  }

  onTagFilter(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    // For now, we'll reload the data when filters change
    // In a more advanced implementation, we could send filter parameters to the backend
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
    this.selectedTag = '';
    this.applyFilters();
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

  deleteQuestion(id: string, event: Event): void {
    event.stopPropagation();

    const question = this.allQuestions.find(q => q.id === id);
    const questionText = question?.questionText?.substring(0, 50) + '...' || 'this question';

    if (confirm(`Are you sure you want to delete "${questionText}"? This action cannot be undone.`)) {
      this.questionService.delete(id).subscribe({
        next: () => {
          this.successMessage = `Question has been successfully deleted.`;
          this.loadQuestions();
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

  // Getter for filtered questions (for template compatibility)
  get filteredQuestions(): InterviewQuestionDTO[] {
    if (!this.questionPage) return [];

    let filtered = [...this.questionPage.content];

    // Apply search filter
    if (this.searchTerm.trim()) {
      const searchLower = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(question =>
        question.questionText?.toLowerCase().includes(searchLower) ||
        question.source?.toLowerCase().includes(searchLower)
      );
    }

    // Apply difficulty filter
    if (this.selectedDifficulty) {
      filtered = filtered.filter(question => question.difficulty === this.selectedDifficulty);
    }

    // Apply tag filter
    if (this.selectedTag) {
      filtered = filtered.filter(question => 
        question.tags?.some((tag: TagDTO) => tag.name?.toLowerCase().includes(this.selectedTag.toLowerCase()))
      );
    }

    return filtered;
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
