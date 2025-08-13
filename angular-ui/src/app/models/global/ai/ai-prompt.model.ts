import {BasisDtoEntityModel} from '../../basis-dto-entity.model';

export interface AiPromptModel extends BasisDtoEntityModel {
    code: string;
    description?: string;
    prompt: string;
    language?: string;
    active?: boolean;
    amountOfArguments?: number;
    argsDescription?: string;
    categoryId: string;
    categoryName?: string;
}