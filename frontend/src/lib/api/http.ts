type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  body?: unknown;
};

function readMessage(payload: unknown): string | null {
  if (typeof payload === 'string' && payload.trim()) return payload.trim();
  if (!payload || typeof payload !== 'object') return null;

  const record = payload as Record<string, unknown>;
  for (const key of ['message', 'detail', 'title', 'error']) {
    const value = record[key];
    if (typeof value === 'string' && value.trim()) return value.trim();
  }

  const errors = record.errors;
  if (errors && typeof errors === 'object') {
    const messages = Object.values(errors as Record<string, unknown>)
      .flatMap((value) => Array.isArray(value) ? value : [value])
      .filter((value): value is string => typeof value === 'string' && Boolean(value.trim()));
    if (messages.length) return messages.join('、');
  }

  return null;
}

export async function requestJson<T>(url: string, options: RequestOptions = {}): Promise<T> {
  let response: Response;
  try {
    response = await fetch(url, {
      method: options.method ?? 'GET',
      headers: options.body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    });
  } catch {
    throw new Error('無法連線至服務，請確認後端已啟動後重試。');
  }

  if (!response.ok) {
    const body = await response.text().catch(() => '');
    let payload: unknown = body;
    if (body.trim()) {
      try {
        payload = JSON.parse(body) as unknown;
      } catch {
        if (!response.headers.get('content-type')?.startsWith('text/plain')) payload = null;
      }
    }
    const message = readMessage(payload);
    throw new Error(message ?? `服務回應錯誤（HTTP ${response.status}），請稍後重試。`);
  }

  if (response.status === 204) return undefined as T;
  try {
    return await response.json() as T;
  } catch {
    throw new Error('服務回傳的資料格式無法辨識，請稍後重試。');
  }
}

export const http = {
  get: <T>(url: string) => requestJson<T>(url),
  post: <T>(url: string, body: unknown) => requestJson<T>(url, { method: 'POST', body }),
  put: <T>(url: string, body: unknown) => requestJson<T>(url, { method: 'PUT', body }),
  delete: (url: string) => requestJson<void>(url, { method: 'DELETE' }),
};
