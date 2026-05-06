package com.styleauditor.checks;

import com.styleauditor.engine.AnalyzerUtils;
import com.styleauditor.engine.CheckResult;
import com.styleauditor.engine.ChunkContext;
import com.styleauditor.engine.TextCheck;
import com.styleauditor.model.Highlight;

import java.util.List;
import java.util.regex.Pattern;

public class PatternTwoAdjectivesCheck implements TextCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b[а-яё]+(?:ый|ий|ой|ая|яя|ое|ее|ые|ие|ым|им|ой|ою|ыми|ими)\\b\\s*,\\s*\\b[а-яё]+(?:ый|ий|ой|ая|яя|ое|ее|ые|ие|ым|им|ой|ою|ыми|ими)\\b\\s+\\b[а-яё]{3,}\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS
    );

    @Override
    public CheckResult check(ChunkContext context) {
        CheckResult result = new CheckResult();
        List<Highlight> found = AnalyzerUtils.regexHighlights(context.text(), PATTERN, "Два определения перед существительным", "medium");

        if (!found.isEmpty()) {
            String severity = found.size() >= 3 ? "high" : "medium";
            List<Highlight> highlights = found.stream()
                    .map(highlight -> new Highlight(highlight.start(), highlight.end(), highlight.type(), severity))
                    .toList();

            result.addHighlights(highlights)
                    .addFlag(
                            "Переизбыток определений",
                            severity,
                            highlights.size(),
                            "Пункт 11: рядом с существительным стоят два определения через запятую.",
                            9
                    );
        }

        return result;
    }
}

