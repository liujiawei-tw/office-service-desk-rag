# IT 故障排查與回報中心

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

員工可自行排查工作場所的 IT 與辦公設施問題，或直接進入「報修單」提交問題並追蹤進度。

## Product Purpose

預設頁面為「AI 問答」。員工可用自己的話描述問題，也可從快速索引選題、修改後送出。AI 以知識文件協助排查；需要後續處理時，員工可從回答直接開啟已帶入原問題的報修表單。

## Operating Context

React 前端有「AI 問答」與「報修單」兩個頁面。問答由 `helpdesk-ai` 檢索文件並呼叫 Gemini，需要查詢或異動報修資料時使用 MCP 工具。報修單頁透過 REST 操作同一個報修服務，資料存於 MySQL。現有服務未實作使用者身分驗證及逐筆報修授權。[架構與啟動方式](README.md)

## Capabilities and Constraints

- **問答與來源**　同一頁可接續提問。切換至報修單或重新整理後，前端會建立新對話。每個回答顯示來源檔名或無來源提示，工具使用紀錄由 API 提供。[對話元件](frontend/src/features/chat/ChatPage.tsx)
- **建立報修單**　表單須填報修人、從九種類別選一項，並提供問題描述。送出成功後，服務產生編號與建立時間，初始狀態為「待處理」。在對話中明確要求建單時，AI 也可透過工具建立。[報修工具](mcp-ticket-server/src/main/java/com/example/ticketserver/mcp/TicketStatusTools.java)
- **查找與管理**　清單可依編號、報修人或問題描述搜尋，並按實際存在的狀態篩選。編輯可變更報修人、問題描述與狀態；狀態支援自訂文字。清單以台北時間顯示建立時間。刪除前會要求確認。[報修單元件](frontend/src/features/tickets/TicketsPage.tsx)

## Product Principles

讓員工能在回答旁核對文件來源，從未解決的問題接續建單，並使用報修服務回傳的編號與狀態追蹤進度。
