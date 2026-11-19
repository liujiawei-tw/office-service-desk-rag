---
name: IT 故障排查與回報中心
description: 員工排查對話與報修單管理介面
colors:
  accent: "#627d6c"
  accent-hover: "#4f6958"
  paper: "#eff4ef"
  ink: "#27362f"
  surface: "#fbfdf9"
  line: "#d5e0d5"
typography:
  body:
    fontFamily: "Noto Sans TC, PingFang TC, Microsoft JhengHei, sans-serif"
    fontSize: "15px"
    lineHeight: 1.65
---

# IT 故障排查與回報中心設計規格

## 導覽與版面

桌面使用 224px 寬的深綠色側欄（`#405849`），目前頁面以淺綠底和深色文字標示。視窗寬度在 760px 以下（含）時，品牌與「AI 問答」「報修單」並列於頂部。鍵盤使用者可透過「跳至主要內容」連結移至主區域。[應用程式外框](frontend/src/app/App.tsx) · [版面規則](frontend/src/app/app.css)

## 色彩、字體與控制項

灰綠色用於主要操作，深墨綠用於內文。全域內文字級為 15px，寬度在 680px 以下（含）時為 16px。共用按鈕最小高度為 44px，表單欄位為 46px，兩者皆使用 3px 圓角。鍵盤焦點使用外框；錯誤與成功提示同時使用文字及色彩。狀態標籤保留服務回傳的文字，並依狀態搭配色塊。[設計變數](frontend/src/styles/tokens.css) · [共用元件樣式](frontend/src/styles/global.css)

## 排查對話

標題、可獨立捲動的對話區、底部輸入框分成三列。對話與輸入框的內容區最寬 880px。快速索引在空白對話時展開，首次送出後收合，可再手動展開。員工問題靠右，回答靠左，回答正文最寬 68ch，可呈現段落、編號或項目清單。

「參考來源」位於回答下方，長檔名可換行。若回應沒有來源，顯示「這次回答沒有列出參考文件。」一般排查回答接著顯示「建立報修單」，將該則提問帶入表單。前端判定為查詢報修進度的提問會省略此按鈕。[對話元件](frontend/src/features/chat/ChatPage.tsx) · [對話樣式](frontend/src/features/chat/chat.css)

## 報修單

頁首提供建立與重新載入操作，下方依序呈現總數與三種常見狀態的統計、開啟中的編輯表單及可篩選的清單。搜尋結果顯示符合筆數與總筆數，統計採用完整清單。

桌面清單分六欄，1100px 以下（含）改成附欄位標籤的紀錄。720px 以下（含）統計改為兩欄，表單改為單欄。刪除前需確認；載入中、資料為空與搜尋無結果各有對應提示。[報修單元件](frontend/src/features/tickets/TicketsPage.tsx) · [報修單樣式](frontend/src/features/tickets/tickets.css)
