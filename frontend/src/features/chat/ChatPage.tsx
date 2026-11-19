import { useEffect, useLayoutEffect, useRef, useState, type FormEvent, type KeyboardEvent } from 'react';
import { chatApi } from '../../lib/api/chatApi';
import { Button, ErrorNotice, LoadingState } from '../../components/ui';
import './chat.css';

type Exchange = {
  id: number;
  question: string;
  answer: string;
  sources: string[];
};

type ChatPageProps = {
  onCreateTicket: (issue: string) => void;
};

const guideTopics = [
  { label: '網路與連線', detail: 'VPN、Wi-Fi', question: 'VPN 無法連線時，可以先檢查哪些設定？' },
  { label: '帳號與登入', detail: '密碼、MFA', question: '忘記公司密碼要如何重設？' },
  { label: '電腦與周邊', detail: '電腦、印表機', question: '印表機無法列印，該怎麼排除？' },
  { label: '辦公軟體', detail: 'Outlook、Teams', question: 'Teams 麥克風沒有聲音時如何處理？' },
  { label: '報修進度', detail: '依單號查詢', question: '幫我查詢 IT-001 報修單目前狀態' },
];

function errorMessage(error: unknown): string {
  if (error instanceof Error && error.message.trim()) return error.message;
  return '目前無法取得回答，請檢查連線後再試一次。';
}

function isTicketStatusQuestion(value: string): boolean {
  return /(?:報修單|IT-\d+)/i.test(value) && /(?:查|狀態|進度)/.test(value);
}

type AnswerBlock = { kind: 'paragraph' | 'ordered' | 'unordered'; lines: string[] };

function formatAnswer(answer: string): AnswerBlock[] {
  const blocks: AnswerBlock[] = [];
  let paragraph: string[] = [];
  let list: AnswerBlock | null = null;

  const flushParagraph = () => {
    if (paragraph.length) blocks.push({ kind: 'paragraph', lines: paragraph });
    paragraph = [];
  };
  const flushList = () => {
    if (list) blocks.push(list);
    list = null;
  };
  const addListItem = (kind: 'ordered' | 'unordered', item: string) => {
    if (list && list.kind === kind) {
      list.lines.push(item);
      return;
    }
    flushList();
    list = { kind, lines: [item] };
  };

  for (const rawLine of answer.split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line) {
      flushParagraph();
      flushList();
      continue;
    }

    const orderedItem = line.match(/^\d+[.)、]\s+(.+)$/);
    const unorderedItem = line.match(/^[-*•]\s+(.+)$/);
    if (orderedItem || unorderedItem) {
      flushParagraph();
      const kind = orderedItem ? 'ordered' : 'unordered';
      addListItem(kind, (orderedItem ?? unorderedItem)![1]);
      continue;
    }

    flushList();
    paragraph.push(line);
  }

  flushParagraph();
  flushList();
  return blocks;
}

