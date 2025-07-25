export interface InterviewStageDTO {
  id?: string;
  code: string;
  label: string;
  orderIndex: number;
  active: boolean;
  finalStage: boolean;
  createdAt?: string;
  updatedAt?: string;
  categoryId?: string;
  categoryCode?: string;
  categoryLabel?: string;
}

export interface InterviewStageCreateRequest {
  code: string;
  label: string;
  orderIndex: number;
  active?: boolean;
  finalStage?: boolean;
  categoryId: string;
}

export interface InterviewStageUpdateRequest extends InterviewStageCreateRequest {
  id: string;
}

export interface InterviewStageSearchRequest {
  query?: string;
  categoryId?: string;
  active?: boolean;
  finalStage?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

export interface InterviewStagePageResponse {
  content: InterviewStageDTO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface InterviewStageWithStats {
  id: string;
  code: string;
  label: string;
  orderIndex: number;
  active: boolean;
  finalStage: boolean;
  categoryCode: string;
  categoryLabel: string;
  responseCount: number;
  createdAt: string;
}