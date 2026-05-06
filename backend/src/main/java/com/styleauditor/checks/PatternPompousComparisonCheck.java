package com.styleauditor.checks;

import java.util.regex.Pattern;

public class PatternPompousComparisonCheck extends RegexCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b\u043a\u0430\u043a\\s+(\u0433\u0432\u043e\u0437\u0434\u044c|\u043a\u043e\u0441\u0442\u044c|\u044f\u0437\u044b\u043a|\u0441\u043b\u0438\u0437\u043d\u044f\u043a|\u0433\u043b\u0430\u0437|\u043a\u043e\u043b\u043e\u0434\u0435\u0446|\u0442\u0440\u0443\u043f|\u043c\u043e\u0433\u0438\u043b\u0430|\u0440\u0436\u0430\u0432\u044b\u0439\\s+\u0433\u0432\u043e\u0437\u0434\u044c|\u043d\u043e\u0436|\u0440\u0430\u043d\u0430|\u043a\u043b\u044f\u043a\u0441\u0430|\u043c\u043e\u043d\u0435\u0442\u0430|\u0432\u043e\u0434\u044f\u043d\u043e\u0439\\s+\u0434\u044b\u043c\u043a\u0435)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS
    );

    @Override
    protected String type() {
        return "Пафосные сравнения";
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
        return "Пункт 19: сильная мрачная образность. Работает дозированно.";
    }

    @Override
    protected double weight() {
        return 8;
    }
}

