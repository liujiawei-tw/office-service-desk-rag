/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_HELPDESK_API_BASE_URL?: string;
  readonly VITE_TICKETS_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
