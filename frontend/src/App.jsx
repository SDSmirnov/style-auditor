import { useState } from "react";
import { Upload } from "lucide-react";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/analyze";

export default function App() {
  const [text, setText] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [activeChunk, setActiveChunk] = useState(null);

  async function analyze() {
    if (!text.trim()) return;

    setLoading(true);

    try {
      const res = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text })
      });

      if (!res.ok) {
        throw new Error(`Ошибка API: ${res.status}`);
      }

      const data = await res.json();
      setResult(data);
      setActiveChunk(null);
    } catch (e) {
      alert(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleFileUpload(event) {
    const file = event.target.files?.[0];
    if (!file) return;

    const content = await file.text();
    setText(content);
    setResult(null);
    setActiveChunk(null);
  }

  const shownChunks = result
      ? activeChunk === null
          ? result.chunks
          : result.chunks.filter((chunk) => chunk.index === activeChunk)
      : [];

  return (
      <main className="page">
        <h1>Стилистический аудитор</h1>

        <section className="inputPanel">
          <div className="toolbar">
            <label className="fileButton">
              <Upload size={18} />
              Загрузить .txt
              <input
                  type="file"
                  accept=".txt,text/plain"
                  onChange={handleFileUpload}
              />
            </label>

            <button onClick={analyze} disabled={loading || !text.trim()}>
              {loading ? "Проверяю..." : "Проверить"}
            </button>
          </div>

          <textarea
              value={text}
              onChange={(e) => {
                setText(e.target.value);
                setResult(null);
                setActiveChunk(null);
              }}
              placeholder="Вставьте текст или загрузите .txt..."
          />
        </section>

        <Legend />

        {result && (
            <>
              {/* ===== SUMMARY В РАМКЕ ===== */}
              <section className="summaryBox">
                <h2>{result.smoothnessLabel}</h2>

                <p className="summaryVerdict">
                  {result.verdict}
                </p>

                <p className="summaryNote">
                  Индекс отражает сглаженность текста, а не «силу ошибки».
                </p>
              </section>

              <section className="layout">
                <aside className="chunkList">
                  <h2>Чанки</h2>

                  {result.chunks.map((chunk) => (
                      <button
                          key={chunk.index}
                          className={`chunkButton ${riskClass(chunk.riskScore)} ${
                              activeChunk === chunk.index ? "active" : ""
                          }`}
                          onClick={() =>
                              setActiveChunk(
                                  activeChunk === chunk.index ? null : chunk.index
                              )
                          }
                      >
                        <span>#{chunk.index + 1}</span>
                        <b>индекс {chunk.riskScore}/100</b>
                        <small>{chunk.label}</small>
                      </button>
                  ))}
                </aside>

                <section className="chunks">
                  {shownChunks.map((chunk) => (
                      <article key={chunk.index} className="chunk">
                        <header className="chunkHeader">
                          <div>
                            <h3>Чанк #{chunk.index + 1}</h3>
                            <p className="meta">
                              {chunk.chars} симв. · {chunk.sentenceCount} предл. ·
                              средняя длина {chunk.avgSentenceLength} · разброс{" "}
                              {chunk.sentenceLengthStd}
                            </p>
                          </div>

                          <div className={`riskBadge ${riskClass(chunk.riskScore)}`}>
                            <small>индекс чанка</small>
                            <br />
                            <b>{chunk.riskScore}/100</b>
                          </div>
                        </header>

                        <div className="meters">
                          <Meter
                              label="Ожидаемость лексики"
                              value={chunk.lexicalPredictability}
                          />
                          <Meter
                              label="Монотонность ритма"
                              value={chunk.rhythmMonotony}
                          />
                        </div>

                        {chunk.flags?.length > 0 ? (
                            <div className="flags">
                              {chunk.flags.map((flag, index) => (
                                  <div
                                      key={index}
                                      className={`flag ${flag.severity || "medium"}`}
                                      style={flagStyle(flag.severity)}
                                  >
                                    <div>
                                      <b>{flag.type}</b>
                                      <span>{flag.count} найдено</span>
                                    </div>
                                    <p>{flag.comment}</p>
                                  </div>
                              ))}
                            </div>
                        ) : (
                            <div className="clean">
                              Сильных формальных флагов нет.
                            </div>
                        )}

                        {chunk.suggestions?.length > 0 && (
                            <div className="suggestions">
                              <h3>Что можно проверить</h3>
                              <ul>
                                {chunk.suggestions.map((suggestion, index) => (
                                    <li key={index}>{suggestion}</li>
                                ))}
                              </ul>
                            </div>
                        )}

                        <div className="textBlock">
                          {renderHighlightedText(chunk)}
                        </div>
                      </article>
                  ))}
                </section>
              </section>
            </>
        )}
      </main>
  );
}

function Legend() {
  return (
    <details className="legendSpoiler">
      <summary>Легенда</summary>
      <div className="legendBody">
        <section className="legendSection">
          <h4>Цвета подсветки</h4>
          <ul className="legendColors">
            <li><mark className="highlight low">низкий</mark> — слабый сигнал, одиночный случай не критичен</li>
            <li><mark className="highlight medium">средний</mark> — стоит обратить внимание при накоплении</li>
            <li><mark className="highlight high">высокий</mark> — характерный паттерн, рекомендуется правка</li>
          </ul>
        </section>

        <section className="legendSection">
          <h4>Паттерны</h4>
          <dl className="legendList">
            <dt>Пункт 1</dt><dd>«Это не… Это…» — драматический контраст. Одиночный случай не страшен, опасно накопление.</dd>
            <dt>Пункты 2, 9</dt><dd>Серия коротких фраз подряд или монтажная нарезка.</dd>
            <dt>Пункт 3</dt><dd>Тройное перечисление. Нормальный приём, но при избытке заметен.</dd>
            <dt>Пункт 4</dt><dd>Псевдоафоризм — обобщающая фраза в конце абзаца.</dd>
            <dt>Пункт 5</dt><dd>Эмоция-ярлык — абстрактное чувство в коротком отдельном предложении.</dd>
            <dt>Пункт 6</dt><dd>«Не потому что… а потому что…» — объяснительная связка. Шаблонна при повторе.</dd>
            <dt>Пункт 7</dt><dd>Зеркальное предложение — соседние фразы с перестановкой похожих слов.</dd>
            <dt>Пункт 8</dt><dd>«Как будто / словно» — сравнения. В хорроре норма, но частый повтор заметен.</dd>
            <dt>Пункт 11</dt><dd>Два прилагательных перед существительным через запятую.</dd>
            <dt>Пункт 12</dt><dd>Тире вне диалога — частые тире в авторской речи. Диалоговые тире игнорируются.</dd>
            <dt>Пункт 14</dt><dd>Гипербола — громкое усиление. Само по себе нормально, при избытке создаёт пафос.</dd>
            <dt>Пункт 15</dt><dd>«Не просто… а…» — усилительная конструкция. Шаблонна при накоплении.</dd>
            <dt>Пункт 16</dt><dd>Абзац-крючок — абзац начинается одиночным словом.</dd>
            <dt>Пункт 17</dt><dd>Жирный текст внутри художественного фрагмента.</dd>
            <dt>Пункт 18</dt><dd>Запах — сенсорное описание через обоняние. Проблемно, если встречается в каждой сцене.</dd>
            <dt>Пункт 19</dt><dd>Помпезное сравнение — тёмная образность. Работает дозированно.</dd>
            <dt>Пункт 20</dt><dd>«Нечто большее, чем…» — шаблонный оборот.</dd>
          </dl>
        </section>

        <section className="legendSection">
          <h4>Метрики сглаженности</h4>
          <dl className="legendList">
            <dt>S1 — Монотонный ритм</dt><dd>Предложения в чанке близки по длине. Подсвечивается весь фрагмент целиком.</dd>
            <dt>S2 — Общая оценочная лексика</dt><dd>Много размытых прилагательных (красивый, странный, тёмный и т.п.).</dd>
            <dt>S3 — Предсказуемость лексики</dt><dd>Высокая доля частотных, абстрактных или взаимозаменяемых слов.</dd>
          </dl>
        </section>
      </div>
    </details>
  );
}

function Meter({ label, value }) {
  const safeValue = Number.isFinite(Number(value)) ? Number(value) : 0;
  const width = Math.min(100, Math.max(0, safeValue));

  return (
      <div className="meter">
        <div className="meterTop">
          <span>{label}</span>
          <b>{safeValue}/100</b>
        </div>
        <div className="meterBar">
          <div style={{ width: `${width}%` }} />
        </div>
      </div>
  );
}

function renderHighlightedText(chunk) {
  const highlights = chunk.highlights || [];

  if (!highlights.length) {
    return chunk.text;
  }

  const sorted = [...highlights]
      .filter((h) => h.start >= 0 && h.end > h.start)
      .sort((a, b) => a.start - b.start);

  const parts = [];
  let cursor = 0;

  sorted.forEach((highlight, index) => {
    if (highlight.start < cursor) return;

    if (highlight.start > cursor) {
      parts.push(chunk.text.slice(cursor, highlight.start));
    }

    parts.push(
        <mark
            key={`${highlight.start}-${highlight.end}-${index}`}
            className={`highlight ${highlight.severity || "medium"}`}
            title={highlight.type}
        >
          {chunk.text.slice(highlight.start, highlight.end)}
        </mark>
    );

    cursor = highlight.end;
  });

  if (cursor < chunk.text.length) {
    parts.push(chunk.text.slice(cursor));
  }

  return parts;
}

function riskClass(score) {
  if (score >= 60) return "high";
  if (score >= 30) return "medium";
  return "low";
}

function flagStyle(severity) {
  if (severity === "high" || severity === "red") {
    return {
      background: "rgba(239, 68, 68, 0.14)",
      border: "1px solid rgba(239, 68, 68, 0.45)",
      color: "#111827"
    };
  }

  if (severity === "medium" || severity === "yellow") {
    return {
      background: "rgba(245, 158, 11, 0.18)",
      border: "1px solid rgba(245, 158, 11, 0.45)",
      color: "#111827"
    };
  }

  return {
    background: "rgba(34, 197, 94, 0.14)",
    border: "1px solid rgba(34, 197, 94, 0.45)",
    color: "#111827"
  };
}