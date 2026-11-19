import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const moduleRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const casesPath = join(moduleRoot, 'src/test/resources/rag-acceptance-cases.json');
const args = process.argv.slice(2);
const baseUrl = (valueAfter('--url') ?? process.env.HELPDESK_API_URL ?? 'http://localhost:8080').replace(/\/$/, '');
const outputArg = valueAfter('--output');
const outputPath = outputArg
  ? resolve(process.cwd(), outputArg)
  : join(moduleRoot, 'target', `rag-acceptance-results-${new Date().toISOString().replaceAll(':', '-')}.json`);
const selectedIds = valueAfter('--ids')?.split(',').map((id) => id.trim()).filter(Boolean);
const timeoutMs = Number(valueAfter('--timeout-ms') ?? 120000);
const cases = JSON.parse(await readFile(casesPath, 'utf8'));
const selectedCases = selectedIds ? cases.filter((testCase) => selectedIds.includes(testCase.test_id)) : cases;

if (selectedCases.length === 0) {
  throw new Error('No acceptance cases matched the requested test IDs.');
}

function valueAfter(flag) {
  const index = args.indexOf(flag);
  return index >= 0 ? args[index + 1] : undefined;
}

function emptyRecord(testCase, reviewNote) {
  return {
    test_id: testCase.test_id,
    capability: testCase.capability,
    question: testCase.question,
    expected_documents: testCase.expected_documents,
    optional_documents: testCase.optional_documents,
    expected_tools: testCase.expected_tools,
    checks: testCase.checks,
    actual_retrieved_chunks: [],
    retrieval_match: {
      required_documents_found: [],
      missing_required_documents: testCase.expected_documents,
      optional_documents_found: [],
      unexpected_documents: [],
    },
    actual_answer: '',
    cited_sources: [],
    tools_used: [],
    score: {
      retrieval: null,
      correctness: null,
      evidence: null,
      boundary_and_tools: null,
    },
    result: 'NOT_TESTED',
    review_note: reviewNote,
  };
}

const records = [];
for (const testCase of selectedCases) {
  if (testCase.skip) {
    records.push(emptyRecord(testCase, testCase.skip_reason));
    console.log(`${testCase.test_id}: NOT_TESTED (skipped)`);
    continue;
  }

  try {
    const response = await fetch(`${baseUrl}/api/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question: testCase.question }),
      signal: AbortSignal.timeout(timeoutMs),
    });
    const body = await response.json();
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${JSON.stringify(body)}`);
    }

    const actualChunks = (body.retrievedChunks ?? []).map((chunk) => ({
        source_file: chunk.sourceFile,
        chunk_id: chunk.chunkId,
        score: chunk.score ?? null,
        selected_for_context: Boolean(chunk.selectedForContext),
      }));
    const actualDocuments = [...new Set(actualChunks.map((chunk) => chunk.source_file))];
    const expectedDocuments = new Set(testCase.expected_documents);
    const optionalDocuments = new Set(testCase.optional_documents);
    const requiredFound = testCase.expected_documents.filter((source) => actualDocuments.includes(source));
    const optionalFound = testCase.optional_documents.filter((source) => actualDocuments.includes(source));
    records.push({
      ...emptyRecord(testCase, '模型回答與四項分數尚待人工檢視。'),
      actual_retrieved_chunks: actualChunks,
      retrieval_match: {
        required_documents_found: requiredFound,
        missing_required_documents: testCase.expected_documents.filter((source) => !actualDocuments.includes(source)),
        optional_documents_found: optionalFound,
        unexpected_documents: actualDocuments.filter((source) => !expectedDocuments.has(source) && !optionalDocuments.has(source)),
      },
      actual_answer: body.answer ?? '',
      cited_sources: body.sources ?? [],
      tools_used: body.toolsUsed ?? [],
    });
    console.log(`${testCase.test_id}: captured ${records.at(-1).actual_retrieved_chunks.length} chunk(s)`);
  } catch (error) {
    records.push(emptyRecord(testCase, `API 呼叫失敗：${error instanceof Error ? error.message : String(error)}`));
    console.log(`${testCase.test_id}: NOT_TESTED (API request failed)`);
  }
}

await mkdir(dirname(outputPath), { recursive: true });
await writeFile(outputPath, `${JSON.stringify(records, null, 2)}\n`, 'utf8');
console.log(`Saved ${records.length} test record(s) to ${outputPath}`);
console.log('Scores and PASS/FAIL decisions remain NOT_TESTED until a reviewer fills in the rubric.');