export default function ChatPage({ onCreateTicket }: ChatPageProps) {
  const [question, setQuestion] = useState('');
  const [exchanges, setExchanges] = useState<Exchange[]>([]);
  const [pending, setPending] = useState<Exchange | null>(null);
  const isLoading = pending !== null;
  const messages = pending ? [...exchanges, pending] : exchanges;
  const [validationError, setValidationError] = useState('');
  const [requestError, setRequestError] = useState('');
  const [announcement, setAnnouncement] = useState('');
  const loadingRef = useRef(false);
  const questionInput = useRef<HTMLTextAreaElement>(null);
  const transcript = useRef<HTMLDivElement>(null);
  const followMessages = useRef(true);
  const latestAnswer = useRef<HTMLDivElement>(null);
  const nextId = useRef(0);
  const conversationId = useRef(crypto.randomUUID());

  useEffect(() => {
    const viewport = window.visualViewport;
    if (!viewport) return;
    const root = document.documentElement;
    function fitViewport() {
      if (viewport?.scale === 1) {
        root.style.setProperty('--chat-viewport', `${viewport.height + viewport.offsetTop}px`);
      } else {
        root.style.removeProperty('--chat-viewport');
      }
    }
    fitViewport();
    viewport.addEventListener('resize', fitViewport);
    viewport.addEventListener('scroll', fitViewport);
    return () => {
      viewport.removeEventListener('resize', fitViewport);
      viewport.removeEventListener('scroll', fitViewport);
      root.style.removeProperty('--chat-viewport');
    };
  }, []);

  useLayoutEffect(() => {
    const pane = transcript.current;
    if (!pane || !followMessages.current) return;
    if (pending || !latestAnswer.current) {
      if (pending) pane.scrollTop = pane.scrollHeight;
      return;
    }
    const offset = latestAnswer.current.getBoundingClientRect().top - pane.getBoundingClientRect().top;
    pane.scrollTop += offset - 12;
  }, [pending, exchanges.length]);

  async function submitQuestion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const submittedQuestion = question.trim();

    if (loadingRef.current) return;
    if (!submittedQuestion) {
      setValidationError('請先輸入想詢問的問題。');
      setRequestError('');
      questionInput.current?.focus();
      return;
    }

    loadingRef.current = true;
    const sent: Exchange = { id: ++nextId.current, question: submittedQuestion, answer: '', sources: [] };
    followMessages.current = true;
    setPending(sent);
    setQuestion('');
    setValidationError('');
    setRequestError('');
    setAnnouncement('');

    try {
      const response = await chatApi.ask(submittedQuestion, conversationId.current);
      setExchanges((current) => [...current, {
        ...sent,
        answer: response.answer ?? '',
        sources: response.sources ?? [],
      }]);
      setAnnouncement('AI 助理已完成回答。');
    } catch (requestError) {
      setQuestion(submittedQuestion);
      setRequestError(errorMessage(requestError));
    } finally {
      loadingRef.current = false;
      setPending(null);
    }
  }

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
      event.preventDefault();
      event.currentTarget.form?.requestSubmit();
    }
  }

  function chooseTopic(nextQuestion: string) {
    setQuestion(nextQuestion);
    setValidationError('');
    setRequestError('');
    questionInput.current?.focus();
  }

  return (
    <div className="chat-page">
      <header className="chat-page__intro">
        <h1 id="chat-page-title">排查對話</h1>
      </header>
      <div className="chat-history" ref={transcript} role="region" aria-labelledby="chat-page-title" tabIndex={0}
        onScroll={(event) => {
          const pane = event.currentTarget;
          followMessages.current = pane.scrollHeight - pane.clientHeight - pane.scrollTop < 48;
        }}>
        <div className="chat-thread">
          {messages.length === 0 && (
            <div className="chat-welcome">
              <h2>需要 IT 協助？</h2>
              <p>先描述遇到的狀況，跟著排查步驟處理；還沒解決時，再接續建立報修。</p>
            </div>
          )}
          <details className="chat-guide" key={messages.length === 0 ? 'welcome' : 'conversation'} open={messages.length === 0}>
            <summary>快速索引</summary>
            <div className="chat-guide__topics">
              {guideTopics.map((topic) => (
                <button
                  className="chat-guide-topic"
                  key={topic.label}
                  type="button"
                  onClick={() => chooseTopic(topic.question)}
                  disabled={isLoading}
                >
                  <span><strong>{topic.label}</strong><small>{topic.detail}</small></span>
                  <svg className="chat-guide-topic__arrow" viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    <path d="M3.5 10h12m-5-5 5 5-5 5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </button>
              ))}
            </div>
          </details>
          <div className="chat-history__list">
            {messages.map((exchange) => (
              <article className="chat-exchange" key={exchange.id} aria-labelledby={`chat-question-title-${exchange.id}`}>
                <div className="chat-exchange__question">
                  <span className="chat-exchange__question-label">你的問題</span>
                  <h3 id={`chat-question-title-${exchange.id}`}>{exchange.question}</h3>
                </div>
                <div className="chat-exchange__answer" ref={exchange === messages.at(-1) ? latestAnswer : undefined}>
                  {exchange === pending ? <LoadingState label="正在整理 IT 資訊，請稍候…" /> : <>
                  <h4>AI 助理回答</h4>
                  <div className="chat-exchange__answer-text">
                    {formatAnswer(exchange.answer || '目前沒有可顯示的回答內容。').map((block, blockIndex) => {
                      if (block.kind === 'ordered' || block.kind === 'unordered') {
                        const List = block.kind === 'ordered' ? 'ol' : 'ul';
                        return <List key={blockIndex}>{block.lines.map((line, lineIndex) => <li key={lineIndex}>{line}</li>)}</List>;
                      }
                      return (
                        <p key={blockIndex}>
                          {block.lines.map((line, lineIndex) => (
                            <span key={lineIndex}>{line}{lineIndex < block.lines.length - 1 && <br />}</span>
                          ))}
                        </p>
                      );
                    })}
                  </div>

                  <section className="chat-sources" aria-labelledby={`chat-sources-${exchange.id}`}>
                    <h5 id={`chat-sources-${exchange.id}`}>參考來源</h5>
                    {exchange.sources.length > 0 ? (
                      <ul>
                        {exchange.sources.map((source, sourceIndex) => (
                          <li key={`${source}-${sourceIndex}`}><code>{source}</code></li>
                        ))}
                      </ul>
                    ) : (
                      <p>這次回答沒有列出參考文件。</p>
                    )}
                  </section>

                  {!isTicketStatusQuestion(exchange.question) && (
                    <div className="chat-handoff">
                      <div>
                        <strong>問題還沒排除？</strong>
                        <span>建立報修單，並帶入你剛才描述的問題。</span>
                      </div>
                      <Button variant="secondary" type="button" onClick={() => onCreateTicket(exchange.question)}>
                        建立報修單
                      </Button>
                    </div>
                  )}
                  </>}
                </div>
              </article>
            ))}
          </div>
        </div>
      </div>
      <form className="chat-compose" onSubmit={submitQuestion} aria-label="從一個問題開始" aria-busy={isLoading}>
        <div className="chat-compose__inner">
          {requestError && <ErrorNotice message={requestError} />}
          {validationError && <p id="chat-question-error" role="alert">{validationError}</p>}
          <div className="chat-input-row">
            <label className="sr-only" htmlFor="chat-question">問題內容</label>
            <textarea id="chat-question" ref={questionInput} value={question} rows={1}
              placeholder="例如：VPN 無法連線時，可以先檢查哪些設定？"
              disabled={isLoading} aria-invalid={Boolean(validationError)}
              aria-describedby={validationError ? 'chat-question-hint chat-question-error' : 'chat-question-hint'}
              onKeyDown={handleKeyDown}
              onChange={(event) => {
                setQuestion(event.target.value);
                if (validationError) setValidationError('');
                if (requestError) setRequestError('');
              }} />
            <Button type="submit" className="chat-compose__submit" variant="primary"
              disabled={isLoading} aria-keyshortcuts="Control+Enter Meta+Enter"
              aria-label={isLoading ? '正在整理回答' : '詢問 AI'} title="詢問 AI">
              <svg viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 19V5m-6 6 6-6 6 6" />
              </svg>
            </Button>
          </div>
          <div id="chat-question-hint" className="chat-compose__notes">
            <span>請勿輸入密碼、驗證碼或 VPN 設定檔。對話只保留在目前頁面。</span>
            <span className="chat-shortcut">可用 Ctrl + Enter 或 ⌘ + Enter 送出</span>
          </div>
        </div>
      </form>
      <p className="sr-only" role="status" aria-live="polite">{announcement}</p>
    </div>
  );
}
