package com.styleauditor.engine;

import com.styleauditor.model.ChunkResult;
import com.styleauditor.model.Flag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AnalyzerUtilsTest {

    // ── parseSentences ──────────────────────────────────────────────────────

    @Test
    void parseSentences_sentencesAndPositionsAlwaysSameSize() {
        String text = "Первое предложение. Второе предложение! Третье предложение?";
        var parsed = AnalyzerUtils.parseSentences(text);
        assertThat(parsed.sentences()).hasSameSizeAs(parsed.positions());
    }

    @Test
    void parseSentences_positionsPointToCorrectText() {
        String text = "Привет мир. Как дела?";
        var parsed = AnalyzerUtils.parseSentences(text);
        for (int i = 0; i < parsed.sentences().size(); i++) {
            int[] pos = parsed.positions().get(i);
            String slice = text.substring(pos[0], pos[1]);
            assertThat(slice.strip()).isEqualTo(parsed.sentences().get(i));
        }
    }

    @Test
    void parseSentences_emptyTextReturnsEmpty() {
        var parsed = AnalyzerUtils.parseSentences("");
        assertThat(parsed.sentences()).isEmpty();
        assertThat(parsed.positions()).isEmpty();
    }

    @Test
    void parseSentences_blankSentencesSkipped() {
        String text = "Нормальное предложение.   \n\n  Второе.";
        var parsed = AnalyzerUtils.parseSentences(text);
        assertThat(parsed.sentences()).hasSize(2);
        assertThat(parsed.sentences()).hasSameSizeAs(parsed.positions());
    }

    // ── splitChunks ─────────────────────────────────────────────────────────

    @Test
    void splitChunks_shortTextIsOneChunk() {
        String text = "Короткий текст без разбивки.";
        assertThat(AnalyzerUtils.splitChunks(text, 1100)).hasSize(1);
    }

    @Test
    void splitChunks_noChunkIsBlank() {
        String text = "А".repeat(5000);
        List<String> chunks = AnalyzerUtils.splitChunks(text, 1100);
        assertThat(chunks).allMatch(c -> !c.isBlank());
    }

    @Test
    void splitChunks_allTextPreserved() {
        String text = "Первое предложение. Второе предложение. Третье предложение.";
        List<String> chunks = AnalyzerUtils.splitChunks(text, 20);
        int total = chunks.stream().mapToInt(String::length).sum();
        // допускаем потерю пробелов по краям чанков (strip)
        assertThat(total).isGreaterThanOrEqualTo(text.strip().length() - chunks.size() * 2);
    }

    @Test
    void splitChunks_nullTextReturnsSingleEmptyChunk() {
        // null и пустая строка → один пустой чанк (поведение по дизайну)
        assertThat(AnalyzerUtils.splitChunks(null, 1100)).containsExactly("");
    }

    // ── mergeOverlappingHighlights ───────────────────────────────────────────

    @Test
    void mergeHighlights_nonOverlappingStaysSeparate() {
        var h1 = new com.styleauditor.model.Highlight(0, 5, "A", "low");
        var h2 = new com.styleauditor.model.Highlight(10, 15, "B", "low");
        var merged = AnalyzerUtils.mergeOverlappingHighlights(List.of(h1, h2));
        assertThat(merged).hasSize(2);
    }

    @Test
    void mergeHighlights_overlappingMergesIntoOne() {
        var h1 = new com.styleauditor.model.Highlight(0, 10, "A", "low");
        var h2 = new com.styleauditor.model.Highlight(5, 15, "B", "low");
        var merged = AnalyzerUtils.mergeOverlappingHighlights(List.of(h1, h2));
        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).start()).isEqualTo(0);
        assertThat(merged.get(0).end()).isEqualTo(15);
        assertThat(merged.get(0).severity()).isEqualTo("high");
    }

    @Test
    void mergeHighlights_threeOverlappingAllTypesPresent() {
        var h1 = new com.styleauditor.model.Highlight(0, 10, "A", "low");
        var h2 = new com.styleauditor.model.Highlight(5, 15, "B", "low");
        var h3 = new com.styleauditor.model.Highlight(8, 20, "C", "low");
        var merged = AnalyzerUtils.mergeOverlappingHighlights(List.of(h1, h2, h3));
        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).type()).contains("A").contains("B").contains("C");
    }

    @Test
    void mergeHighlights_emptyListReturnsEmpty() {
        assertThat(AnalyzerUtils.mergeOverlappingHighlights(List.of())).isEmpty();
    }

    // ── clamp / round ────────────────────────────────────────────────────────

    @Test
    void clamp_belowMinReturnsMin() {
        assertThat(AnalyzerUtils.clamp(-5, 0, 100)).isEqualTo(0.0);
    }

    @Test
    void clamp_aboveMaxReturnsMax() {
        assertThat(AnalyzerUtils.clamp(150, 0, 100)).isEqualTo(100.0);
    }

    @Test
    void clamp_withinRangeUnchanged() {
        assertThat(AnalyzerUtils.clamp(42, 0, 100)).isEqualTo(42.0);
    }

    // ── wordCount ────────────────────────────────────────────────────────────

    @Test
    void wordCount_countsRussianWords() {
        assertThat(AnalyzerUtils.wordCount("Он пришёл домой поздно.")).isEqualTo(4);
    }

    @Test
    void wordCount_ignoresPunctuation() {
        assertThat(AnalyzerUtils.wordCount("Боль, тоска — и пустота.")).isEqualTo(4);
    }

    @Test
    void wordCount_emptyTextIsZero() {
        assertThat(AnalyzerUtils.wordCount("")).isEqualTo(0);
    }

    @Test
    void wordCount_countsNumbers() {
        assertThat(AnalyzerUtils.wordCount("Глава 1: начало")).isEqualTo(3);
    }

    // ── firstWord ────────────────────────────────────────────────────────────

    @Test
    void firstWord_returnsFirstToken() {
        assertThat(AnalyzerUtils.firstWord("Дима пришёл домой.")).isEqualTo("Дима");
    }

    @Test
    void firstWord_skipsPunctuationAtStart() {
        assertThat(AnalyzerUtils.firstWord("— Привет, как дела?")).isEqualTo("Привет");
    }

    @Test
    void firstWord_emptyTextReturnsEmpty() {
        assertThat(AnalyzerUtils.firstWord("")).isEmpty();
    }

    @Test
    void firstWord_punctuationOnlyReturnsEmpty() {
        assertThat(AnalyzerUtils.firstWord("... — ...")).isEmpty();
    }

    // ── significantWords ─────────────────────────────────────────────────────

    @Test
    void significantWords_filtersShortWords() {
        // "он", "в", "на" — короче 4 символов, должны быть отфильтрованы
        var words = AnalyzerUtils.significantWords("Он шёл в темноте.");
        assertThat(words).doesNotContain("он", "в", "шёл");
        assertThat(words).contains("темноте");
    }

    @Test
    void significantWords_filtersStopWords() {
        var words = AnalyzerUtils.significantWords("Когда они были здесь, всё началось.");
        assertThat(words).doesNotContain("когда", "были", "чтобы");
    }

    @Test
    void significantWords_returnsLowercasedForms() {
        var words = AnalyzerUtils.significantWords("Дима пришёл домой.");
        assertThat(words).contains("дима", "пришёл", "домой");
    }

    @Test
    void significantWords_emptyTextReturnsEmptySet() {
        assertThat(AnalyzerUtils.significantWords("")).isEmpty();
    }

    @Test
    void significantWords_deduplicatesWords() {
        var words = AnalyzerUtils.significantWords("Дима пришёл. Дима ушёл.");
        assertThat(words).containsOnlyOnce("дима");
    }

    // ── std ──────────────────────────────────────────────────────────────────

    @Test
    void std_zeroForIdenticalValues() {
        assertThat(AnalyzerUtils.std(List.of(5, 5, 5, 5), 5.0)).isEqualTo(0.0);
    }

    @Test
    void std_correctForKnownValues() {
        // values = [2, 4, 4, 4, 5, 5, 7, 9], avg = 5, std = 2.0
        double result = AnalyzerUtils.std(List.of(2, 4, 4, 4, 5, 5, 7, 9), 5.0);
        assertThat(result).isCloseTo(2.0, within(0.001));
    }

    @Test
    void std_emptyListReturnsZero() {
        assertThat(AnalyzerUtils.std(List.of(), 0.0)).isEqualTo(0.0);
    }

    @Test
    void std_nullListReturnsZero() {
        assertThat(AnalyzerUtils.std(null, 0.0)).isEqualTo(0.0);
    }

    // ── labelFor ─────────────────────────────────────────────────────────────

    @Test
    void labelFor_lowRiskBelow35() {
        assertThat(AnalyzerUtils.labelFor(0)).isEqualTo("низкий риск");
        assertThat(AnalyzerUtils.labelFor(34)).isEqualTo("низкий риск");
    }

    @Test
    void labelFor_moderateRisk35to59() {
        assertThat(AnalyzerUtils.labelFor(35)).isEqualTo("умеренный риск");
        assertThat(AnalyzerUtils.labelFor(59)).isEqualTo("умеренный риск");
    }

    @Test
    void labelFor_highRisk60andAbove() {
        assertThat(AnalyzerUtils.labelFor(60)).isEqualTo("высокий риск сглаженности");
        assertThat(AnalyzerUtils.labelFor(100)).isEqualTo("высокий риск сглаженности");
    }

    // ── buildProblemStats ─────────────────────────────────────────────────────

    @Test
    void buildProblemStats_aggregatesAcrossChunks() {
        var flag = new Flag("Тест", "medium", 3, "комментарий");
        var chunk1 = makeChunk(List.of(flag));
        var chunk2 = makeChunk(List.of(flag));
        var stats = AnalyzerUtils.buildProblemStats(List.of(chunk1, chunk2));

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).type()).isEqualTo("Тест");
        assertThat(stats.get(0).chunks()).isEqualTo(2);
        assertThat(stats.get(0).totalCount()).isEqualTo(6); // 3 + 3
    }

    @Test
    void buildProblemStats_sortedByTotalCountDesc() {
        var rare = new Flag("Редкий", "low", 1, "");
        var frequent = new Flag("Частый", "high", 10, "");
        var chunk = makeChunk(List.of(rare, frequent));
        var stats = AnalyzerUtils.buildProblemStats(List.of(chunk));

        assertThat(stats.get(0).type()).isEqualTo("Частый");
    }

    @Test
    void buildProblemStats_limitsTwelveEntries() {
        var flags = new java.util.ArrayList<Flag>();
        for (int i = 0; i < 20; i++) {
            flags.add(new Flag("Паттерн " + i, "low", i + 1, ""));
        }
        var stats = AnalyzerUtils.buildProblemStats(List.of(makeChunk(flags)));
        assertThat(stats).hasSizeLessThanOrEqualTo(12);
    }

    @Test
    void buildProblemStats_emptyChunksReturnsEmpty() {
        assertThat(AnalyzerUtils.buildProblemStats(List.of())).isEmpty();
    }

    private ChunkResult makeChunk(List<Flag> flags) {
        return new ChunkResult(0, "", 0, 0, 0, 0, 0, 0, 0, "низкий риск",
                flags, List.of(), List.of());
    }
}
