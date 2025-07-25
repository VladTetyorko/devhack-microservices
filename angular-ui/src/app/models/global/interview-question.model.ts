export interface InterviewQuestionDTO {
  id?: string;
  questionText: string;
  difficulty: string;
  source?: string;
  expectedAnswer?: string;
  hints?: string;
  createdAt?: string;
  updatedAt?: string;
  userId?: string;
  userName?: string;
  tagNames?: string[];
  tags?: TagDTO[];
}

export interface TagDTO {
  id?: string;
  name: string;
  description?: string;
  createdAt?: string;
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
