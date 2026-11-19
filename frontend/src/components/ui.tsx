import type { ButtonHTMLAttributes, ReactNode } from 'react';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'default' | 'small' | 'icon';
};

export function Button({ variant = 'secondary', size = 'default', className = '', ...props }: ButtonProps) {
  const sizeClass = size === 'small' ? ' button--small' : size === 'icon' ? ' button--icon' : '';
  return <button className={`button button--${variant}${sizeClass}${className ? ` ${className}` : ''}`} {...props} />;
}

type FieldProps = {
  label: string;
  htmlFor: string;
  hint?: string;
  children: ReactNode;
};

export function Field({ label, htmlFor, hint, children }: FieldProps) {
  return (
    <div className="field">
      <label htmlFor={htmlFor}>{label}</label>
      {children}
      {hint && <span className="field__hint">{hint}</span>}
    </div>
  );
}

export function StatusBadge({ status }: { status: string }) {
  const normalized = status.trim().toLowerCase();
  let tone = 'neutral';
  if (/待處理|待辦|pending|open/.test(normalized)) tone = 'waiting';
  else if (/處理中|進行中|處理中|working|in progress/.test(normalized)) tone = 'working';
  else if (/已完成|完成|結案|closed|resolved|done/.test(normalized)) tone = 'done';
  else if (/緊急|高優先|urgent|critical/.test(normalized)) tone = 'urgent';
  return <span className={`status-badge status-badge--${tone}`}>{status || '未提供狀態'}</span>;
}

export function LoadingState({ label = '載入中…' }: { label?: string }) {
  return <div className="loading-state" role="status"><span className="loading-state__spinner" aria-hidden="true" /><span>{label}</span></div>;
}

export function ErrorNotice({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="notice notice--error" role="alert">
      <span>{message}</span>
      {onRetry && <Button size="small" onClick={onRetry}>再試一次</Button>}
    </div>
  );
}

export function SuccessNotice({ message }: { message: string }) {
  return <div className="notice notice--success" role="status">{message}</div>;
}

export function EmptyState({ title, description, action }: { title: string; description: string; action?: ReactNode }) {
  return (
    <div className="empty-state">
      <p className="empty-state__title">{title}</p>
      <p className="empty-state__copy">{description}</p>
      {action}
    </div>
  );
}
