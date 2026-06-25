import { get, post } from '../../../shared/api/client';
import type { CardInfo, Transaction } from '../../../shared/types';

export const cardApi = {
  getInfo: () => get<CardInfo>('/card'),
  getTransactions: (limit?: number) => get<Transaction[]>(`/card/transactions${limit ? `?limit=${limit}` : ''}`),
  topUp: (amount: number) => post<CardInfo>('/card/topup', { amount }),
};
