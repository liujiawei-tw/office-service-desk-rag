import { useState } from 'react';
import ChatPage from '../features/chat/ChatPage';
import TicketsPage from '../features/tickets/TicketsPage';
import { NavigationIcon } from '../components/NavigationIcon';
import './app.css';

export type AppView = 'assistant' | 'tickets';

const navigation: { id: AppView; label: string; description: string }[] = [
  { id: 'assistant', label: 'AI 問答', description: '從問題開始排查' },
  { id: 'tickets', label: '報修單', description: '提交並追蹤進度' },
];

function App() {
  const [view, setView] = useState<AppView>('assistant');
  const [ticketIssue, setTicketIssue] = useState<string | null>(null);

  const openTicketWithIssue = (issue: string) => {
    setTicketIssue(issue);
    setView('tickets');
  };

  const navigate = (next: AppView) => {
    setTicketIssue(null);
    setView(next);
  };

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main">跳至主要內容</a>
      <header className="topbar">
        <a className="brand" href="#main" aria-label="IT 故障排查與回報中心首頁">
          <img className="brand-mark" src="/favicon.svg" alt="" width="32" height="32" />
          <strong className="brand-copy">IT 故障排查與回報中心</strong>
        </a>
        <nav className="primary-navigation" aria-label="主要導覽">
          {navigation.map((item) => (
            <button key={item.id} type="button" className={`navigation-item${view === item.id ? ' navigation-item--active' : ''}`} aria-current={view === item.id ? 'page' : undefined} onClick={() => navigate(item.id)}>
              <NavigationIcon name={item.id} />
              <span className="navigation-item__text"><strong>{item.label}</strong></span>
            </button>
          ))}
        </nav>
      </header>

      <main className={`main-content main-content--${view}`} id="main">
        <div className="content-wrap">
          <div className={`page-content page-content--${view}`} key={view}>
            {view === 'assistant'
              ? <ChatPage onCreateTicket={openTicketWithIssue} />
              : <TicketsPage initialIssue={ticketIssue} />}
          </div>
          <footer className="page-footer">資料以系統回傳內容為準</footer>
        </div>
      </main>

    </div>
  );
}

export default App;
