import { get } from '../../../shared/api/client';
import type { AcademicProfile } from '../../../shared/types';

export const academicApi = {
  getProfile: () => get<AcademicProfile>('/academic/profile'),
};
