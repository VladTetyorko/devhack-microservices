export interface VacancyDTO {
  id?: string;
  title: string;
  companyName: string;
  description?: string;
  requirements?: string;
  location?: string;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  employmentType?: string;
  experienceLevel?: string;
  technologies?: string;
  benefits?: string;
  contactEmail?: string;
  contactPhone?: string;
  applicationDeadline?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
  userId?: string;
  userName?: string;
}

export interface VacancySearchRequest {
  query?: string;
  companyName?: string;
  location?: string;
  technologies?: string;
  employmentType?: string;
  experienceLevel?: string;
  salaryMin?: number;
  salaryMax?: number;
  isActive?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

export interface VacancyPageResponse {
  content: VacancyDTO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface VacancyCreateRequest {
  title: string;
  companyName: string;
  description?: string;
  requirements?: string;
  location?: string;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  employmentType?: string;
  experienceLevel?: string;
  technologies?: string;
  benefits?: string;
  contactEmail?: string;
  contactPhone?: string;
  applicationDeadline?: string;
}

export interface VacancyUpdateRequest extends VacancyCreateRequest {
  isActive?: boolean;
}