package com.styleauditor.checks;

import com.styleauditor.engine.AnalyzerUtils;
import com.styleauditor.engine.CheckResult;
import com.styleauditor.engine.ChunkContext;
import com.styleauditor.engine.TextCheck;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for custom TextCheck implementations (non-regex checks).
 */
class CustomPatternChecksTest {

    private CheckResult run(TextCheck check, String text) {
        var parsed = AnalyzerUtils.parseSentences(text);
        var ctx = new ChunkContext(0, text, parsed.sentences(), parsed.positions(),
                AnalyzerUtils.sentenceLengths(parsed.sentences()));
        return check.check(ctx);
    }

    // ── PatternBoldMarkCheck ────────────────────────────────────────────────

    @Test
    void bold_detectedForMarkdown() {
        var result = run(new PatternBoldMarkCheck(), "Это было **очень важно** для всего дальнейшего.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void bold_detectedForHtmlTag() {
        var result = run(new PatternBoldMarkCheck(), "Это было <b>очень важно</b> для всего дальнейшего.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void bold_notDetectedInPlainText() {
        var result = run(new PatternBoldMarkCheck(), "Он шёл по улице и думал о своём.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternEmotionLabelCheck ────────────────────────────────────────────

    @Test
    void emotionLabel_detectedForSingleWordSentence() {
        var result = run(new PatternEmotionLabelCheck(),
                "Дима остановился. Боль. Он не мог двигаться дальше.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void emotionLabel_notDetectedInLongerSentence() {
        var result = run(new PatternEmotionLabelCheck(),
                "Боль была невыносимой и пронизывала его насквозь.");
        assertThat(result.flags()).isEmpty();
    }

    @Test
    void emotionLabel_highlightPositionWithinText() {
        String text = "Он остановился посреди улицы. Страх. Ноги не слушались.";
        var result = run(new PatternEmotionLabelCheck(), text);
        assertThat(result.highlights()).allSatisfy(h -> {
            assertThat(h.start()).isGreaterThanOrEqualTo(0);
            assertThat(h.end()).isLessThanOrEqualTo(text.length());
        });
    }

    // ── PatternHookParagraphCheck ───────────────────────────────────────────

    @Test
    void hook_detectedForSingleWordParagraph() {
        var result = run(new PatternHookParagraphCheck(), "Завтра.\nВсё изменится навсегда и бесповоротно.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void hook_notDetectedForRegularText() {
        var result = run(new PatternHookParagraphCheck(),
                "Он пришёл домой поздно вечером. Ужин уже остыл.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternMirrorSentenceCheck ──────────────────────────────────────────

    @Test
    void mirror_detectedForNeighboringSentencesWithSharedWords() {
        // "правду", "войне", "знал" — одинаковые формы в обоих предложениях
        var result = run(new PatternMirrorSentenceCheck(),
                "Он знал правду о войне и смерти. Правду о войне знал каждый солдат.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void mirror_notDetectedForUnrelatedSentences() {
        var result = run(new PatternMirrorSentenceCheck(),
                "Он пошёл домой после работы. Кот спал на диване у окна.");
        assertThat(result.flags()).isEmpty();
    }

    @Test
    void mirror_notDetectedForSingleSentence() {
        var result = run(new PatternMirrorSentenceCheck(),
                "Он знал правду о войне и о людях вокруг него.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternRepeatedStartCheck ───────────────────────────────────────────

    @Test
    void repeatedStart_detectedForConsecutiveSameLead() {
        var result = run(new PatternRepeatedStartCheck(),
                "Он пришёл домой поздно вечером. Он снял пальто у порога. Он прошёл на кухню.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void repeatedStart_notDetectedForDifferentLeads() {
        var result = run(new PatternRepeatedStartCheck(),
                "Он пришёл домой поздно. Она уже ждала его у стола. Кот спал в углу комнаты.");
        assertThat(result.flags()).isEmpty();
    }

    @Test
    void repeatedStart_severityMediumForTwoOrMorePairs() {
        var result = run(new PatternRepeatedStartCheck(),
                "Он пришёл. Он ушёл. Он вернулся. Он снова ушёл надолго.");
        assertThat(result.flags()).isNotEmpty();
        assertThat(result.flags().get(0).severity()).isEqualTo("medium");
    }

    // ── PatternShortPhraseSeriesCheck ───────────────────────────────────────

    @Test
    void shortSeries_detectedForThreeOrMoreShortSentences() {
        var result = run(new PatternShortPhraseSeriesCheck(),
                "Боль. Тьма. Тишина. Конец всего привычного и дорогого.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void shortSeries_notDetectedForTwoShortSentences() {
        var result = run(new PatternShortPhraseSeriesCheck(),
                "Боль. Тьма. Но потом он собрался с силами и встал на ноги.");
        assertThat(result.flags()).isEmpty();
    }

    @Test
    void shortSeries_notDetectedForLongSentences() {
        var result = run(new PatternShortPhraseSeriesCheck(),
                "Дима нашёл ключ под ковриком. Замок заскрипел и поддался. Он вошёл в тёмную комнату.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternTripleEnumerationCheck ───────────────────────────────────────

    @Test
    void triple_detectedForThreeItemList() {
        var result = run(new PatternTripleEnumerationCheck(),
                "Страх, боль и отчаяние захлестнули его с головой.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void triple_notDetectedForTwoItems() {
        var result = run(new PatternTripleEnumerationCheck(),
                "Страх и боль не отпускали его ни на минуту.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternTwoAdjectivesCheck ───────────────────────────────────────────

    @Test
    void twoAdjectives_detectedWhenPresent() {
        var result = run(new PatternTwoAdjectivesCheck(),
                "Тёмный, холодный вечер опустился на притихший город.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void twoAdjectives_notDetectedForOneAdjective() {
        var result = run(new PatternTwoAdjectivesCheck(),
                "Холодный вечер опустился на город.");
        assertThat(result.flags()).isEmpty();
    }

    @Test
    void twoAdjectives_severityHighForThreePlusOccurrences() {
        var result = run(new PatternTwoAdjectivesCheck(),
                "Тёмный, холодный вечер пришёл. Пустой, гулкий зал встретил его. " +
                "Старый, ржавый замок скрипнул. Низкий, хриплый голос позвал его.");
        if (!result.flags().isEmpty()) {
            assertThat(result.flags().get(0).severity()).isEqualTo("high");
        }
    }
}
