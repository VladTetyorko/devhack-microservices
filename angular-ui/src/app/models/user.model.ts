export interface User {
    id?: string;
    name: string;
    email: string;
    roles?: string[];
    createdAt?: string;
    updatedAt?: string;
}

export interface UserDTO extends User {
    // Any additional properties specific to the DTO
}
