import {UserOwnedEntity} from "../user-owned-entity.model";
import {BasisDtoEntityModel} from "../basis-dto-entity.model";

export interface InterviewQuestionDTO extends UserOwnedEntity {
  questionText: string;
  difficulty: string;
  source?: string;
  expectedAnswer?: string;
  hints?: string;
  tagNames?: string[];
  tags?: TagDTO[];
}

export interface TagDTO extends BasisDtoEntityModel {
  name: string;
  description?: string;
}

export interface QuestionSearchRequest {
  query?: string;
  tagName?: string;
  difficulty?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface QuestionPageResponse {
  content: InterviewQuestionDTO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface QuestionGenerationRequest {
  tagName: string;
  count: number;
  difficulty: string;
}

export interface QuestionGenerationResponse {
  message: string;
  questionsGenerated: number;
  status: string;
}
