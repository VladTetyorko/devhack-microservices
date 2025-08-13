import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {TagDTO, TagMoveRequest, TagTreeNode} from '../../../models/global/tag.model';
import {TagService} from '../../../services/global/tag.service';

/**
 * Component for displaying and managing tag hierarchy in a tree structure.
 * Supports expand/collapse, drag-and-drop, and context menu operations.
 */
@Component({
    selector: 'app-tag-hierarchy-tree',
    templateUrl: './tag-hierarchy-tree.component.html',
    styleUrls: ['./tag-hierarchy-tree.component.css']
})
export class TagHierarchyTreeComponent implements OnInit, OnChanges {
    @Input() tags: TagDTO[] = [];
    @Input() selectable: boolean = false;
    @Input() draggable: boolean = true;
    @Input() showContextMenuOption: boolean = true;
    @Input() progressiveMode: boolean = false; // New: Enable progressive disclosure mode
    @Output() tagSelected = new EventEmitter<TagDTO>();
    @Output() tagMoved = new EventEmitter<TagMoveRequest>();
    @Output() tagDeleted = new EventEmitter<TagDTO>();
    @Output() tagEdited = new EventEmitter<TagDTO>();
    @Output() tagClicked = new EventEmitter<TagDTO>(); // New: For progressive navigation

    treeNodes: TagTreeNode[] = [];
    flattenedNodes: TagTreeNode[] = [];
    selectedTag: TagDTO | null = null;
    draggedNode: TagTreeNode | null = null;
    contextMenuVisible = false;
    contextMenuX = 0;
    contextMenuY = 0;
    contextMenuTag: TagDTO | null = null;
    loading = false;
    successMessage = '';
    error = '';

    constructor(
        private tagService: TagService
    ) {
    }

    ngOnInit(): void {
        if (this.progressiveMode) {
            this.loadRootTags();
        } else {
            this.buildTreeStructure();
        }
    }

    ngOnChanges(): void {
        this.buildTreeStructure();
    }

    /**
     * Build tree structure from flat tag list
     */
    buildTreeStructure(): void {
        if (this.tags && this.tags.length > 0) {
            this.treeNodes = this.tagService.buildTree(this.tags);
            this.updateFlattenedNodes();
        }
    }

    /**
     * Update flattened nodes for display
     */
    updateFlattenedNodes(): void {
        this.flattenedNodes = this.tagService.flattenTree(this.treeNodes, false);
    }

    /**
     * Toggle node expansion
     */
    toggleNode(node: TagTreeNode): void {
        node.expanded = !node.expanded;
        this.updateFlattenedNodes();
    }

    /**
     * Load root tags for progressive mode
     */
    loadRootTags(): void {
        this.loading = true;
        this.error = '';

        this.tagService.getRootTags().subscribe({
            next: (rootTags) => {
                this.tags = rootTags;
                this.buildTreeStructure();
                this.loading = false;
            },
            error: (error) => {
                this.error = 'Failed to load root tags';
                this.loading = false;
            }
        });
    }

    /**
     * Load children for a specific tag
     */
    loadChildren(parentTag: TagDTO): void {
        if (!parentTag.id) return;

        this.loading = true;
        this.tagService.getChildren(parentTag.id).subscribe({
            next: (children) => {
                // Find the parent node and add children
                const parentNode = this.findNodeByTagId(parentTag.id!);
                if (parentNode) {
                    parentNode.children = children.map(child => ({
                        tag: child,
                        children: [],
                        expanded: false,
                        level: parentNode.level + 1
                    }));
                    parentNode.expanded = true;
                    this.updateFlattenedNodes();
                }
                this.loading = false;
            },
            error: (error) => {
                this.error = 'Failed to load children';
                this.loading = false;
            }
        });
    }

    /**
     * Find a tree node by tag ID
     */
    private findNodeByTagId(tagId: string): TagTreeNode | null {
        const findInNodes = (nodes: TagTreeNode[]): TagTreeNode | null => {
            for (const node of nodes) {
                if (node.tag.id === tagId) {
                    return node;
                }
                const found = findInNodes(node.children);
                if (found) return found;
            }
            return null;
        };
        return findInNodes(this.treeNodes);
    }

    /**
     * Select a tag (handles progressive navigation)
     */
    selectTag(tag: TagDTO): void {
        if (this.progressiveMode) {
            // In progressive mode, handle navigation
            if (tag.isLeaf) {
                // Navigate to tag detail for leaf nodes
                this.tagClicked.emit(tag);
            } else {
                // Load children for non-leaf nodes
                const node = this.findNodeByTagId(tag.id!);
                if (node && node.children.length === 0) {
                    // Load children if not already loaded
                    this.loadChildren(tag);
                } else if (node) {
                    // Toggle expansion if children already loaded
                    this.toggleNode(node);
                }
            }
        } else {
            // Standard selection mode
            if (this.selectable) {
                this.selectedTag = tag;
                this.tagSelected.emit(tag);
            }
        }
    }

    /**
     * Check if tag is selected
     */
    isSelected(tag: TagDTO): boolean {
        return this.selectedTag?.id === tag.id;
    }

    /**
     * Get indentation style for tree level
     */
    getIndentStyle(level: number): any {
        return {
            'margin-left': `${level * 20}px`
        };
    }

    /**
     * Get icon class for node
     */
    getNodeIcon(node: TagTreeNode): string {
        if (node.children.length === 0) {
            return 'bi-file-text';
        }
        return node.expanded ? 'bi-folder2-open' : 'bi-folder2';
    }

    /**
     * Handle drag start
     */
    onDragStart(event: DragEvent, node: TagTreeNode): void {
        if (!this.draggable || !node.tag.id) return;

        this.draggedNode = node;
        event.dataTransfer?.setData('text/plain', node.tag.id);
        event.dataTransfer!.effectAllowed = 'move';
    }

