package com.styleauditor.checks;

import java.util.regex.Pattern;

public class PatternBecauseCheck extends RegexCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b\u043d\u0435\\s+\u043f\u043e\u0442\u043e\u043c\u0443\\s+\u0447\u0442\u043e\\b.{0,180}?\\b\u0430\\s+\u043f\u043e\u0442\u043e\u043c\u0443\\s+\u0447\u0442\u043e\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS | Pattern.DOTALL
    );

    @Override
    protected String type() {
        return "Не потому что…, а потому что…";
    }

    @Override
    protected String severity() {
        return "high";
    }

    @Override
    protected Pattern pattern() {
        return PATTERN;
    }

    @Override
    protected String comment() {
        return "Пункт 6: объяснительная связка, которая часто выглядит шаблонно при повторе.";
    }

    @Override
    protected double weight() {
        return 14;
    }
}

