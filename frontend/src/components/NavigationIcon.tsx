import type { ReactNode } from 'react';

type NavigationIconName = 'assistant' | 'tickets' | 'menu' | 'close';

type NavigationIconProps = {
  name: NavigationIconName;
  size?: number;
};

export function NavigationIcon({ name, size = 20 }: NavigationIconProps) {
  const paths: Record<NavigationIconName, ReactNode> = {
    assistant: <><path d="M12 3.5v3" /><path d="M12 17.5v3" /><path d="M3.5 12h3" /><path d="M17.5 12h3" /><path d="m6 6 2.1 2.1" /><path d="m15.9 15.9 2.1 2.1" /><path d="m18 6-2.1 2.1" /><path d="M8.1 15.9 6 18" /><circle cx="12" cy="12" r="3.25" /></>,
    tickets: <><path d="M4 7.5h16v3a2 2 0 0 0 0 4v2H4v-2a2 2 0 0 0 0-4z" /><path d="M10 7.5v2" /><path d="M10 14.5v2" /></>,
    menu: <><path d="M4 6h16" /><path d="M4 12h16" /><path d="M4 18h16" /></>,
    close: <><path d="m6 6 12 12" /><path d="M18 6 6 18" /></>,
  };

  return (
    <svg aria-hidden="true" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      {paths[name]}
    </svg>
  );
}
