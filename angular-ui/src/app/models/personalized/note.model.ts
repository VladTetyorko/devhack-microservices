export interface Note {
    id?: string;
    content: string;
    linkedQuestionId?: string;
    userId?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface NoteDTO extends Note {
    // Any additional properties specific to the DTO
}
