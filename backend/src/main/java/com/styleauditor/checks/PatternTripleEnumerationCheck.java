package com.styleauditor.checks;

import com.styleauditor.engine.AnalyzerUtils;
import com.styleauditor.engine.CheckResult;
import com.styleauditor.engine.ChunkContext;
import com.styleauditor.engine.TextCheck;
import com.styleauditor.model.Highlight;

import java.util.List;
import java.util.regex.Pattern;

public class PatternTripleEnumerationCheck implements TextCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b[\\p{L}]{3,}\\b\\s*,\\s*\\b[\\p{L}]{3,}\\b\\s+(?:и|или)\\s+\\b[\\p{L}]{3,}\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS
    );

    @Override
    public CheckResult check(ChunkContext context) {
        List<Highlight> found = AnalyzerUtils.regexHighlights(context.text(), PATTERN, "Тройное перечисление", "medium");
        CheckResult result = new CheckResult();

        if (!found.isEmpty()) {
            result.addHighlights(found)
                    .addFlag(
                            "Тройные перечисления",
                            "medium",
                            found.size(),
                            "Пункт 3: перечисления из трёх элементов. Это нормальный приём, но при избытке заметен.",
                            7
                    )
                    .addSuggestion("Проверьте, не слишком ли часто в тексте идут ровные тройки.");
        }

        return result;
    }
}

