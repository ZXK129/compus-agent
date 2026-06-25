import { get, post } from '../../../shared/api/client';
import type { Course } from '../../../shared/types';

export const courseApi = {
  getAll: () => get<Course[]>('/courses'),
  getByWeekday: (weekday: number) => get<Course[]>(`/courses?weekday=${weekday}`),
  getById: (id: number) => get<Course>(`/courses/${id}`),
  checkin: (id: number) => post<string>(`/courses/${id}/checkin`),
};
