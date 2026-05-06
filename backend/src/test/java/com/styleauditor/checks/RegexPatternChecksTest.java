package com.styleauditor.checks;

import com.styleauditor.engine.AnalyzerUtils;
import com.styleauditor.engine.CheckResult;
import com.styleauditor.engine.ChunkContext;
import com.styleauditor.engine.TextCheck;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for all RegexCheck subclasses — one positive and one negative case per check.
 */
class RegexPatternChecksTest {

    private CheckResult run(TextCheck check, String text) {
        var parsed = AnalyzerUtils.parseSentences(text);
        var ctx = new ChunkContext(0, text, parsed.sentences(), parsed.positions(),
                AnalyzerUtils.sentenceLengths(parsed.sentences()));
        return check.check(ctx);
    }

    // ── PatternAsIfCheck ────────────────────────────────────────────────────

    @Test
    void asIf_detectedWhenPresent() {
        var result = run(new PatternAsIfCheck(),
                "Он шёл будто во сне. Словно призрак появился из темноты. Это было как будто наяву.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void asIf_notDetectedWhenAbsent() {
        var result = run(new PatternAsIfCheck(),
                "Он шёл по улице и думал о завтрашнем дне. Всё казалось спокойным.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternBecauseCheck ─────────────────────────────────────────────────

    @Test
    void because_detectedWhenPresent() {
        var result = run(new PatternBecauseCheck(),
                "Он ушёл не потому что устал, а потому что понял правду.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void because_notDetectedWhenAbsent() {
        var result = run(new PatternBecauseCheck(),
                "Он ушёл потому что устал. Дорога была долгой.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternContrastCheck ────────────────────────────────────────────────

    @Test
    void contrast_detectedWhenPresent() {
        var result = run(new PatternContrastCheck(),
                "Это не страх — это нечто совсем другое и непонятное.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void contrast_notDetectedWhenAbsent() {
        var result = run(new PatternContrastCheck(),
                "Страх был настоящим. Он не собирался уходить.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternHyperboleCheck ───────────────────────────────────────────────

    @Test
    void hyperbole_detectedWhenPresent() {
        var result = run(new PatternHyperboleCheck(),
                "Оглушающий грохот разнёсся по всей улице. Невыносимый холод пробрал до костей.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void hyperbole_notDetectedWhenAbsent() {
        var result = run(new PatternHyperboleCheck(),
                "Дима закрыл дверь и прошёл к столу. На улице шёл дождь.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternMoreThanCheck ────────────────────────────────────────────────

    @Test
    void moreThan_detectedWhenPresent() {
        var result = run(new PatternMoreThanCheck(),
                "Это было нечто большее, чем просто страх и тревога.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void moreThan_notDetectedWhenAbsent() {
        var result = run(new PatternMoreThanCheck(),
                "Страх был настоящим. Дима не мог объяснить его словами.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternNotJustCheck ─────────────────────────────────────────────────

    @Test
    void notJust_detectedWhenPresent() {
        var result = run(new PatternNotJustCheck(),
                "Он был не просто другом, а настоящим братом для него.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void notJust_notDetectedWhenAbsent() {
        var result = run(new PatternNotJustCheck(),
                "Он был хорошим другом. Всегда помогал в трудную минуту.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternPompousComparisonCheck ───────────────────────────────────────

    @Test
    void pompous_detectedWhenPresent() {
        var result = run(new PatternPompousComparisonCheck(),
                "Слова вонзились как нож прямо в самое сердце.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void pompous_notDetectedWhenAbsent() {
        var result = run(new PatternPompousComparisonCheck(),
                "Дима закрыл книгу и положил её на полку.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternPseudoAphorismCheck ──────────────────────────────────────────

    @Test
    void pseudoAphorism_detectedWhenPresent() {
        var result = run(new PatternPseudoAphorismCheck(),
                "После этого мир уже не будет прежним для него.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void pseudoAphorism_notDetectedWhenAbsent() {
        var result = run(new PatternPseudoAphorismCheck(),
                "Дима вышел на улицу. Дождь уже закончился.");
        assertThat(result.flags()).isEmpty();
    }

    // ── PatternSmellCheckboxCheck ───────────────────────────────────────────

    @Test
    void smell_detectedWhenPresent() {
        var result = run(new PatternSmellCheckboxCheck(),
                "В комнате пахло старыми книгами и пылью.");
        assertThat(result.flags()).isNotEmpty();
    }

    @Test
    void smell_notDetectedWhenAbsent() {
        var result = run(new PatternSmellCheckboxCheck(),
                "Дима сел за стол и открыл ноутбук. Окно было закрыто.");
        assertThat(result.flags()).isEmpty();
    }
}
