package com.styleauditor.checks;

import java.util.regex.Pattern;

public class PatternNotJustCheck extends RegexCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b\u043d\u0435\\s+\u043f\u0440\u043e\u0441\u0442\u043e\\b.{0,160}?\\b\u0430\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS | Pattern.DOTALL
    );

    @Override
    protected String type() {
        return "Не просто…, а…";
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
        return "Пункт 15: конструкция «не просто…, а…». При накоплении выглядит как усилитель.";
    }

    @Override
    protected double weight() {
        return 14;
    }
}

