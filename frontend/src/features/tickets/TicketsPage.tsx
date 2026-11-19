import { useCallback, useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { ticketsApi } from '../../lib/api/ticketsApi';
import type { TicketResponse } from '../../lib/api/types';
import { Button, EmptyState, ErrorNotice, Field, LoadingState, StatusBadge, SuccessNotice } from '../../components/ui';
import './tickets.css';

type TicketDraft = {
  requesterName: string;
  category: string;
  issue: string;
  status: string;
};

type TicketsPageProps = {
  initialIssue?: string | null;
};

const COMMON_STATUSES = ['待處理', '處理中', '已完成'];
const TICKET_CATEGORIES = [
  ['NETWORK', '網路'],
  ['COMPUTER', '電腦'],
  ['PERIPHERAL', '周邊設備'],
  ['ACCOUNT', '帳號與權限'],
  ['OFFICE_SOFTWARE', '辦公軟體'],
  ['TICKET_SERVICE', '報修服務'],
  ['MEETING_EQUIPMENT', '會議設備'],
  ['OFFICE_FACILITY', '辦公設施'],
  ['OTHER', '其他'],
] as const;

function formatCreatedAt(value?: string | null): string {
  if (!value || typeof value !== 'string') return '未提供';
  const parsed = new Date(/[zZ]|[+-]\d{2}:?\d{2}$/.test(value) ? value : `${value}+08:00`);
  if (Number.isNaN(parsed.getTime())) return '未提供';
  return new Intl.DateTimeFormat('zh-TW', {
    timeZone: 'Asia/Taipei', year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
  }).format(parsed);
}

function errorMessage(error: unknown): string {
  return error instanceof Error && error.message.trim()
    ? error.message
    : '目前無法完成操作，請稍後再試。';
}

export default function TicketsPage({ initialIssue }: TicketsPageProps) {
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [draft, setDraft] = useState<TicketDraft | null>(null);
  const [editingTicket, setEditingTicket] = useState<TicketResponse | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const loadTickets = useCallback(async (manual = false) => {
    if (manual) setIsRefreshing(true);
    else setIsLoading(true);
    setLoadError(null);
    setActionError(null);
    setNotice(null);
    try {
      setTickets(await ticketsApi.list());
    } catch (error) {
      setLoadError(errorMessage(error));
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    void loadTickets();
  }, [loadTickets]);

  useEffect(() => {
    if (!initialIssue) return;
    setEditingTicket(null);
    setDraft({ requesterName: '', category: '', issue: initialIssue, status: '' });
    setValidationError(null);
    setActionError(null);
    setNotice(null);
  }, [initialIssue]);

  const statuses = useMemo(
    () => [...new Set(tickets.map((ticket) => ticket.status).filter((status) => status.trim()))]
      .sort((left, right) => left.localeCompare(right, 'zh-Hant')),
    [tickets],
  );

  const visibleTickets = useMemo(() => {
    const normalizedQuery = query.trim().toLocaleLowerCase();
    return tickets.filter((ticket) => {
      const matchesQuery = !normalizedQuery
        || ticket.ticketId.toLocaleLowerCase().includes(normalizedQuery)
        || ticket.requesterName.toLocaleLowerCase().includes(normalizedQuery)
        || ticket.issue.toLocaleLowerCase().includes(normalizedQuery);
      const matchesStatus = statusFilter === 'all' || ticket.status === statusFilter;
      return matchesQuery && matchesStatus;
    });
  }, [query, statusFilter, tickets]);

  const openCreateForm = () => {
    setEditingTicket(null);
    setDraft({ requesterName: '', category: '', issue: '', status: '' });
    setValidationError(null);
    setActionError(null);
    setNotice(null);
  };

  const openEditForm = (ticket: TicketResponse) => {
    setEditingTicket(ticket);
    setDraft({ requesterName: ticket.requesterName, category: '', issue: ticket.issue, status: ticket.status });
    setValidationError(null);
    setActionError(null);
    setNotice(null);
  };

  const closeForm = () => {
    if (isSaving) return;
    setDraft(null);
    setEditingTicket(null);
    setValidationError(null);
  };

  const saveTicket = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!draft) return;

    const requesterName = draft.requesterName.trim();
    const category = draft.category.trim();
    const issue = draft.issue.trim();
    const status = draft.status.trim();
    if (!requesterName) {
      setValidationError('請輸入報修人名稱。');
      return;
    }
    if (requesterName.length > 100) {
      setValidationError('報修人名稱不可超過 100 個字元。');
      return;
    }
    if (!editingTicket && !category) {
      setValidationError('請選擇報修類別。');
      return;
    }
    if (!issue) {
      setValidationError('請輸入問題描述。');
      return;
    }
    if (editingTicket && !status) {
      setValidationError('請輸入報修單狀態。');
      return;
    }

    setValidationError(null);
    setActionError(null);
    setIsSaving(true);
    try {
      if (editingTicket) {
        const updated = await ticketsApi.update(editingTicket.ticketId, { requesterName, issue, status });
        setTickets((current) => current.map((ticket) => (
          ticket.ticketId === updated.ticketId ? updated : ticket
        )));
        setNotice(`已更新報修單 ${updated.ticketId}。`);
      } else {
        const created = await ticketsApi.create({ requesterName, category, issue });
        setTickets((current) => [...current, created]);
        setNotice(`已建立報修單 ${created.ticketId}。`);
      }
      setDraft(null);
      setEditingTicket(null);
    } catch (error) {
      setActionError(errorMessage(error));
    } finally {
      setIsSaving(false);
    }
  };

  const removeTicket = async (ticket: TicketResponse) => {
    const confirmed = window.confirm(`確定刪除報修單 ${ticket.ticketId}？此操作無法復原。`);
    if (!confirmed) return;

    setActionError(null);
    setNotice(null);
    setDeletingId(ticket.ticketId);
    try {
      await ticketsApi.delete(ticket.ticketId);
      setTickets((current) => current.filter((item) => item.ticketId !== ticket.ticketId));
      setStatusFilter((current) => (
        current !== 'all' && current === ticket.status
          && tickets.filter((item) => item.status === current).length === 1
          ? 'all'
          : current
      ));
      setNotice(`已刪除報修單 ${ticket.ticketId}。`);
      if (editingTicket?.ticketId === ticket.ticketId) closeForm();
    } catch (error) {
      setActionError(errorMessage(error));
    } finally {
      setDeletingId(null);
    }
  };

  const isSearchActive = query.trim().length > 0 || statusFilter !== 'all';
  const ticketSummary = [
    { label: '全部報修單', value: tickets.length, tone: 'total' },
    { label: '待處理', value: tickets.filter((ticket) => /待處理|待辦|pending|open/i.test(ticket.status)).length, tone: 'waiting' },
    { label: '處理中', value: tickets.filter((ticket) => /處理中|進行中|working|in progress/i.test(ticket.status)).length, tone: 'working' },
    { label: '已完成', value: tickets.filter((ticket) => /已完成|完成|結案|closed|resolved|done/i.test(ticket.status)).length, tone: 'done' },
  ];

  return (
    <div className="tickets-page">
      <header className="tickets-page__header">
        <div>
          <h1>報修單</h1>
          <p>建立報修、搜尋問題描述，或查看服務端回傳的處理狀態。</p>
        </div>
        <div className="tickets-page__header-actions">
          <Button
            variant="secondary"
            type="button"
            onClick={() => void loadTickets(true)}
            disabled={isLoading || isRefreshing}
          >
            {isRefreshing ? '載入中…' : '重新載入'}
          </Button>
          <Button variant="primary" type="button" onClick={openCreateForm}>
            建立報修單
          </Button>
        </div>
      </header>

      {loadError && (
        <div className="tickets-page__notice">
          <ErrorNotice message={`無法載入報修單：${loadError}`} onRetry={() => void loadTickets(true)} />
        </div>
      )}
      {actionError && <div className="tickets-page__notice"><ErrorNotice message={actionError} /></div>}
      {notice && <div className="tickets-page__notice"><SuccessNotice message={notice} /></div>}

      <section className="ticket-summary" aria-label="報修單狀態概況">
        {ticketSummary.map((item) => (
          <div className={`ticket-summary__card ticket-summary__card--${item.tone}`} key={item.tone}>
            <span className="ticket-summary__label">{item.label}</span>
            <strong>{isLoading ? '—' : item.value}</strong>
            <span className="ticket-summary__unit">筆</span>
          </div>
        ))}
      </section>

      {draft && (
        <section className="ticket-editor" aria-labelledby="ticket-editor-title">
          <div className="ticket-editor__heading">
            <div>
              <h2 id="ticket-editor-title">
                {editingTicket ? `編輯報修單 ${editingTicket.ticketId}` : '建立報修單'}
              </h2>
              <p>{editingTicket ? '更新報修人、問題描述與目前狀態。' : '填寫報修人名稱和遇到的問題；若從 AI 問答接續，問題描述會先帶入。'}</p>
            </div>
            <Button variant="secondary" type="button" onClick={closeForm} disabled={isSaving}>
              取消
            </Button>
          </div>
          <form className="ticket-editor__form" onSubmit={saveTicket} noValidate>
            <Field label="報修人名稱" htmlFor="ticket-requester-name">
              <input
                className="input"
                id="ticket-requester-name"
                name="requesterName"
                type="text"
                maxLength={100}
                value={draft.requesterName}
                onChange={(event) => setDraft({ ...draft, requesterName: event.target.value })}
                aria-describedby={validationError?.includes('報修人') ? 'ticket-form-error' : undefined}
                aria-invalid={Boolean(validationError?.includes('報修人'))}
                required
              />
              <span className="ticket-editor__hint">僅填寫姓名，請勿填入密碼或驗證碼。</span>
            </Field>

            {!editingTicket && (
              <Field label="報修類別" htmlFor="ticket-category">
                <select
                  className="select"
                  id="ticket-category"
                  name="category"
                  value={draft.category}
                  onChange={(event) => setDraft({ ...draft, category: event.target.value })}
                  aria-describedby={validationError?.includes('類別') ? 'ticket-form-error' : 'ticket-category-hint'}
                  aria-invalid={Boolean(validationError?.includes('類別'))}
                  required
                >
                  <option value="">請選擇類別</option>
                  {TICKET_CATEGORIES.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                </select>
                <span className="ticket-editor__hint" id="ticket-category-hint">請選擇最符合問題的類別。</span>
              </Field>
            )}

            <Field
              label="問題描述"
              htmlFor="ticket-issue"
            >
              <textarea
                className="textarea"
                id="ticket-issue"
                name="issue"
                rows={3}
                value={draft.issue}
                onChange={(event) => setDraft({ ...draft, issue: event.target.value })}
                aria-describedby={validationError ? 'ticket-form-error' : 'ticket-issue-hint'}
                aria-invalid={Boolean(validationError && !draft.issue.trim())}
                required
              />
              <span className="ticket-editor__hint" id="ticket-issue-hint">
                {editingTicket ? '描述問題本身即可，請勿填入密碼、驗證碼或不相關的個人資料。' : '請勿填入密碼或驗證碼。建立後編號與預設狀態會由服務端提供。'}
              </span>
            </Field>

            {editingTicket && (
              <Field label="狀態" htmlFor="ticket-status">
                <input
                  className="input"
                  id="ticket-status"
                  name="status"
                  list="ticket-status-suggestions"
                  value={draft.status}
                  onChange={(event) => setDraft({ ...draft, status: event.target.value })}
                  aria-describedby={validationError ? 'ticket-form-error' : 'ticket-status-hint'}
                  aria-invalid={Boolean(validationError && !draft.status.trim())}
                  required
                />
                <datalist id="ticket-status-suggestions">
                  {COMMON_STATUSES.map((status) => <option key={status} value={status} />)}
                </datalist>
                <span className="ticket-editor__hint" id="ticket-status-hint">可輸入任意狀態文字，也可選擇常見狀態。</span>
              </Field>
            )}

            {validationError && <p className="ticket-editor__error" id="ticket-form-error" role="alert">{validationError}</p>}
            <div className="ticket-editor__actions">
              <Button variant="secondary" type="button" onClick={closeForm} disabled={isSaving}>取消</Button>
              <Button variant="primary" type="submit" disabled={isSaving}>
                {isSaving ? '儲存中…' : editingTicket ? '儲存變更' : '送出報修單'}
              </Button>
            </div>
          </form>
        </section>
      )}

      <section className="ticket-list-panel" aria-labelledby="ticket-list-title">
        <div className="ticket-list-panel__heading">
          <div>
            <h2 id="ticket-list-title">報修清單</h2>
            <p>{isLoading ? '正在從服務端載入資料…' : `目前顯示 ${visibleTickets.length} 筆，共 ${tickets.length} 筆`}</p>
          </div>
        </div>

        <div className="ticket-filters">
          <div className="ticket-filters__search">
            <Field label="搜尋報修單" htmlFor="ticket-search">
            <input
              className="input"
              id="ticket-search"
              type="search"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
                placeholder="依編號、報修人或問題描述搜尋"
            />
            </Field>
          </div>
          <div className="ticket-filters__status">
            <Field label="狀態" htmlFor="ticket-status-filter">
            <select
              className="select"
              id="ticket-status-filter"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="all">所有狀態</option>
              {statuses.map((status) => <option key={status} value={status}>{status}</option>)}
            </select>
            </Field>
          </div>
        </div>

        {isLoading ? (
          <LoadingState label="正在載入報修單…" />
        ) : tickets.length === 0 && !loadError ? (
          <EmptyState
            title="目前沒有報修單"
            description="建立報修單後，編號和狀態會由服務端提供。"
            action={<Button variant="primary" type="button" onClick={openCreateForm}>建立第一張報修單</Button>}
          />
        ) : visibleTickets.length === 0 ? (
          <EmptyState
            title={isSearchActive ? '找不到符合條件的報修單' : '目前沒有報修單'}
            description={isSearchActive ? '請調整搜尋文字或狀態篩選。' : '重新載入服務端資料後再試一次。'}
            action={isSearchActive ? (
              <Button variant="secondary" type="button" onClick={() => { setQuery(''); setStatusFilter('all'); }}>
                清除篩選
              </Button>
            ) : undefined}
          />
        ) : (
          <>
            <div className="ticket-list__columns" aria-hidden="true">
              <span>報修單編號</span>
              <span>報修人</span>
              <span>問題描述</span>
              <span>狀態</span>
              <span>建立時間</span>
              <span>操作</span>
            </div>
            <ul className="ticket-list" aria-label="報修單列表">
              {visibleTickets.map((ticket) => (
                <li className="ticket-row" key={ticket.ticketId}>
                  <div className="ticket-row__id">
                    <span className="ticket-row__field-label">報修單編號</span>
                    <span className="ticket-id">{ticket.ticketId}</span>
                  </div>
                  <div className="ticket-row__requester">
                    <span className="ticket-row__field-label">報修人</span>
                    <span>{ticket.requesterName}</span>
                  </div>
                  <div className="ticket-row__issue">
                    <span className="ticket-row__field-label">問題描述</span>
                    <span>{ticket.issue}</span>
                  </div>
                  <div className="ticket-row__status">
                    <span className="ticket-row__field-label">狀態</span>
                    <StatusBadge status={ticket.status} />
                  </div>
                  <div className="ticket-row__created-at">
                    <span className="ticket-row__field-label">建立時間</span>
                    <time>{formatCreatedAt(ticket.createdAt)}</time>
                  </div>
                  <div className="ticket-row__actions">
                    <span className="ticket-row__field-label">操作</span>
                    <div className="ticket-row__action-buttons">
                      <Button variant="secondary" size="small" type="button" onClick={() => openEditForm(ticket)}>
                        編輯<span className="visually-hidden"> {ticket.ticketId}</span>
                      </Button>
                      <Button
                        variant="danger"
                        size="small"
                        type="button"
                        onClick={() => void removeTicket(ticket)}
                        disabled={deletingId === ticket.ticketId}
                      >
                        {deletingId === ticket.ticketId ? '刪除中…' : '刪除'}
                        <span className="visually-hidden"> {ticket.ticketId}</span>
                      </Button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}
      </section>
    </div>
  );
}
