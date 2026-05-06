package com.styleauditor.checks;

import java.util.regex.Pattern;

public class PatternContrastCheck extends RegexCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b\u044d\u0442\u043e\\s+\u043d\u0435\\b.{0,120}?\\b\u044d\u0442\u043e\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS | Pattern.DOTALL
    );

    @Override
    protected String type() {
        return "Это не… Это…";
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
        return "Пункт 1: драматический контраст «это не… это…». Одиночный случай не страшен, опасно накопление.";
    }

    @Override
    protected double weight() {
        return 14;
    }
}

