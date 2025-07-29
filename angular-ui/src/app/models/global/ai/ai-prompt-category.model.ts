import {BasisDtoEntityModel} from '../../basis-dto-entity.model';
import {AiPromptModel} from './ai-prompt.model';

export interface AiPromptCategoryModel extends BasisDtoEntityModel {
    code: string;
    description?: string;
    name: string;
    prompts?: AiPromptModel[];
    promptCount?: number;
}