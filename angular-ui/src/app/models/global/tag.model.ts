import {BasisDtoEntityModel} from "../basis-dto-entity.model";

export interface TagDTO extends BasisDtoEntityModel {
    name: string;
    description?: string;
    slug?: string;
    path?: string;
    parent?: TagDTO;
    children?: TagDTO[];
    questionCount?: number;
    questionIds?: string[];
    answeredQuestions?: number;
    progressPercentage?: number;
    isPopular?: boolean;
    depth?: number;
    isRoot?: boolean;
    isLeaf?: boolean;
}

export interface TagSearchRequest {
    query?: string;
    page?: number;
    size?: number;
    sort?: string;
}

export interface TagPageResponse {
    content: TagDTO[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}

export interface TagWithQuestionCount extends BasisDtoEntityModel {
    name: string;
    description?: string;
    questionCount: number;
}

export interface TagCreateRequest {
    name: string;
    description?: string;
    parentId?: string;
}

export interface TagUpdateRequest {
    id: string;
    name: string;
    description?: string;
}

export interface TagMoveRequest {
    tagId: string;
    newParentId?: string;
}

export interface TagTreeNode {
    tag: TagDTO;
    children: TagTreeNode[];
    expanded?: boolean;
    level: number;
}

export interface TagHierarchyResponse {
    rootTags: TagDTO[];
    allTags: TagDTO[];
}

export interface TagValidationResponse {
    valid: boolean;
    message?: string;
}
