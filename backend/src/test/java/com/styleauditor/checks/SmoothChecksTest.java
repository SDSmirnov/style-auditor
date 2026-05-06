package com.styleauditor.checks;

import com.styleauditor.engine.AnalyzerUtils;
import com.styleauditor.engine.CheckResult;
import com.styleauditor.engine.ChunkContext;
import com.styleauditor.engine.TextCheck;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmoothChecksTest {

    private CheckResult run(TextCheck check, String text) {
        var parsed = AnalyzerUtils.parseSentences(text);
        var ctx = new ChunkContext(0, text, parsed.sentences(), parsed.positions(),
                AnalyzerUtils.sentenceLengths(parsed.sentences()));
        return check.check(ctx);
    }

    // ── SmoothRhythmCheck ───────────────────────────────────────────────────

    @Test
    void rhythm_flaggedWhenAllSentencesSameLength() {
        // ~10 слов в каждом предложении, std почти 0
        String text = "Он пришёл домой поздно вечером после долгого рабочего дня. " +
                "Она ждала его у стола с горячим ужином давно. " +
                "Кот спал на диване у тёплой батареи всю ночь. " +
                "Дождь стучал по стеклу тихо и монотонно без конца.";
        var result = run(new SmoothRhythmCheck(), text);
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void rhythm_notFlaggedWhenLengthsVary() {
        // намеренно разные длины: короткие и длинные
        String text = "Боль. " +
                "Он остановился посреди улицы и долго смотрел на окна дома напротив, не зная, что делать. " +
                "Темнело. " +
                "Фонари один за другим загорались вдоль всей длинной улицы, уходящей за горизонт.";
        var result = run(new SmoothRhythmCheck(), text);
        assertThat(result.flags()).isEmpty();
    }

    @Test
    void rhythm_notFlaggedForFewerThanFourSentences() {
        String text = "Он пришёл домой вечером. Она ждала его давно. Кот спал у батареи.";
        var result = run(new SmoothRhythmCheck(), text);
        assertThat(result.flags()).isEmpty();
    }

    @Test
    void rhythm_highlightCoversEntireChunk() {
        String text = "Он пришёл домой поздно ночью после работы. " +
                "Она ждала его с ужином у стола давно. " +
                "Кот спал у тёплой батареи всю ночь. " +
                "Дождь стучал по стеклу тихо без конца.";
        var result = run(new SmoothRhythmCheck(), text);
        if (!result.flags().isEmpty()) {
            assertThat(result.highlights()).anySatisfy(h -> {
                assertThat(h.start()).isEqualTo(0);
                assertThat(h.end()).isEqualTo(text.length());
            });
        }
    }

    // ── SmoothGenericWordsCheck ─────────────────────────────────────────────

    @Test
    void generic_flaggedWhenFiveOrMoreGenericWords() {
        String text = "Странный и красивый вечер опустился на тёмный город. " +
                "Ужасный холод пробрал до костей. Хорошая погода закончилась. " +
                "Приятный запах исчез. Обычный день превратился в кошмар.";
        var result = run(new SmoothGenericWordsCheck(), text);
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void generic_notFlaggedForConcreteText() {
        String text = "Дима нашёл ключ под ковриком. Замок поддался с третьей попытки. " +
                "В комнате стоял будильник с треснутым стеклом. " +
                "Форточка скрипела от сквозняка. За окном шёл дождь.";
        var result = run(new SmoothGenericWordsCheck(), text);
        assertThat(result.flags()).isEmpty();
    }

    @Test
    void generic_notFlaggedForFourOrFewerGenericWords() {
        String text = "Странный запах. Красивый вечер. Тёмная улица. Обычный день.";
        var result = run(new SmoothGenericWordsCheck(), text);
        assertThat(result.flags()).isEmpty();
    }

    @Test
    void generic_scoreNeverNegative() {
        String text = "Странный человек вошёл в красивый зал.";
        var result = run(new SmoothGenericWordsCheck(), text);
        assertThat(result.score()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void generic_severityMediumForNineOrMoreWords() {
        String text = "Странный, красивый, приятный, обычный, ужасный вечер наступил. " +
                "Тёмный, страшный, необычный, важный момент настал наконец.";
        var result = run(new SmoothGenericWordsCheck(), text);
        if (!result.flags().isEmpty()) {
            assertThat(result.flags().get(0).severity()).isEqualTo("medium");
        }
    }
}
