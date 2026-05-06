package com.styleauditor.checks;

import java.util.regex.Pattern;

public class PatternPseudoAphorismCheck extends RegexCheck {
    private static final Pattern PATTERN = Pattern.compile(
            "\\b(\u0442\u0438\u0448\u0438\u043d\u0430\\s+\u0431\u044b\u0432\u0430\u0435\u0442\\s+\u0433\u0440\u043e\u043c\u0447\u0435|\u0447\u0442\u043e\u0431\u044b\\s+\u0432\u044b\u0436\u0438\u0442\u044c|\u043d\u0430\u0437\u0430\u0434\\s+\u043f\u0443\u0442\u0438\\s+\u043d\u0435\u0442|\u044d\u0442\u043e\\s+\u0431\u044b\u043b\\s+\u043d\u0435\\s+\u043a\u043e\u043d\u0435\u0446|\u0432\u0441\u0451\\s+\u0442\u043e\u043b\u044c\u043a\u043e\\s+\u043d\u0430\u0447\u0438\u043d\u0430\u043b\u043e\u0441\u044c|\u043c\u0438\u0440\\s+\u0443\u0436\u0435\\s+\u043d\u0435\\s+\u0431\u0443\u0434\u0435\u0442\\s+\u043f\u0440\u0435\u0436\u043d\u0438\u043c)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS
    );

    @Override
    protected String type() {
        return "Псевдо-афоризмы";
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
        return "Пункт 4: похоже на афористичную обобщающую фразу.";
    }

    @Override
    protected double weight() {
        return 9;
    }
}

