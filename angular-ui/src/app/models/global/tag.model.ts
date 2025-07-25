export interface TagDTO {
  id?: string;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
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

export interface TagWithQuestionCount {
  id: string;
  name: string;
  description?: string;
  questionCount: number;
  createdAt: string;
}