    /**
     * Handle drag over
     */
    onDragOver(event: DragEvent): void {
        if (!this.draggable || !this.draggedNode) return;

        event.preventDefault();
        event.dataTransfer!.dropEffect = 'move';
    }

    /**
     * Handle drop
     */
    onDrop(event: DragEvent, targetNode: TagTreeNode): void {
        if (!this.draggable || !this.draggedNode) return;

        event.preventDefault();

        const draggedTag = this.draggedNode.tag;
        const targetTag = targetNode.tag;

        // Check if both tags have valid IDs
        if (!draggedTag.id || !targetTag.id) {
            this.error = 'Invalid tag data for move operation';
            this.successMessage = '';
            return;
        }

        // Prevent dropping on itself or its descendants
        if (draggedTag.id === targetTag.id || this.isDescendant(draggedTag, targetTag)) {
            this.error = 'Cannot move tag to itself or its descendants';
            this.successMessage = '';
            return;
        }

        // Validate move
        this.validateAndMoveTag(draggedTag.id, targetTag.id);
    }

    /**
     * Check if tag is descendant of another tag
     */
    private isDescendant(ancestor: TagDTO, potential: TagDTO): boolean {
        if (!potential.parent) return false;
        if (potential.parent.id === ancestor.id) return true;
        return this.isDescendant(ancestor, potential.parent);
    }

    /**
     * Validate and move tag
     */
    private validateAndMoveTag(tagId: string, newParentId: string): void {
        this.loading = true;

        this.tagService.validateMove(tagId, newParentId).subscribe({
            next: (validation) => {
                if (validation.valid) {
                    this.moveTag(tagId, newParentId);
                } else {
                    this.error = validation.message || 'Invalid move operation';
                    this.successMessage = '';
                    this.loading = false;
                }
            },
            error: (error) => {
                this.error = 'Failed to validate move operation';
                this.successMessage = '';
                this.loading = false;
            }
        });
    }

    /**
     * Move tag to new parent
     */
    private moveTag(tagId: string, newParentId: string): void {
        const moveRequest: TagMoveRequest = {
            tagId,
            newParentId
        };

        this.tagService.moveTag(moveRequest).subscribe({
            next: (updatedTag) => {
                this.successMessage = 'Tag moved successfully';
                this.error = '';
                this.tagMoved.emit(moveRequest);
                this.loading = false;
            },
            error: (error) => {
                this.error = 'Failed to move tag';
                this.successMessage = '';
                this.loading = false;
            }
        });
    }

    /**
     * Show context menu
     */
    showContextMenu(event: MouseEvent, tag: TagDTO): void {
        if (!this.showContextMenuOption) return;

        event.preventDefault();
        event.stopPropagation();

        this.contextMenuTag = tag;
        this.contextMenuX = event.clientX;
        this.contextMenuY = event.clientY;
        this.contextMenuVisible = true;

        // Hide context menu when clicking elsewhere
        setTimeout(() => {
            document.addEventListener('click', this.hideContextMenu.bind(this), {once: true});
        });
    }

    /**
     * Hide context menu
     */
    hideContextMenu(): void {
        this.contextMenuVisible = false;
        this.contextMenuTag = null;
    }

    /**
     * Edit tag from context menu
     */
    editTag(): void {
        if (this.contextMenuTag) {
            this.tagEdited.emit(this.contextMenuTag);
        }
        this.hideContextMenu();
    }

    /**
     * Delete tag from context menu
     */
    deleteTag(): void {
        if (this.contextMenuTag) {
            this.tagDeleted.emit(this.contextMenuTag);
        }
        this.hideContextMenu();
    }

    /**
     * Add child tag from context menu
     */
    addChildTag(): void {
        if (this.contextMenuTag) {
            // Emit event to parent component to handle child creation
            this.tagEdited.emit(this.contextMenuTag);
        }
        this.hideContextMenu();
    }

    /**
     * Expand all nodes
     */
    expandAll(): void {
        this.expandAllNodes(this.treeNodes);
        this.updateFlattenedNodes();
    }

    /**
     * Collapse all nodes
     */
    collapseAll(): void {
        this.collapseAllNodes(this.treeNodes);
        this.updateFlattenedNodes();
    }

    /**
     * Recursively expand all nodes
     */
    private expandAllNodes(nodes: TagTreeNode[]): void {
        nodes.forEach(node => {
            node.expanded = true;
            this.expandAllNodes(node.children);
        });
    }

    /**
     * Recursively collapse all nodes
     */
    private collapseAllNodes(nodes: TagTreeNode[]): void {
        nodes.forEach(node => {
            node.expanded = false;
            this.collapseAllNodes(node.children);
        });
    }

    /**
     * Get context menu style
     */
    getContextMenuStyle(): any {
        return {
            position: 'fixed',
            left: `${this.contextMenuX}px`,
            top: `${this.contextMenuY}px`,
            'z-index': 1000
        };
    }

    /**
     * Handle drop on root level
     */
    onDropToRoot(event: DragEvent): void {
        if (!this.draggable || !this.draggedNode) return;

        event.preventDefault();

        const draggedTag = this.draggedNode.tag;

        // Check if tag has valid ID
        if (!draggedTag.id) {
            this.error = 'Invalid tag data for move operation';
            this.successMessage = '';
            return;
        }

        // Move to root (no parent)
        this.validateAndMoveTag(draggedTag.id, '');
    }

    /**
     * Track by function for ngFor
     */
    trackByTagId(index: number, node: TagTreeNode): string {
        return node.tag.id || `temp-${index}`;
    }
}