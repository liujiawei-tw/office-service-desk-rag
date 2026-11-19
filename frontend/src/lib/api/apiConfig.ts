const helpdeskBaseUrl = import.meta.env.VITE_HELPDESK_API_BASE_URL?.trim() ?? '';
const ticketsBaseUrl = import.meta.env.VITE_TICKETS_API_BASE_URL?.trim() ?? '';

function endpoint(baseUrl: string, path: string): string {
  return baseUrl ? `${baseUrl.replace(/\/+$/, '')}${path}` : path;
}

export const apiConfig = {
  chatUrl: endpoint(helpdeskBaseUrl, '/api/chat'),
  ticketsUrl: endpoint(ticketsBaseUrl, '/api/tickets'),
};
