# Office Service Desk RAG

「IT 故障排查與回報中心」提供員工自助服務：先透過 AI 問答查詢知識文件、排查問題；若仍需協助，可接續建立報修單並查看處理狀態。前端使用 React + Vite，後端由 AI/RAG 服務與獨立的報修服務組成。

## 快速開始（Docker Compose）

需要 Docker Engine 與 Docker Compose，以及可用的 Gemini API 金鑰。首次啟動 AI 服務會下載約 1.3 GB 的 ONNX 嵌入模型，請預留網路、磁碟與記憶體資源。

在專案根目錄執行：

~~~powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
New-Item -ItemType Directory -Force .secrets | Out-Null
if (-not (Test-Path .secrets/db_password.txt)) { Copy-Item .secrets.example/db_password.txt .secrets/db_password.txt }
if (-not (Test-Path .secrets/mysql_root_password.txt)) { Copy-Item .secrets.example/mysql_root_password.txt .secrets/mysql_root_password.txt }
# 在 .env 填入 SPRING_AI_GOOGLE_GENAI_API_KEY，並將兩個 .secrets/*.txt 改成不同的密碼
docker compose --env-file .env up --build -d
docker compose --env-file .env ps
~~~

服務健康後，開啟 <http://localhost:5173>。前端經 Nginx 將 `/api/chat` 與 `/api/tickets` 轉送至各自的後端。其他啟動與連接埠細節見下方「環境變數與啟動」及「Docker Compose 一鍵啟動」。

## 架構

~~~text
瀏覽器 → 前端（React + Vite；Compose 使用 Nginx，主機 port 5173）
   |-- /api/chat    → helpdesk-ai（Spring Boot, port 8080）
   |                   |-- Gemini API: gemini-3.8-flash
   |                   |-- Spring AI Transformers: BAAI/bge-large-zh-v1.5 on ONNX Runtime
   |                   |-- TokenTextSplitter → SimpleVectorStore（啟動時重建記憶體索引）
   |                   `-- MCP Client（Streamable HTTP）--+
   |-- /api/tickets ---------------------------------------+--→ mcp-ticket-server（Spring Boot, port 8081）
                                                          |    |-- REST CRUD /api/tickets
                                                          |    |-- MCP 報修單工具
                                                          |    `-- Spring Data JPA + Flyway → MySQL
Postman 可直接呼叫兩個後端的 REST API
~~~

前端的報修單頁也會直接透過 `/api/tickets` 呼叫 MCP Server 的 REST API。AI 應用負責檢索知識文件及產生回答；報修單資料由 MCP Server 存取 MySQL。報修狀態只依 MCP 回傳，不以靜態文件推測。本機開發時三個應用分別啟動；Docker Compose 另啟動 MySQL 與 Nginx。

## Maven 模組與依賴

- helpdesk-ai：POST /api/chat、RAG 文件載入、300-token chunking/向量檢索、Gemini Chat、ONNX Embedding、MCP Client。
- mcp-ticket-server：使用 Spring Data JPA 和 MySQL 儲存報修單，Flyway 管理資料表版本，並提供 REST CRUD API 及 MCP 工具。

後端採用標準 Maven `src/main/java` 與 `src/main/resources` 結構，並依責任分 package：

~~~text
helpdesk-ai/src/main/java/com/example/helpdesk/
├── config/       AI、向量庫與 Web 設定
├── controller/   REST 入口
├── dto/          聊天請求與回應
├── knowledge/    知識文件載入
├── service/      RAG 聊天流程
└── HelpdeskAiApplication.java

helpdesk-ai/src/main/resources/
├── knowledge/    RAG 使用的 TXT 文件
└── application.properties

mcp-ticket-server/src/main/java/com/example/ticketserver/
├── config/       啟動初始化設定
├── controller/   報修單 REST 入口
├── dto/          REST 請求與回應
├── entity/       JPA 實體
├── mcp/          MCP 工具
├── repository/   Spring Data JPA 存取
├── service/      報修單業務流程
└── TicketServerApplication.java

