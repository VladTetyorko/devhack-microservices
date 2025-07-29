import {BasisDtoEntityModel} from "../basis-dto-entity.model";

export interface TagDTO extends BasisDtoEntityModel {
  name: string;
  description?: string;
  questionCount?: number;
  questionIds?: string[];
  answeredQuestions?: number;
  progressPercentage?: number;
  isPopular?: boolean;
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
