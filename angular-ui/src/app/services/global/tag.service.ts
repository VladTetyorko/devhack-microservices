import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
    TagCreateRequest,
    TagDTO,
    TagHierarchyResponse,
    TagMoveRequest,
    TagTreeNode,
    TagValidationResponse
} from '../../models/global/tag.model';
import {BaseService} from '../base.service';

/**
 * Service for managing tags.
 * Extends BaseService to inherit common CRUD operations and follows OOP principles.
 */
@Injectable({
    providedIn: 'root'
})
export class TagService extends BaseService<TagDTO> {
    protected baseUrl = '/api/tags';

    constructor(http: HttpClient) {
        super(http);
    }

    /**
     * Get tag by name
     * @param name - Tag name
     * @returns Observable tag
     */
    getByName(name: string): Observable<TagDTO> {
        return this.http.get<TagDTO>(`${this.baseUrl}/by-name/${name}`);
    }

    /**
     * Search tags by query
     * @param query - Search query
     * @returns Observable array of tags
     */
    override search(searchParams: { [key: string]: string | undefined }): Observable<TagDTO[]>;
    override search(query: string): Observable<TagDTO[]>;
    override search(queryOrParams: string | { [key: string]: string | undefined }): Observable<TagDTO[]> {
        if (typeof queryOrParams === 'string') {
            return super.search({query: queryOrParams});
        }
        return super.search(queryOrParams);
    }

    /**
     * Get popular tags with optional limit
     * @param limit - Optional limit for number of tags
     * @returns Observable array of popular tags
     */
    getPopular(limit?: number): Observable<TagDTO[]> {
        return this.getWithParams('popular', {limit});
    }

    /**
     * Get tags with their question count
     * @returns Observable array of tags with question count
     */
    getTagsWithQuestionCount(): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/with-question-count`);
    }

    // ========== Hierarchical Methods ==========

    /**
     * Create a new tag with optional parent
     * @param request - Tag creation request with parent information
     * @returns Observable created tag
     */
    createWithParent(request: TagCreateRequest): Observable<TagDTO> {
        return this.http.post<TagDTO>(`${this.baseUrl}/hierarchy`, request);
    }

    /**
     * Move a tag to a new parent
     * @param request - Tag move request
     * @returns Observable updated tag
     */
    moveTag(request: TagMoveRequest): Observable<TagDTO> {
        return this.http.put<TagDTO>(`${this.baseUrl}/hierarchy/move`, request);
    }

    /**
     * Delete a tag with cascade option
     * @param id - Tag ID
     * @param cascade - Whether to cascade delete children
     * @returns Observable void
     */
    deleteWithCascade(id: string, cascade: boolean = false): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/hierarchy/${id}?cascade=${cascade}`);
    }

    /**
     * Get all root tags (tags with no parent)
     * @returns Observable array of root tags
     */
    getRootTags(): Observable<TagDTO[]> {
        return this.http.get<TagDTO[]>(`${this.baseUrl}/hierarchy/roots`);
    }

    /**
     * Get direct children of a tag
     * @param parentId - Parent tag ID
     * @returns Observable array of child tags
     */
    getChildren(parentId: string): Observable<TagDTO[]> {
        return this.http.get<TagDTO[]>(`${this.baseUrl}/hierarchy/${parentId}/children`);
    }

    /**
     * Get all descendants of a tag (entire subtree)
     * @param parentId - Parent tag ID
     * @returns Observable array of descendant tags
     */
    getDescendants(parentId: string): Observable<TagDTO[]> {
        return this.http.get<TagDTO[]>(`${this.baseUrl}/hierarchy/${parentId}/descendants`);
    }

    /**
     * Get all ancestors of a tag
     * @param tagId - Tag ID
     * @returns Observable array of ancestor tags
     */
    getAncestors(tagId: string): Observable<TagDTO[]> {
        return this.http.get<TagDTO[]>(`${this.baseUrl}/hierarchy/${tagId}/ancestors`);
    }

    /**
     * Get subtree with limited depth
     * @param parentId - Parent tag ID
     * @param depth - Maximum depth to include
     * @returns Observable array of tags in subtree
     */
    getSubtree(parentId: string, depth: number): Observable<TagDTO[]> {
        return this.http.get<TagDTO[]>(`${this.baseUrl}/hierarchy/${parentId}/subtree?depth=${depth}`);
    }

    /**
     * Get siblings of a tag
     * @param tagId - Tag ID
     * @returns Observable array of sibling tags
     */
    getSiblings(tagId: string): Observable<TagDTO[]> {
        return this.http.get<TagDTO[]>(`${this.baseUrl}/hierarchy/${tagId}/siblings`);
    }

    /**
     * Get tags at specific depth level
     * @param depth - Depth level (0 for root)
     * @returns Observable array of tags at depth
     */
    getTagsByDepth(depth: number): Observable<TagDTO[]> {
        return this.http.get<TagDTO[]>(`${this.baseUrl}/hierarchy/depth/${depth}`);
    }

    /**
     * Validate if a tag move is valid (no cycles)
     * @param tagId - Tag ID to move
     * @param newParentId - New parent ID
     * @returns Observable validation response
     */
    validateMove(tagId: string, newParentId?: string): Observable<TagValidationResponse> {
        const params = newParentId ? `?newParentId=${newParentId}` : '';
        return this.http.get<TagValidationResponse>(`${this.baseUrl}/hierarchy/${tagId}/validate-move${params}`);
    }

    /**
     * Get complete tag hierarchy
     * @returns Observable hierarchy response with root tags and all tags
     */
    getHierarchy(): Observable<TagHierarchyResponse> {
        return this.http.get<TagHierarchyResponse>(`${this.baseUrl}/hierarchy`);
    }

    /**
     * Build tree structure from flat tag list
     * @param tags - Flat array of tags
     * @returns Tree structure
     */
    buildTree(tags: TagDTO[]): TagTreeNode[] {
        const tagMap = new Map<string, TagTreeNode>();
        const rootNodes: TagTreeNode[] = [];

        // Create nodes for all tags
        tags.forEach(tag => {
            if (tag.id) {
                tagMap.set(tag.id, {
                    tag,
                    children: [],
                    expanded: false,
                    level: tag.depth || 0
                });
            }
        });

        // Build tree structure
        tags.forEach(tag => {
            if (tag.id) {
                const node = tagMap.get(tag.id)!;
                if (tag.parent && tag.parent.id) {
                    const parentNode = tagMap.get(tag.parent.id);
                    if (parentNode) {
                        parentNode.children.push(node);
                    }
                } else {
                    rootNodes.push(node);
                }
            }
        });

        return rootNodes;
    }

    /**
     * Flatten tree structure to array
     * @param nodes - Tree nodes
     * @param includeCollapsed - Whether to include collapsed children
     * @returns Flat array of tree nodes
     */
    flattenTree(nodes: TagTreeNode[], includeCollapsed: boolean = true): TagTreeNode[] {
        const result: TagTreeNode[] = [];

        const traverse = (nodeList: TagTreeNode[]) => {
            nodeList.forEach(node => {
                result.push(node);
                if (node.expanded || includeCollapsed) {
                    traverse(node.children);
                }
            });
        };

        traverse(nodes);
        return result;
    }
}
