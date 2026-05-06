package com.styleauditor.checks;

import java.util.regex.Pattern;

public class PatternSmellCheckboxCheck extends RegexCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b(\u043f\u0430\u0445\u043b\u043e|\u0437\u0430\u043f\u0430\u0445|\u0432\u043e\u043d\u044f\u043b\u043e|\u0432\\s+\u043d\u043e\u0441\\s+\u0443\u0434\u0430\u0440\u0438\u043b|\u043f\u043e\u0442\u044f\u043d\u0443\u043b\u043e\\s+[^.]{0,80}?(\u0441\u044b\u0440\u043e\u0441\u0442\u044c\u044e|\u0437\u0435\u043c\u043b[\u0435\u0451]\u0439|\u043f\u043b\u0435\u0441\u0435\u043d\u044c\u044e|\u0430\u043f\u0442\u0435\u043a\u043e\u0439|\u0431\u0438\u043d\u0442\u0430\u043c\u0438|\u0434\u044b\u043c\u043e\u043c|\u043d\u0430\u0432\u043e\u0437\u043e\u043c))\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS | Pattern.DOTALL
    );

    @Override
    protected String type() {
        return "Сенсорный чекбокс: запах";
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
        return "Пункт 18: сенсорное описание через запах. Проблемно, если почти каждая сцена начинается с запаха.";
    }

    @Override
    protected double weight() {
        return 5;
    }
}

