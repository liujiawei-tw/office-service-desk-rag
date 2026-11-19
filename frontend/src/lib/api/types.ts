export type ChatRequest = {
  question: string;
  conversationId?: string;
};

export type ChatResponse = {
  answer: string;
  sources: string[];
  toolsUsed: string[];
  retrievedChunks: RetrievedChunk[];
};

export type RetrievedChunk = {
  sourceFile: string;
  chunkId: string;
  score: number | null;
  selectedForContext: boolean;
};

export type TicketResponse = {
  ticketId: string;
  requesterName: string;
  issue: string;
  category?: string;
  status: string;
  createdAt?: string | null;
};

export type CreateTicketRequest = {
  requesterName: string;
  category: string;
  issue: string;
};

export type UpdateTicketRequest = {
  requesterName: string;
  issue: string;
  status: string;
};
