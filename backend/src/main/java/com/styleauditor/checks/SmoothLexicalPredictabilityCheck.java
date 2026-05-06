package com.styleauditor.checks;

import com.styleauditor.engine.AnalyzerUtils;
import com.styleauditor.engine.CheckResult;
import com.styleauditor.engine.ChunkContext;
import com.styleauditor.engine.TextCheck;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmoothLexicalPredictabilityCheck implements TextCheck {

    private static final Pattern WORD_PATTERN =
            Pattern.compile("[\\p{L}\\p{N}]+", Pattern.UNICODE_CHARACTER_CLASS);

    private static final Set<String> COMMON_WORDS = Set.of(
            "и", "в", "во", "не", "на", "он", "она", "они", "а", "но",
            "что", "как", "к", "с", "по", "из", "у", "за", "для", "это",
            "его", "её", "их", "был", "была", "были", "есть", "же", "там",
            "тут", "всё", "все", "или", "то", "от", "до", "потом", "сначала"
    );

    private static final Set<String> ABSTRACT_EMOTION_WORDS = Set.of(
            "страх", "тревога", "боль", "пустота", "отчаяние", "надежда",
            "радость", "злость", "обида", "паника", "ужас", "сомнение",
            "счастье", "одиночество", "вина", "стыд"
    );

    private static final Set<String> GENERIC_PERCEPTION_WORDS = Set.of(
            "понял", "поняла", "почувствовал", "почувствовала", "увидел",
            "увидела", "заметил", "заметила", "услышал", "услышала",
            "подумал", "подумала", "вспомнил", "вспомнила", "показалось",
            "казалось"
    );

    private static final Set<String> SMOOTH_CONNECTORS = Set.of(
            "словно", "будто", "вдруг", "неожиданно", "почему-то",
            "почти", "очень", "просто", "медленно", "тихо", "быстро"
    );

    @Override
    public CheckResult check(ChunkContext context) {
        double value = lexicalPredictability(context.text());

        CheckResult result = new CheckResult()
                .addMetric("lexicalPredictability", Math.round(value));

        if (value >= 45) {
            String severity = value >= 58 ? "high" : "medium";

            result.addFlag(
                            "Высокая предсказуемость лексики",
                            severity,
                            1,
                            "В чанке много частотных, абстрактных или универсальных слов. Это не признак ИИ сам по себе, а сигнал, что текст может звучать слишком ожидаемо.",
                            1
                    )
                    .addScore(Math.max(0, (value - 45) * 0.35))
                    .addSuggestion("Проверьте, можно ли заменить часть общих слов точными наблюдаемыми деталями: предмет, жест, звук, локальная особенность, конкретное действие.");
        }

        return result;
    }

    private double lexicalPredictability(String chunk) {
        if (chunk == null || chunk.isBlank()) {
            return 0;
        }

        Matcher matcher = WORD_PATTERN.matcher(chunk.toLowerCase(Locale.ROOT));

        int total = 0;
        int common = 0;
        int shortWords = 0;
        int longWords = 0;
        int abstractEmotion = 0;
        int perception = 0;
        int smoothConnectors = 0;
        Set<String> unique = new HashSet<>();

        while (matcher.find()) {
            String word = matcher.group();

            total++;
            unique.add(word);

            if (COMMON_WORDS.contains(word)) common++;
            if (word.length() <= 3) shortWords++;
            if (word.length() >= 11) longWords++;
            if (ABSTRACT_EMOTION_WORDS.contains(word)) abstractEmotion++;
            if (GENERIC_PERCEPTION_WORDS.contains(word)) perception++;
            if (SMOOTH_CONNECTORS.contains(word)) smoothConnectors++;
        }

        if (total == 0) return 0;

        double commonRatio = common * 100.0 / total;
        double shortRatio = shortWords * 100.0 / total;
        double uniqueRatio = unique.size() * 100.0 / total;
        double longRatio = longWords * 100.0 / total;
        double abstractRatio = abstractEmotion * 100.0 / total;
        double perceptionRatio = perception * 100.0 / total;
        double connectorRatio = smoothConnectors * 100.0 / total;

        double score =
                24
                        + commonRatio * 0.75
                        + shortRatio * 0.10
                        + abstractRatio * 2.0
                        + perceptionRatio * 1.6
                        + connectorRatio * 1.5
                        - uniqueRatio * 0.12
                        - longRatio * 0.30;

        return AnalyzerUtils.clamp(score, 0, 100);
    }
}