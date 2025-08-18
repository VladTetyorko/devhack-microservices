import {BasisDtoEntityModel} from '../../basis-dto-entity.model';

export interface AiPromptModel extends BasisDtoEntityModel {
    // New schema fields
    key?: string;
    systemTemplate?: string;
    userTemplate?: string;
    enabled?: boolean;
    argsSchema?: any;
    defaults?: any;
    model?: string;
    parameters?: any;
    responseContract?: any;
    version?: number;
    description?: string;
    categoryId: string;
    categoryName?: string;
    createdAt?: string;
    updatedAt?: string;

    // Legacy fields kept for backward compatibility in templates (will be removed later)
    code?: string;
    prompt?: string;
    language?: string;
    active?: boolean;
    amountOfArguments?: number;
    argsDescription?: string;
}