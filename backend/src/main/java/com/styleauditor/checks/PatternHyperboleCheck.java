package com.styleauditor.checks;

import java.util.regex.Pattern;

public class PatternHyperboleCheck extends RegexCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b(\u043e\u0433\u043b\u0443\u0448\u0430\u044e\u0449\\w+|\u0432\u0441\u0435\u043f\u043e\u0433\u043b\u043e\u0449\u0430\u044e\u0449\\w+|\u043f\u0435\u0440\u0432\u043e\u0431\u044b\u0442\u043d\\w+|\u0441\u0443\u0434\u044c\u0431\u043e\u043d\u043e\u0441\u043d\\w+|\u0435\u0434\u0438\u043d\u0441\u0442\u0432\u0435\u043d\u043d\u043e\\s+\u0432\u043e\u0437\u043c\u043e\u0436\u043d\\w+|\u0430\u0431\u0441\u043e\u043b\u044e\u0442\u043d\u043e\\s+\u043c[\u0435\u0451]\u0440\u0442\u0432\\w+|\u043d\u0435\u0432\u044b\u043d\u043e\u0441\u0438\u043c\\w+|\u0431\u0435\u0441\u043a\u043e\u043d\u0435\u0447\u043d\\w+|\u043d\u0435\u0432\u0435\u0440\u043e\u044f\u0442\u043d\\w+|\u043f\u0435\u0440\u0435\u043b\u043e\u043c\u043d\\w+)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS
    );

    @Override
    protected String type() {
        return "Гиперболическое усиление";
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
        return "Пункт 14: громкое усиление. Само по себе нормально, но при избытке создаёт пафос.";
    }

    @Override
    protected double weight() {
        return 7;
    }
}

