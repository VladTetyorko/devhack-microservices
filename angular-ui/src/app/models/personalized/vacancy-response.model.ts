import {UserOwnedEntity} from "../user-owned-entity.model";
import {InterviewStageDTO} from "../global/interview-stage.model";

export interface VacancyResponseDTO extends UserOwnedEntity {
    vacancyId: string;

    companyName?: string;
    position?: string;
    technologies?: string;

    pros?: string;
    cons?: string;
    notes?: string;

    salary?: string;
    location?: string;

    interviewStageId: string;
    interviewStage: string;
    interviewStageDTO?: InterviewStageDTO;

    /** Tags you’ve attached: just IDs & names, no full Tag objects */
    tagIds: string[];
    tagNames: string[];
}
