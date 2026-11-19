---
version: 1
slug: "frontend-src-app-app-tsx"
primary_target: "frontend/src/app/App.tsx"
related_targets: ["frontend/src/features/chat/ChatPage.tsx","frontend/src/features/tickets/TicketsPage.tsx"]
---

# 前端頁面與狀態切換

| 員工操作 | 畫面與狀態 |
| --- | --- |
| 開啟應用程式 | App 預設掛載「AI 問答」。桌面側欄與窄螢幕頂部導覽皆提供兩個目的地。 |
| 從快速索引選題 | 範例文字帶入問題欄位並取得焦點。按送出、Ctrl + Enter 或 ⌘ + Enter 後才發出請求。 |
| 收到回答 | 問題與回答依次留在對話區。來源區顯示檔名或空白提示；報修進度查詢省略轉建單按鈕。 |
| 從排查回答建立報修單 | App 切換頁面，報修表單預填該則問題。員工補齊姓名、類別後提交才會新增資料。 |
| 直接選擇「報修單」 | 進入報修清單，可搜尋、依狀態篩選、建立、編輯及確認刪除。 |
| 從報修單返回「AI 問答」 | React 重新掛載對話元件，以新的對話識別碼開始。 |

頁面切換與帶入問題由 App 管理；對話及報修草稿各自由對應頁面管理。[App](../../frontend/src/app/App.tsx) · [ChatPage](../../frontend/src/features/chat/ChatPage.tsx) · [TicketsPage](../../frontend/src/features/tickets/TicketsPage.tsx)
