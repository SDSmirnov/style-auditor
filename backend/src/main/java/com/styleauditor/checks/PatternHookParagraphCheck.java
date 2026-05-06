package com.styleauditor.checks;

import com.styleauditor.engine.CheckResult;
import com.styleauditor.engine.ChunkContext;
import com.styleauditor.engine.TextCheck;
import com.styleauditor.model.Highlight;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternHookParagraphCheck implements TextCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "(^|\\R)\\s*[А-ЯЁA-Z][а-яёa-z]{2,15}\\.",
            Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS
    );

    private static final Set<String> SECTION_TITLES = Set.of(
            "пролог", "эпилог", "предисловие", "послесловие",
            "введение", "заключение", "приложение", "содержание",
            "глава", "часть", "раздел", "примечание", "продолжение"
    );

    @Override
    public CheckResult check(ChunkContext context) {
        CheckResult result = new CheckResult();
        List<Highlight> highlights = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(context.text());

        while (matcher.find()) {
            int start = matcher.start();

            while (start < matcher.end() && !Character.isLetter(context.text().charAt(start))) {
                start++;
            }

            String word = context.text().substring(start, matcher.end() - 1).toLowerCase(Locale.ROOT);
            if (SECTION_TITLES.contains(word)) continue;

            highlights.add(new Highlight(start, matcher.end(), "Абзац-крючок", "medium"));
        }

        if (!highlights.isEmpty()) {
            result.addHighlights(highlights)
                    .addFlag(
                            "Абзац-крючок",
                            "medium",
                            highlights.size(),
                            "Пункт 16: абзац начинается одиночным словом-ориентиром.",
                            8
                    );
        }

        return result;
    }
}

