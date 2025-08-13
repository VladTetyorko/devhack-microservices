import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TagService} from '../../../services/global/tag.service';
import {TagDTO} from '../../../models/global/tag.model';

@Component({
  selector: 'app-tag-list',
  templateUrl: './tag-list.component.html',
  styleUrls: ['./tag-list.component.css']
})
export class TagListComponent implements OnInit {
  topics: TagDTO[] = [];
  isLoading = false;
  error = '';
  successMessage = '';
  searchTerm = '';
  selectedCategoryId: string | null = null;
  selectedCategoryName: string = '';

  constructor(
    private tagService: TagService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Check if we're filtering by category
    this.route.queryParams.subscribe(params => {
      this.selectedCategoryId = params['category'] || null;
      this.loadTopics();
    });
  }

  /**
   * Load topics (child tags) from the server
   */
  loadTopics(): void {
    this.isLoading = true;
    this.error = '';

    if (this.selectedCategoryId) {
      // Load topics for specific category
      this.tagService.getChildren(this.selectedCategoryId).subscribe({
        next: (topics) => {
          this.topics = topics;
          this.isLoading = false;
          // Load category name for display
          this.loadCategoryName();
        },
        error: (err) => {
          this.error = 'Failed to load topics. ' + err.message;
          this.isLoading = false;
        }
      });
    } else {
      // Load all child tags (topics) at depth 1 - more efficient than filtering all tags
      this.tagService.getTagsByDepth(2).subscribe({
        next: (topics) => {
          this.topics = topics;
          this.isLoading = false;
        },
        error: (err) => {
          this.error = 'Failed to load topics. ' + err.message;
          this.isLoading = false;
        }
      });
    }
  }

  /**
   * Load category name for display
   */
  loadCategoryName(): void {
    if (this.selectedCategoryId) {
      this.tagService.getById(this.selectedCategoryId).subscribe({
        next: (category) => {
          this.selectedCategoryName = category.name;
        },
        error: (err) => {
          console.error('Failed to load category name:', err);
        }
      });
    }
  }

  /**
   * Get filtered topics based on search term
   */
  get filteredTopics(): TagDTO[] {
    if (!this.searchTerm) {
      return this.topics;
    }
    return this.topics.filter(topic =>
        topic.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (topic.description && topic.description.toLowerCase().includes(this.searchTerm.toLowerCase()))
    );
  }

  /**
   * Clear search term
   */
  clearSearch(): void {
    this.searchTerm = '';
  }

  /**
   * Clear category filter
   */
  clearCategoryFilter(): void {
    this.router.navigate(['/topics']);
  }

  viewDetail(id: string): void {
    this.router.navigate(['/topics', id]);
  }

  editTopic(id: string): void {
    this.router.navigate(['/topics', id, 'edit']);
  }

  createNew(): void {
    const queryParams = this.selectedCategoryId ? {parent: this.selectedCategoryId} : {};
    this.router.navigate(['/topics/create'], {queryParams});
  }

  deleteTopic(id: string, event: Event): void {
    event.stopPropagation();

    if (confirm(`Are you sure you want to delete this topic? This action cannot be undone.`)) {
      this.tagService.delete(id).subscribe({
        next: () => {
          this.successMessage = `Topic has been successfully deleted.`;
          this.loadTopics(); // Reload the list
          // Clear success message after 5 seconds
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          this.error = 'Failed to delete topic. ' + err.message;
        }
      });
    }
  }

  /**
   * Navigate to category detail
   */
  viewCategory(categoryId: string): void {
    this.router.navigate(['/categories', categoryId]);
  }
}
