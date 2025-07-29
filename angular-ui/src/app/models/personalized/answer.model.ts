import {UserOwnedEntity} from "../user-owned-entity.model";

export interface AnswerDTO extends UserOwnedEntity {
  text: string;
  confidenceLevel?: number;
  aiScore?: number;
  aiFeedback?: string;
  isCorrect?: boolean;
  isCheating?: boolean;
  questionId?: string;
  questionText?: string;
}

export interface AnswerSearchRequest {
  query?: string;
  userId?: string;
  questionId?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface AnswerPageResponse {
  content: AnswerDTO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}