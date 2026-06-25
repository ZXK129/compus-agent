import { get } from '../../../shared/api/client';
import type { PersonalOverview } from '../../../shared/types';

export const overviewApi = {
  getPersonal: () => get<PersonalOverview>('/overview/personal'),
};