mcp-ticket-server/src/main/resources/
├── db/migration/  Flyway 版本化資料庫 migration
└── application.properties

mcp-ticket-server/scripts/
└── create-ticket-database.sql  建立空資料庫的一次性腳本
~~~

最低環境為 Java 21、Maven、Spring Boot 4.1.2、Spring AI 2.0.2。聊天使用 Gemini API；向量嵌入由 Spring AI Transformers 在 Java 程序內執行 ONNX，不需安裝 Ollama。

## 環境變數與啟動

以下為不使用 Docker 的本機開發流程，需要 Java 21、Maven、Node.js/npm、MySQL，以及可用的 Gemini API 金鑰。報修資料由獨立 MySQL Server 儲存；MySQL 自己管理資料目錄，專案不存放資料庫實體檔。先啟動本機 MySQL 服務，預設連接埠為 `3306`。若是全新資料庫，使用 MySQL Workbench 或 MySQL 用戶端，以管理者帳號執行 `mcp-ticket-server/scripts/create-ticket-database.sql` 一次建立 `ticket_db`。資料庫建立後，Flyway 會在 MCP Server 啟動時自動套用 `src/main/resources/db/migration` 內尚未執行的 migration；Hibernate 只驗證結構，不自行修改資料表。

先從範例建立根目錄 `.env`，填入 Gemini API 金鑰及本機 MySQL 密碼。MCP Server 預設以 `root` 連線 `jdbc:mysql://localhost:3306/ticket_db`；若使用不同帳號或連線網址，在 `.env` 增加 `TICKET_DB_USERNAME` 或 `TICKET_DB_URL`。兩個 Spring Boot 應用都會從專案根目錄載入 `.env`。接著啟動 MCP Server：

~~~powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
# 編輯 .env，設定 SPRING_AI_GOOGLE_GENAI_API_KEY 與 TICKET_DB_PASSWORD
mvn -pl mcp-ticket-server spring-boot:run
~~~

MCP Server 首次啟動時會依序套用 Flyway migration 建立報修單資料表，並新增報修人名稱、類別與建立時間欄位。既有報修單名稱回填為「未提供」，既有建立時間於 V4 migration 回填；新建立時間由伺服器產生。`created_at` 使用 MySQL `DATETIME(6)` 儲存台北本地時間。往後的結構變更請新增下一版 migration，不要修改已存在的 migration。

另開終端機啟動 AI 應用：

~~~powershell
mvn -pl helpdesk-ai spring-boot:run
~~~

本機若改用 8083，在根目錄 `.env` 同時設定 `MCP_SERVER_PORT=8083`（報修服務監聽）與 `TICKET_SERVER_PORT=8083`（AI 的 MCP 連線）。使用 Vite 開發前端時，也要將 [Vite 設定](frontend/vite.config.ts) 的 `/api/tickets` 代理目標改為 8083。

知識 TXT 預設放在 `helpdesk-ai/src/main/resources/knowledge`。以 Maven 在本機啟動時，AI 應用從 classpath 載入；以 Docker Compose 啟動時，Compose 將此主機資料夾唯讀掛載至容器的 `/app/knowledge`，AI 應用從掛載路徑載入。正式部署可在 `.env` 將 `KNOWLEDGE_DIR` 設為 Docker 主機上的知識文件資料夾路徑。文件依網路、電腦、周邊、帳號、辦公軟體、會議設備及辦公設施等問題拆分，提供一般員工可執行的安全排查方式；報修單狀態一律透過 MCP 查詢。

新增、修改或刪除知識 TXT 後，重啟 Docker 的 AI 服務以重新載入文件並建立向量索引：

~~~powershell
docker compose --env-file .env restart helpdesk-ai
~~~

