import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {InterviewStageService} from '../../../services/global/interview-stage.service';
import {InterviewStageDTO} from '../../../models/global/interview-stage.model';
import {Page, PageRequest} from '../../../models/basic/page.model';

@Component({
  selector: 'app-interview-stage-list',
  templateUrl: './interview-stage-list.component.html',
  styleUrls: ['./interview-stage-list.component.css']
})
export class InterviewStageListComponent implements OnInit {
  stagePage: Page<InterviewStageDTO> | null = null;
  allStages: InterviewStageDTO[] = []; // Keep for filtering
  isLoading = true;
  error = '';
  successMessage = '';

  // Search and filter properties
  searchTerm = '';
  selectedCategory = '';
  selectedActive = '';
  viewMode = 'table'; // 'table' or 'cards'

  // Pagination properties
  currentPageRequest: PageRequest = {
    page: 0,
    size: 10,
    sort: ['orderIndex,asc']
  };

  // Skeleton loading
  skeletonItems = Array(6).fill(0); // Show 6 skeleton items while loading

  constructor(
    private stageService: InterviewStageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStages();
  }

  loadStages(): void {
    this.isLoading = true;
    this.error = '';
    this.stageService.getAllPaged(this.currentPageRequest).subscribe({
      next: (page) => {
        console.log('[DEBUG_LOG] Loaded stages page:', page);
        this.stagePage = page;
        this.allStages = page.content || [];
        this.isLoading = false;

        console.log('[DEBUG_LOG] Total stages in page:', page.content.length);
        console.log('[DEBUG_LOG] Total stages overall:', page.totalElements);
        console.log('[DEBUG_LOG] Current page:', page.number + 1);
        console.log('[DEBUG_LOG] Total pages:', page.totalPages);
      },
      error: (err) => {
        console.error('[DEBUG_LOG] Error loading stages:', err);
        this.error = 'Failed to load interview stages. ' + (err.error?.message || err.message || 'Unknown error');
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
    this.loadStages();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

  clearCategoryFilter(): void {
    this.selectedCategory = '';
    this.applyFilters();
  }

  clearActiveFilter(): void {
    this.selectedActive = '';
    this.applyFilters();
  }

    viewDetail(id: string | number): void {
    this.router.navigate(['/interview-stages', id]);
  }

    editStage(id: string | number): void {
    this.router.navigate(['/interview-stages', id, 'edit']);
  }

  createNew(): void {
    this.router.navigate(['/interview-stages/create']);
  }

    deleteStage(deleteEvent: { id: string | number, event: MouseEvent }): void {
        deleteEvent.event.stopPropagation();

        const stage = this.allStages.find(s => s.id === deleteEvent.id);
    const stageName = stage?.label || 'this stage';

    if (confirm(`Are you sure you want to delete ${stageName}? This action cannot be undone.`)) {
        this.stageService.delete(deleteEvent.id.toString()).subscribe({
        next: () => {
          this.successMessage = `Interview stage ${stageName} has been successfully deleted.`;
          this.loadStages();
          // Clear success message after 5 seconds
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          this.error = 'Failed to delete interview stage. ' + err.message;
        }
      });
    }
  }

  // Pagination event handlers
  onPageChange(page: number): void {
    this.currentPageRequest.page = page;
    this.loadStages();
  }

  onPageSizeChange(size: number): void {
    this.currentPageRequest.size = size;
    this.currentPageRequest.page = 0; // Reset to first page when changing size
    this.loadStages();
  }

  // Getter for filtered stages (for template compatibility)
  get filteredStages(): InterviewStageDTO[] {
    if (!this.stagePage) return [];

    let filtered = [...this.stagePage.content];

    // Apply search filter
    if (this.searchTerm.trim()) {
      const searchLower = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(stage =>
        stage.label?.toLowerCase().includes(searchLower) ||
        stage.code?.toLowerCase().includes(searchLower) ||
        stage.categoryLabel?.toLowerCase().includes(searchLower)
      );
    }

    // Apply category filter
    if (this.selectedCategory) {
      filtered = filtered.filter(stage => stage.categoryCode === this.selectedCategory);
    }

    // Apply active filter
    if (this.selectedActive !== '') {
      const isActive = this.selectedActive === 'true';
      filtered = filtered.filter(stage => stage.active === isActive);
    }

    return filtered;
  }

  // Utility methods for the template
  getStatusBadgeClass(active: boolean): string {
    return active ? 'bg-success' : 'bg-secondary';
  }

  getFinalStageBadgeClass(finalStage: boolean): string {
    return finalStage ? 'bg-warning text-dark' : 'bg-light text-dark';
  }

  trackByStageId(index: number, stage: InterviewStageDTO): string {
    return stage.id || index.toString();
  }
}
