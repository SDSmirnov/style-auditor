export default function SummaryBox({ result, isStale }) {
  const problems = result.summary?.commonProblems?.filter(p => p.totalCount > 0) ?? [];
  const maxCount = problems[0]?.totalCount ?? 1;

  return (
    <section className={`summaryBox${isStale ? " stale" : ""}`}>
      {isStale && (
        <p className="staleNotice">Текст изменён — результаты могут быть устаревшими.</p>
      )}
      <h2>{result.smoothnessLabel}</h2>
      <p className="summaryVerdict">{result.verdict}</p>
      <p className="summaryNote">Индекс отражает сглаженность текста, а не «силу ошибки».</p>

      {problems.length > 0 && (
        <div className="commonProblems">
          <p className="commonProblemsTitle">Частые паттерны</p>
          <ul className="problemList">
            {problems.map(p => (
              <li key={p.type} className="problemItem">
                <div className="problemBar" style={{ width: `${Math.round(p.totalCount / maxCount * 100)}%` }} />
                <span className="problemName">{p.type}</span>
                <span className="problemMeta">
                  <b>{p.totalCount}×</b>
                  <span>в {p.chunks} чанк{p.chunks % 10 === 1 && p.chunks % 100 !== 11 ? "е" : "ах"}</span>
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}