目前不支援服務運行期間自動重載文件；每次重啟會從掛載目錄重新建立記憶體中的索引。掛載為唯讀，知識文件需在 Docker 主機端修改。

首次啟動 AI 應用時，Spring AI 會自動從 Hugging Face 取得 ONNX 模型與 tokenizer，並快取至暫存目錄；不需手動安裝 Ollama 或執行模型下載指令。模型檔約 1.3 GB，首次啟動需要網路連線與足夠的磁碟及記憶體。

`.env.example` 目前提供 `SPRING_AI_GOOGLE_GENAI_API_KEY` 與本機用的 `TICKET_DB_PASSWORD`。需要調整模型或 RAG 行為時，可自行在 `.env` 增加 `GOOGLE_GENAI_CHAT_MODEL`、`EMBEDDING_MODEL`、`RAG_TOP_K`、`RAG_SIMILARITY_THRESHOLD`、`RAG_CHUNK_SIZE` 等變數；預設值可查閱 `helpdesk-ai/src/main/resources/application.properties`。本機資料庫連線預設值及覆寫名稱則見 `mcp-ticket-server/src/main/resources/application.properties`。`.env` 不納入版控。

## Docker Compose 一鍵啟動

Docker Compose 會啟動 MySQL、MCP 報修服務、AI/RAG 服務與 Nginx 前端。複製環境變數範例和本機 secret 範例，並填入 Gemini API 金鑰及兩組資料庫密碼。Compose 的資料庫密碼取自 `.secrets`；`.env.example` 裡的 `TICKET_DB_PASSWORD` 僅供不使用 Docker 的本機 MySQL 流程，可在 Compose 專用的 `.env` 刪除該行：

~~~powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
New-Item -ItemType Directory -Force .secrets | Out-Null
$secretFiles = @('db_password.txt', 'mysql_root_password.txt')
foreach ($name in $secretFiles) {
  $path = Join-Path '.secrets' $name
  if (Test-Path -LiteralPath $path -PathType Container) { throw "$path is a folder; it must be a password file." }
  if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { Copy-Item (Join-Path '.secrets.example' $name) $path }
}
# 編輯 .env 和 .secrets 內的檔案
docker compose --env-file .env up --build -d
~~~

Docker 連線網址與資料庫帳號由 Compose 環境變數設定；密碼由 `.secrets` 掛載給 MySQL 與 Spring Boot，不放在 `.env`。Compose 會等待 MySQL 與報修服務健康後再啟動相依服務；AI 服務完成啟動與知識庫載入後，前端才會啟動。

使用 MySQL Workbench 時選 `Standard TCP/IP`，Hostname 填 `127.0.0.1`、Port 填 `13306`、Username 填 `ticket_app`、Default Schema 填 `ticket_db`；密碼使用 `.secrets/db_password.txt` 的內容。`mysql:3306` 是容器網路內的位址，Workbench 不使用它。

開啟 `http://localhost:5173` 使用前端。API 也會只綁定本機提供：AI API 預設 `http://localhost:8080`，報修 API 預設 `http://localhost:8081`。若這些 port 已被占用，可在 `.env` 覆寫 `FRONTEND_HOST_PORT`、`HELPDESK_HOST_PORT` 或 `MCP_HOST_PORT`。MySQL 容器內使用 `3306`，映射到主機的 `127.0.0.1:13306`；容器之間則透過 Compose 服務名稱連線。

MCP Server 啟動時會執行 Flyway migration，並建立缺少的 IT-001、IT-002、IT-003 示範資料。MySQL 資料與 ONNX 模型快取分別保存在 `mysql_data` 和 `embedding_cache` volume；`docker compose down` 會保留這些資料。首次啟動 AI 服務仍需下載約 1.3 GB 的嵌入模型。

查看服務狀態或日誌：

~~~powershell
docker compose --env-file .env ps
docker compose --env-file .env logs -f helpdesk-ai
~~~

