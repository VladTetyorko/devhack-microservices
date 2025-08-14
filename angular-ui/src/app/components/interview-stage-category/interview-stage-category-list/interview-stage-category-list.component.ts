import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {InterviewStageCategoryService} from '../../../services/global/interview-stage-category.service';
import {InterviewStageCategoryDTO} from '../../../models/global/interview-stage-category.model';
import {Page, PageRequest} from '../../../models/basic/page.model';
import {PAGINATION_DEFAULTS, SKELETON_CONFIG, ViewMode} from '../../../shared/constants/constraints';

@Component({
  selector: 'app-interview-stage-category-list',
  templateUrl: './interview-stage-category-list.component.html',
  styleUrls: ['./interview-stage-category-list.component.css']
})
export class InterviewStageCategoryListComponent implements OnInit {
  categoryPage: Page<InterviewStageCategoryDTO> | null = null;
  allCategories: InterviewStageCategoryDTO[] = []; // Keep for filtering
  isLoading = true;
  error = '';
  successMessage = '';

  // Search and filter properties
  searchTerm = '';
  viewMode = ViewMode.TABLE;

  // Pagination properties
  currentPageRequest: PageRequest = {
    page: PAGINATION_DEFAULTS.PAGE,
    size: PAGINATION_DEFAULTS.SIZE,
    sort: ['label,asc'] // Specific sort for categories by label
  };

  // Skeleton loading
  skeletonItems = SKELETON_CONFIG.ITEMS;

  constructor(
    private categoryService: InterviewStageCategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.error = '';
    this.categoryService.getAllPaged(this.currentPageRequest).subscribe({
      next: (page) => {
        console.log('[DEBUG_LOG] Loaded categories page:', page);
        this.categoryPage = page;
        this.allCategories = page.content || [];
        this.isLoading = false;

        console.log('[DEBUG_LOG] Total categories in page:', page.content.length);
        console.log('[DEBUG_LOG] Total categories overall:', page.totalElements);
        console.log('[DEBUG_LOG] Current page:', page.number + 1);
        console.log('[DEBUG_LOG] Total pages:', page.totalPages);
      },
      error: (err) => {
        console.error('[DEBUG_LOG] Error loading categories:', err);
        this.error = 'Failed to load interview stage categories. ' + (err.error?.message || err.message || 'Unknown error');
        this.isLoading = false;
      }
    });
  }

  onSearch(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    // For now, we'll reload the data when filters change
    // In a more advanced implementation, we could send filter parameters to the backend
    this.currentPageRequest.page = 0; // Reset to first page when filtering
    this.loadCategories();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

    viewDetail(id: string | number): void {
    this.router.navigate(['/interview-stage-categories', id]);
  }

    editCategory(id: string | number): void {
    this.router.navigate(['/interview-stage-categories', id, 'edit']);
  }

  createNew(): void {
    this.router.navigate(['/interview-stage-categories/create']);
  }

    deleteCategory(deleteEvent: { id: string | number, event: MouseEvent }): void {
        deleteEvent.event.stopPropagation();

        const category = this.allCategories.find(c => c.id === deleteEvent.id);
    const categoryName = category?.label || 'this category';

    if (confirm(`Are you sure you want to delete ${categoryName}? This action cannot be undone.`)) {
        this.categoryService.delete(deleteEvent.id.toString()).subscribe({
        next: () => {
          this.successMessage = `Interview stage category ${categoryName} has been successfully deleted.`;
          this.loadCategories();
          // Clear success message after 5 seconds
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          this.error = 'Failed to delete interview stage category. ' + err.message;
        }
      });
    }
  }

  // Pagination event handlers
  onPageChange(page: number): void {
    this.currentPageRequest.page = page;
    this.loadCategories();
  }

  onPageSizeChange(size: number): void {
    this.currentPageRequest.size = size;
    this.currentPageRequest.page = 0; // Reset to first page when changing size
    this.loadCategories();
  }

  // Getter for filtered categories (for template compatibility)
  get filteredCategories(): InterviewStageCategoryDTO[] {
    if (!this.categoryPage) return [];

    let filtered = [...this.categoryPage.content];

    // Apply search filter
    if (this.searchTerm.trim()) {
      const searchLower = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(category =>
        category.label?.toLowerCase().includes(searchLower) ||
        category.code?.toLowerCase().includes(searchLower) ||
        category.description?.toLowerCase().includes(searchLower)
      );
    }

    return filtered;
  }

  // Utility methods for the template
  trackByCategoryId(index: number, category: InterviewStageCategoryDTO): string {
    return category.id || index.toString();
  }
}
