import {BasisDtoEntityModel} from "../basis-dto-entity.model";

export interface ProfileDTO extends BasisDtoEntityModel {
  name: string;
  cvFileHref?: string;
  cvFileName?: string;
  cvFileType?: string;
  cvFileSize?: number;
  cvStoragePath?: string;
  cvUploadedAt?: string;
  cvParsedSuccessfully?: boolean;
  aiUsageEnabled?: boolean;
  aiPreferredLanguage?: string;
  aiCvScore?: number;
  aiSkillsSummary?: string;
  aiSuggestedImprovements?: string;
  isVisibleToRecruiters?: boolean;
}