## 報修單 REST API

所有 API 都在 MCP Server（預設 `http://localhost:8081`）：

| 方法 | 路徑 | 說明 | 成功狀態 |
| --- | --- | --- | --- |
| GET | `/api/tickets` | 列出報修單 | 200 |
| GET | `/api/tickets/{ticketId}` | 查詢單筆 | 200；不存在為 404 |
| POST | `/api/tickets` | 建立報修單 | 201；欄位錯誤為 400 |
| PUT | `/api/tickets/{ticketId}` | 更新報修人名稱、問題和狀態 | 200；不存在為 404；欄位錯誤為 400 |
| DELETE | `/api/tickets/{ticketId}` | 刪除報修單 | 204；不存在為 404 |

API 回應包含對外編號、報修人名稱、類別、問題、狀態與伺服器產生的 `createdAt`，不暴露資料庫內部主鍵。建立時需提供報修人名稱、類別與問題描述，狀態預設為「待處理」，編號由資料庫自動遞增主鍵產生。建立與更新請求不能指定 `createdAt`：

~~~powershell
curl.exe -X POST http://localhost:8081/api/tickets `
  -H "Content-Type: application/json" `
  -d '{"requesterName":"王小明","category":"NETWORK","issue":"VPN 無法連線"}'

curl.exe http://localhost:8081/api/tickets
curl.exe http://localhost:8081/api/tickets/IT-004

curl.exe -X PUT http://localhost:8081/api/tickets/IT-004 `
  -H "Content-Type: application/json" `
  -d '{"requesterName":"王小明","issue":"VPN 間歇性斷線","status":"處理中"}'

curl.exe -X DELETE http://localhost:8081/api/tickets/IT-004
~~~

單筆回應範例：

~~~json
{"ticketId":"IT-004","requesterName":"王小明","category":"網路","issue":"VPN 無法連線","status":"待處理","createdAt":"2026-11-19T14:30:00"}
~~~

## MCP 報修單工具

MCP Server 透過 Streamable HTTP 提供以下工具；每次讀取或更新都經由 `TicketService` 存取 MySQL：

- `getTicketStatus(ticketId)`：查詢問題與目前狀態，保留原有回應格式。
- `createTicket(requesterName, category, issue)`：只有使用者明確要求建立、提交或新增報修單時建立；缺少姓名或問題描述時先詢問。AI 依問題描述選擇九種合法 enum 代碼之一。成功結果包含伺服器產生的報修單編號、類別、狀態與台北建立時間。
- `updateTicketStatus(ticketId, status)`：使用者明確要求時更新狀態。
- `updateTicketIssue(ticketId, issue)`：使用者明確要求時更新問題描述。
- `updateTicketRequesterName(ticketId, requesterName)`：使用者明確要求時更新報修人名稱。

若建立或更新缺少必要內容，應先詢問；編號不存在時工具會回覆查無此單，不會回報成功。聊天只在收到工具成功回覆後才宣告建立成功，並以成功回覆中的編號為準。前端列表將 `createdAt` 以 `Asia/Taipei` 顯示日期及時間，缺失或無效資料顯示「未提供」。

## 獨立前端

前端預設開啟「AI 問答」，提供常見問題快速索引與連續對話。點選索引會將範例帶入輸入框，可修改後再送出。每則回答顯示來源檔名或無來源提示。若排查後仍需協助，可從一般排查回答下方按「建立報修單」，帶入剛才的問題描述；仍需填入報修人名稱並選擇類別才會送出。前端判定為報修進度查詢時會省略建單按鈕。切換頁面或重新整理會開始新對話，聊天紀錄不會保存在瀏覽器。報修單頁可依編號、報修人或問題描述搜尋，以狀態篩選，並建立、編輯、刪除報修單；刪除前會要求確認。

先依上方步驟啟動 `mcp-ticket-server`（預設 `8081`）與 `helpdesk-ai`（預設 `8080`），再另開終端機：

~~~powershell
cd frontend
npm ci
npm run dev
~~~

開啟 Vite 預設網址 `http://localhost:5173`。開發伺服器會將 `/api/chat` 轉送到 `http://localhost:8080`，並將 `/api/tickets` 轉送到 `http://localhost:8081`。

