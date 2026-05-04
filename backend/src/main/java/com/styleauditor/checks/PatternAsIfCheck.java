package com.styleauditor.checks;

import java.util.regex.Pattern;

public class PatternAsIfCheck extends RegexCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b(\u043a\u0430\u043a\\s+\u0431\u0443\u0434\u0442\u043e|\u0431\u0443\u0434\u0442\u043e|\u0441\u043b\u043e\u0432\u043d\u043e)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    @Override
    protected String type() {
        return "Как будто / будто / словно";
    }

    @Override
    protected String severity() {
        return "medium";
    }

    @Override
    protected Pattern pattern() {
        return PATTERN;
    }

    @Override
    protected String comment() {
        return "Пункт 8: сравнения через «будто/словно».";
    }

    @Override
    protected double weight() {
        return 6;
    }
}

