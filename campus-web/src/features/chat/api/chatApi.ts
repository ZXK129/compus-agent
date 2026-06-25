import { get, post, del } from '../../../shared/api/client';
import type { ChatMessage, ChatResponse } from '../../../shared/types';

export const chatApi = {
  sendMessage: (message: string) => post<ChatResponse>('/chat', { message }),
  getHistory: () => get<ChatMessage[]>('/chat/history'),
  clearHistory: () => del<void>('/chat/history'),
};