建立 production build：

~~~powershell
cd frontend
npm run build
~~~

獨立部署前端時，可參考 `frontend/.env.example`，在建置前設定 `VITE_HELPDESK_API_BASE_URL` 與 `VITE_TICKETS_API_BASE_URL`（例如設為各自後端的來源網址）；留空時使用相對路徑。跨來源部署需由後端或反向代理提供正確的 CORS 設定，尤其 ticket server 目前沒有明確的 CORS 設定。

## 聊天 REST API

送出 POST http://localhost:8080/api/chat，標頭為 Content-Type: application/json。`conversationId` 是選填欄位；相同 ID 可沿用最近 12 輪對話（記憶體最多保留 500 個對話），不傳時每次請求仍維持舊版獨立提問行為。聊天頁會在目前頁面使用隨機 ID，重新整理或離開頁面即開始新對話。

~~~json
{"question":"公司的 VPN 無法連線怎麼辦？","conversationId":"可選的隨機對話識別碼"}
~~~

回應格式：

~~~json
{
  "answer": "原有回答內容",
  "sources": ["01_network_vpn_cannot_connect.txt"],
  "toolsUsed": [],
  "retrievedChunks": [
    {
      "sourceFile": "01_network_vpn_cannot_connect.txt",
      "chunkId": "01_network_vpn_cannot_connect.txt:0001",
      "score": 0.82,
      "selectedForContext": true
    }
  ]
}
~~~

`sources` 列出送入模型上下文的來源檔；`toolsUsed` 列出本次請求實際呼叫的工具名稱，沒有時為 `[]`。`retrievedChunks` 記錄每個檢索片段的來源、片段 ID、相似度分數與是否送入模型上下文。片段 ID 由來源檔名和文件內序號組成，重啟後仍可對照同一片段。

也可測試：

- {"question":"幫我查詢 IT-001 報修單進度"}
- {"question":"我的 VPN 無法連線，而且 IT-001 報修單目前處理到哪？"}
- {"question":"幫我查 IT-999"}

## 測試與 RAG 驗收

`helpdesk-ai/src/test/resources/rag-acceptance-cases.json` 保存 20 道驗收題、預期文件、預期工具與檢查重點。AI 服務與 MCP 報修服務啟動後，在專案根目錄執行：

~~~powershell
node helpdesk-ai/scripts/run-rag-acceptance.mjs
~~~

若只需驗證特定案例，可執行 `node helpdesk-ai/scripts/run-rag-acceptance.mjs --ids A01,A02`。AI 使用其他連接埠時，可用 `--url` 指定服務根網址。後端單元測試可在專案根目錄執行 `mvn test`。

測試紀錄會寫到 `helpdesk-ai/target/rag-acceptance-results-<timestamp>.json`，包含題目、預期來源、實際檢索片段與分數、回答、引用來源、工具名稱及四項評分欄位。檢索結果會自動收集；回答正確性、證據使用、知識邊界與工具判斷需人工依測試表評分。D20 暫列 `NOT_TESTED`，因目前 API 尚無使用者身份和工單授權功能。

## MVP 限制

SimpleVectorStore 僅保存在程序記憶體，重啟時會重新載入文件、切塊及建立向量。Spring AI Transformers 的 ONNX 實作使用 mean pooling；BAAI/bge-large-zh-v1.5 官方範例採 CLS pooling。此 MVP 保留 Spring AI starter 的標準 pooling 以維持最小架構，向量排序可能與官方參考實作不同。MCP Server 目前供本機開發，尚未加入身份驗證或授權。


