import {BasisDtoEntityModel} from '../../basis-dto-entity.model';

export interface AiPromptModel extends BasisDtoEntityModel {
    code: string;
    description?: string;
    prompt: string;
    language?: string;
    active?: boolean;
    categoryId: string;
    categoryName?: string;
}