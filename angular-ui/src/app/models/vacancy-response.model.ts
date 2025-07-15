export interface VacancyResponse {
    id?: string;
    userId?: string;
    vacancyId?: string;
    responseText: string;
    status?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface VacancyResponseDTO extends VacancyResponse {
    // Any additional properties specific to the DTO
}
