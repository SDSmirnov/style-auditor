package com.styleauditor.checks;

import java.util.regex.Pattern;

public class PatternMoreThanCheck extends RegexCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b\u043d\u0435\u0447\u0442\u043e\\s+\u0431\u043e\u043b\u044c\u0448\u0435\u0435\\s*,?\\s+\u0447\u0435\u043c\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS
    );

    @Override
    protected String type() {
        return "Нечто большее, чем…";
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
        return "Пункт 20: шаблон «нечто большее, чем…».";
    }

    @Override
    protected double weight() {
        return 15;
    }
}

