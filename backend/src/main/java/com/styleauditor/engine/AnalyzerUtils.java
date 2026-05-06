package com.styleauditor.engine;

import com.styleauditor.model.ChunkResult;
import com.styleauditor.model.Highlight;
import com.styleauditor.model.ProblemStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class AnalyzerUtils {
    private static final Logger log = LoggerFactory.getLogger(AnalyzerUtils.class);
    private static final Pattern SENTENCE_PATTERN =
            Pattern.compile("[^.!?…]+[.!?…]+|[^.!?…]+$", Pattern.UNICODE_CHARACTER_CLASS);

    private AnalyzerUtils() {
    }

    public static List<String> splitChunks(String text, int targetSize) {
        String clean = text == null ? "" : text.strip();

        if (clean.isEmpty()) {
            return List.of("");
        }

        List<String> chunks = new ArrayList<>();
        int cursor = 0;

        while (cursor < clean.length()) {
            int end = Math.min(clean.length(), cursor + targetSize);

            if (end < clean.length()) {
                int paragraphBreak = clean.lastIndexOf("\n\n", end);
                int sentenceBreak = Math.max(
                        clean.lastIndexOf(". ", end),
                        Math.max(clean.lastIndexOf("! ", end), clean.lastIndexOf("? ", end))
                );

                if (paragraphBreak > cursor + targetSize / 2) {
                    end = paragraphBreak + 2;
                } else if (sentenceBreak > cursor + targetSize / 2) {
                    end = sentenceBreak + 2;
                }
            }

            chunks.add(clean.substring(cursor, end).strip());
            cursor = end;
        }

        return chunks.stream()
                .filter(s -> !s.isBlank())
                .toList();
    }

    public record ParsedSentences(List<String> sentences, List<int[]> positions) {}

    public static ParsedSentences parseSentences(String text) {
        List<String> sentences = new ArrayList<>();
        List<int[]> positions = new ArrayList<>();
        Matcher matcher = SENTENCE_PATTERN.matcher(text);

        while (matcher.find()) {
            String sentence = matcher.group().strip();
            if (sentence.isBlank()) continue;

            int start = matcher.start();
            int end = matcher.end();
            while (start < end && Character.isWhitespace(text.charAt(start))) start++;
            while (end > start && Character.isWhitespace(text.charAt(end - 1))) end--;

            sentences.add(sentence);
            positions.add(new int[]{start, end});
        }

        return new ParsedSentences(sentences, positions);
    }

    public static List<Integer> sentenceLengths(List<String> sentences) {
        return sentences.stream()
                .map(AnalyzerUtils::wordCount)
                .toList();
    }

    public static int wordCount(String text) {
        Matcher matcher = Pattern.compile("[\\p{L}\\p{N}]+", Pattern.UNICODE_CHARACTER_CLASS).matcher(text);
        int count = 0;

        while (matcher.find()) {
            count++;
        }

        return count;
    }

    public static String firstWord(String sentence) {
        Matcher matcher = Pattern.compile("[\\p{L}\\p{N}]+", Pattern.UNICODE_CHARACTER_CLASS).matcher(sentence);
        return matcher.find() ? matcher.group() : "";
    }

    public static Set<String> significantWords(String text) {
        Set<String> stop = Set.of(
                "он", "она", "они", "это", "не", "и", "а", "но", "его", "ее", "её", "их",
                "в", "на", "с", "к", "по", "как", "что", "для", "там", "тут", "был", "была",
                "были", "есть", "себя", "свой", "своя", "своё", "чтобы", "когда", "потом"
        );

        Matcher matcher = Pattern.compile("[\\p{L}]{4,}", Pattern.UNICODE_CHARACTER_CLASS)
                .matcher(text.toLowerCase(Locale.ROOT));

        Set<String> result = new HashSet<>();

        while (matcher.find()) {
            String word = matcher.group();

            if (!stop.contains(word)) {
                result.add(word);
            }
        }

        return result;
    }

    public static double std(List<Integer> values, double avg) {
        if (values == null || values.isEmpty()) {
            return 0;
        }

        double variance = 0;

        for (int value : values) {
            variance += Math.pow(value - avg, 2);
        }

        return Math.sqrt(variance / values.size());
    }

    public static int round(double value) {
        return (int) Math.round(value);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static String labelFor(double score) {
        if (score >= 60) {
            return "высокий риск сглаженности";
        }

        if (score >= 35) {
            return "умеренный риск";
        }

        return "низкий риск";
    }

    public static List<Highlight> regexHighlights(String text, Pattern pattern, String type, String severity) {
        List<Highlight> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            result.add(new Highlight(matcher.start(), matcher.end(), type, severity));
        }

        return result;
    }

    public static List<Highlight> mergeOverlappingHighlights(List<Highlight> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return List.of();
        }

        List<Highlight> sorted = new ArrayList<>(highlights);
        sorted.sort(Comparator.comparingInt(Highlight::start).thenComparingInt(Highlight::end));

        List<Highlight> result = new ArrayList<>();

        for (Highlight current : sorted) {
            if (current.start() < 0 || current.end() <= current.start()) {
                log.warn("Invalid highlight skipped: type='{}' start={} end={}", current.type(), current.start(), current.end());
                continue;
            }

            if (result.isEmpty()) {
                result.add(current);
                continue;
            }

            Highlight last = result.get(result.size() - 1);

            if (current.start() < last.end()) {
                // Если один и тот же участок попадает под несколько паттернов,
                // считаем это сильным сигналом и красим объединённую зону в красный.
                result.set(result.size() - 1, new Highlight(
                        last.start(),
                        Math.max(last.end(), current.end()),
                        last.type() + " / " + current.type(),
                        "high"
                ));
            } else {
                result.add(current);
            }
        }

        return result;
    }

    private static int severityRank(String severity) {
        return switch (severity) {
            case "high" -> 3;
            case "medium" -> 2;
            default -> 1;
        };
    }

    public static List<ProblemStat> buildProblemStats(List<ChunkResult> chunks) {
        Map<String, int[]> stats = new LinkedHashMap<>();

        for (ChunkResult chunk : chunks) {
            Set<String> seenInChunk = new HashSet<>();

            chunk.flags().forEach(flag -> {
                int[] arr = stats.computeIfAbsent(flag.type(), key -> new int[]{0, 0});
                arr[1] += flag.count();
                if (seenInChunk.add(flag.type())) arr[0]++;
            });
        }

        return stats.entrySet().stream()
                .map(entry -> new ProblemStat(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
                .sorted((a, b) -> Integer.compare(b.totalCount(), a.totalCount()))
                .limit(12)
                .collect(Collectors.toList());
    }
}

