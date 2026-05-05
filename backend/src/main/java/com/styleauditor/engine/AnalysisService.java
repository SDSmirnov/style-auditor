package com.styleauditor.engine;

import com.styleauditor.checks.PatternAsIfCheck;
import com.styleauditor.checks.PatternBecauseCheck;
import com.styleauditor.checks.PatternBoldMarkCheck;
import com.styleauditor.checks.PatternContrastCheck;
import com.styleauditor.checks.PatternDashOutsideDialogueCheck;
import com.styleauditor.checks.PatternEmotionLabelCheck;
import com.styleauditor.checks.PatternHookParagraphCheck;
import com.styleauditor.checks.PatternHyperboleCheck;
import com.styleauditor.checks.PatternMirrorSentenceCheck;
import com.styleauditor.checks.PatternMoreThanCheck;
import com.styleauditor.checks.PatternNotJustCheck;
import com.styleauditor.checks.PatternPompousComparisonCheck;
import com.styleauditor.checks.PatternPseudoAphorismCheck;
import com.styleauditor.checks.PatternRepeatedStartCheck;
import com.styleauditor.checks.PatternShortPhraseSeriesCheck;
import com.styleauditor.checks.PatternSmellCheckboxCheck;
import com.styleauditor.checks.PatternTripleEnumerationCheck;
import com.styleauditor.checks.PatternTwoAdjectivesCheck;
import com.styleauditor.checks.SmoothGenericWordsCheck;
import com.styleauditor.checks.SmoothLexicalPredictabilityCheck;
import com.styleauditor.checks.SmoothRhythmCheck;
import com.styleauditor.model.AnalysisResult;
import com.styleauditor.model.ChunkResult;
import com.styleauditor.model.Flag;
import com.styleauditor.model.Highlight;
import com.styleauditor.model.Summary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisService {
    private static final int CHUNK_TARGET = 1100;

    private final List<TextCheck> checks = List.of(
            new PatternContrastCheck(),
            new PatternBecauseCheck(),
            new PatternNotJustCheck(),
            new PatternMoreThanCheck(),
            new PatternAsIfCheck(),
            new PatternPseudoAphorismCheck(),
            new PatternHyperboleCheck(),
            new PatternSmellCheckboxCheck(),
            new PatternPompousComparisonCheck(),
            new PatternTripleEnumerationCheck(),
            new PatternEmotionLabelCheck(),
            new PatternMirrorSentenceCheck(),
            new PatternHookParagraphCheck(),
            new PatternBoldMarkCheck(),
            new PatternShortPhraseSeriesCheck(),
            new PatternRepeatedStartCheck(),
            new PatternTwoAdjectivesCheck(),
            new PatternDashOutsideDialogueCheck(),
            new SmoothLexicalPredictabilityCheck(),
            new SmoothRhythmCheck(),
            new SmoothGenericWordsCheck()
    );

    public AnalysisResult analyze(String text) {
        List<String> chunkTexts = AnalyzerUtils.splitChunks(text, CHUNK_TARGET);
        List<ChunkResult> chunks = new ArrayList<>();

        for (int i = 0; i < chunkTexts.size(); i++) {
            chunks.add(analyzeChunk(i, chunkTexts.get(i)));
        }

        double averageRisk = chunks.stream().mapToDouble(ChunkResult::riskScore).average().orElse(0);
        double avgSentenceLength = chunks.stream().mapToDouble(ChunkResult::avgSentenceLength).average().orElse(0);
        double avgSentenceStd = chunks.stream().mapToDouble(ChunkResult::sentenceLengthStd).average().orElse(0);

        int suspicious = (int) chunks.stream().filter(chunk -> chunk.riskScore() >= 35).count();
        int strong = (int) chunks.stream().filter(chunk -> chunk.riskScore() >= 60).count();

        String label = averageRisk < 30
                ? "низкая сглаженность"
                : averageRisk < 60
                  ? "умеренная сглаженность"
                  : "высокая сглаженность";

        String verdict = averageRisk < 30
                ? "Формально текст выглядит достаточно живым. Единичные подсветки стоит проверять только по уместности."
                : averageRisk < 60
                  ? "Есть зоны сглаженности и совпадения с чек-листом паттерна. Это не доказательство ИИ, а список мест для ручной редактуры."
                  : "Много формально сглаженных зон. Стоит проверить повторяющиеся приёмы, ритм и шаблонные связки.";

        Summary summary = new Summary(
                chunks.size(),
                suspicious,
                strong,
                AnalyzerUtils.round(averageRisk),
                AnalyzerUtils.round(avgSentenceLength),
                AnalyzerUtils.round(avgSentenceStd),
                AnalyzerUtils.buildProblemStats(chunks)
        );

        return new AnalysisResult(
                "Единый анализ",
                AnalyzerUtils.round(averageRisk),
                label,
                verdict,
                summary,
                chunks
        );
    }

    private ChunkResult analyzeChunk(int index, String chunkText) {
        List<String> sentences = AnalyzerUtils.splitSentences(chunkText);
        List<int[]> positions = AnalyzerUtils.sentencePositions(chunkText);
        List<Integer> lengths = AnalyzerUtils.sentenceLengths(sentences);

        double avg = lengths.stream().mapToInt(value -> value).average().orElse(0);
        double std = AnalyzerUtils.std(lengths, avg);

        ChunkContext context = new ChunkContext(index, chunkText, sentences, positions, lengths);

        List<Flag> flags = new ArrayList<>();
        List<Highlight> highlights = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        double score = 0;
        double lexical = 0;

        for (TextCheck check : checks) {
            CheckResult result = check.check(context);

            flags.addAll(result.flags());
            highlights.addAll(result.highlights());
            suggestions.addAll(result.suggestions());
            score += result.score();

            lexical = Math.max(lexical, result.metric("lexicalPredictability"));
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Сильных формальных проблем не найдено.");
        }

        double rhythm = rhythmMonotony(lengths, std);
        double finalScore = Math.min(100, Math.max(0, score));

        return new ChunkResult(
                index,
                chunkText,
                chunkText.length(),
                sentences.size(),
                AnalyzerUtils.round(avg),
                AnalyzerUtils.round(std),
                AnalyzerUtils.round(lexical),
                AnalyzerUtils.round(rhythm),
                AnalyzerUtils.round(finalScore),
                AnalyzerUtils.labelFor(finalScore),
                flags,
                AnalyzerUtils.mergeOverlappingHighlights(highlights),
                suggestions.stream().distinct().toList()
        );
    }

    private static double rhythmMonotony(List<Integer> lengths, double std) {
        if (lengths.size() < 3) {
            return 0;
        }

        double avg = lengths.stream().mapToInt(value -> value).average().orElse(0);
        double coefficient = avg == 0 ? 0 : std / avg;
        double score = 100 - (coefficient * 180);

        if (std < 2.0) {
            score += 24;
        } else if (std < 3.0) {
            score += 12;
        }

        return AnalyzerUtils.clamp(score, 0, 100);
    }
}