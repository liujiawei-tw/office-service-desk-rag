import { apiConfig } from './apiConfig';
import { http } from './http';
import type { CreateTicketRequest, TicketResponse, UpdateTicketRequest } from './types';

export const ticketsApi = {
  list: () => http.get<TicketResponse[]>(apiConfig.ticketsUrl),
  create: (request: CreateTicketRequest) => http.post<TicketResponse>(apiConfig.ticketsUrl, request),
  update: (ticketId: string, request: UpdateTicketRequest) =>
    http.put<TicketResponse>(`${apiConfig.ticketsUrl}/${encodeURIComponent(ticketId)}`, request),
  delete: (ticketId: string) =>
    http.delete(`${apiConfig.ticketsUrl}/${encodeURIComponent(ticketId)}`),
};
