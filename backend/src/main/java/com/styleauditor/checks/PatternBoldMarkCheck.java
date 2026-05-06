package com.styleauditor.checks;

import com.styleauditor.engine.AnalyzerUtils;
import com.styleauditor.engine.CheckResult;
import com.styleauditor.engine.ChunkContext;
import com.styleauditor.engine.TextCheck;
import com.styleauditor.model.Highlight;

import java.util.List;
import java.util.regex.Pattern;

public class PatternBoldMarkCheck implements TextCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "(\\*\\*[^*]+\\*\\*|<b>.*?</b>|<strong>.*?</strong>)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS | Pattern.DOTALL
    );

    @Override
    public CheckResult check(ChunkContext context) {
        List<Highlight> found = AnalyzerUtils.regexHighlights(context.text(), PATTERN, "Жирное выделение", "medium");
        CheckResult result = new CheckResult();

        if (!found.isEmpty()) {
            result.addHighlights(found)
                    .addFlag(
                            "Жирное выделение",
                            "medium",
                            found.size(),
                            "Пункт 17: выделение слов жирным внутри художественного текста.",
                            10
                    );
        }

        return result;
    }
}

