import { get, post } from '../../../shared/api/client';
import type { LibraryBook, Seat } from '../../../shared/types';

export const libraryApi = {
  getBooks: () => get<LibraryBook[]>('/library/books'),
  renewBook: (id: number) => post<LibraryBook>(`/library/books/${id}/renew`),
  getSeats: () => get<Seat[]>('/library/seats'),
  bookSeat: (id: number) => post<Seat>(`/library/seats/${id}/book`),
  releaseSeat: (id: number) => post<void>(`/library/seats/${id}/release`),
  search: (keyword: string) => post<string>('/library/search', { keyword }),
};
