import {InterviewStageDTO} from './interview-stage.model';

export interface InterviewStageCategoryDTO {
  id?: string;
  code: string;
  label: string;
  description?: string;
  createdAt?: string;
  stageCount?: number;
}

export interface InterviewStageCategoryCreateRequest {
  code: string;
  label: string;
  description?: string;
}

export interface InterviewStageCategoryUpdateRequest extends InterviewStageCategoryCreateRequest {
  id: string;
}

export interface InterviewStageCategorySearchRequest {
  query?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface InterviewStageCategoryPageResponse {
  content: InterviewStageCategoryDTO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface InterviewStageCategoryWithStages {
  id: string;
  code: string;
  label: string;
  description?: string;
  stages: InterviewStageDTO[];
  createdAt: string;
}
