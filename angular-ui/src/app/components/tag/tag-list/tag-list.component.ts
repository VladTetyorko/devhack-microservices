import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TagService } from '../../../services/tag.service';
import { TagDTO } from '../../../models/global/tag.model';
import { Page, PageRequest } from '../../../models/basic/page.model';

@Component({
  selector: 'app-tag-list',
  templateUrl: './tag-list.component.html',
  styleUrls: ['./tag-list.component.css']
})
export class TagListComponent implements OnInit {
  tagPage: Page<TagDTO> | null = null;
  allTags: TagDTO[] = []; // Keep for filtering
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
    sort: ['name,asc']
  };

  // Skeleton loading
  skeletonItems = Array(6).fill(0); // Show 6 skeleton items while loading

  constructor(
    private tagService: TagService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTags();
  }

  loadTags(): void {
    this.isLoading = true;
    this.error = '';
    this.tagService.getAllPaged(this.currentPageRequest).subscribe({
      next: (page) => {
        console.log('[DEBUG_LOG] Loaded tags page:', page);
        this.tagPage = page;
        this.allTags = page.content || [];
        this.isLoading = false;

        console.log('[DEBUG_LOG] Total tags in page:', page.content.length);
        console.log('[DEBUG_LOG] Total tags overall:', page.totalElements);
        console.log('[DEBUG_LOG] Current page:', page.number + 1);
        console.log('[DEBUG_LOG] Total pages:', page.totalPages);
      },
      error: (err) => {
        console.error('[DEBUG_LOG] Error loading tags:', err);
        this.error = 'Failed to load tags. ' + (err.error?.message || err.message || 'Unknown error');
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
    this.loadTags();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

  viewDetail(id: string): void {
    this.router.navigate(['/tags', id]);
  }

  editTag(id: string): void {
    this.router.navigate(['/tags', id, 'edit']);
  }

  createNew(): void {
    this.router.navigate(['/tags/create']);
  }

  deleteTag(id: string, event: Event): void {
    event.stopPropagation();

    const tag = this.allTags.find(t => t.id === id);
    const tagName = tag?.name || 'this tag';

    if (confirm(`Are you sure you want to delete ${tagName}? This action cannot be undone.`)) {
      this.tagService.delete(id).subscribe({
        next: () => {
          this.successMessage = `Tag ${tagName} has been successfully deleted.`;
          this.loadTags();
          // Clear success message after 5 seconds
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          this.error = 'Failed to delete tag. ' + err.message;
        }
      });
    }
  }

  // Pagination event handlers
  onPageChange(page: number): void {
    this.currentPageRequest.page = page;
    this.loadTags();
  }

  onPageSizeChange(size: number): void {
    this.currentPageRequest.size = size;
    this.currentPageRequest.page = 0; // Reset to first page when changing size
    this.loadTags();
  }

  // Getter for filtered tags (for template compatibility)
  get filteredTags(): TagDTO[] {
    if (!this.tagPage) return [];

    let filtered = [...this.tagPage.content];

    // Apply search filter
    if (this.searchTerm.trim()) {
      const searchLower = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(tag =>
        tag.name?.toLowerCase().includes(searchLower)
      );
    }

    return filtered;
  }

  // Utility methods for the template
  getProgressBarClass(percentage: number): string {
    if (percentage >= 80) return 'bg-success';
    if (percentage >= 60) return 'bg-warning';
    if (percentage >= 40) return 'bg-info';
    return 'bg-danger';
  }

  trackByTagId(index: number, tag: TagDTO): string {
    return tag.id || index.toString();
  }
}
