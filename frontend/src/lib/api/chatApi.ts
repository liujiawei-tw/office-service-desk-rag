import { apiConfig } from './apiConfig';
import { http } from './http';
import type { ChatRequest, ChatResponse } from './types';

export const chatApi = {
  ask: (question: string, conversationId?: string) => http.post<ChatResponse>(
    apiConfig.chatUrl, { question, conversationId } satisfies ChatRequest,
  ),
};
