export interface AnswerDTO {
  id?: string;
  text: string;
  confidenceLevel?: number;
  aiScore?: number;
  aiFeedback?: string;
  isCorrect?: boolean;
  isCheating?: boolean;
  createdAt?: string;
  updatedAt?: string;
  userId?: string;
  userName?: string;
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