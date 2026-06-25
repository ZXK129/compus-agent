import { get, post } from '../../../shared/api/client';
import type { CampusMoment } from '../../../shared/types';

export const presenceApi = {
  getMoments: () => get<CampusMoment[]>('/presence/moments'),
  createMoment: (title: string, tag: string, location: string) => post<CampusMoment>('/presence/moments', { title, tag, location }),
  joinEvent: (id: number) => post<CampusMoment>(`/presence/moments/${id}/join`),
  getCheckinStatus: () => get<string>('/presence/checkin-status'),
  checkin: () => post<string>('/presence/checkin'),
};
