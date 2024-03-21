/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm;

import com.intellij.rt.coverage.report.api.Filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

class Wildcards {
    private static final HashSet<Character> regexMetacharactersSet = new HashSet<>();

    private static final String regexMetacharacters = "<([{\\^-=$!|]})+.>";

    static {
        for (int i = 0; i < regexMetacharacters.length(); i++) {
            char c = regexMetacharacters.charAt(i);
            regexMetacharactersSet.add(c);
        }
    }

    private Wildcards() {
        // no-op
    }

    static Filters convertFilters(KoverLegacyFeatures.ClassFilters filters) {
        return new Filters(
                convert(filters.includeClasses),
                convert(filters.excludeClasses),
                convert(filters.excludeAnnotation)
        );
    }

    private static List<Pattern> convert(Set<String> templates) {
        ArrayList<Pattern> patterns = new ArrayList<>(templates.size());
        for (String template : templates) {
            patterns.add(Pattern.compile(Wildcards.wildcardsToRegex(template)));
        }
        return patterns;
    }

    /**
     * Replaces characters `*` or `.` to `.*`, `#` to `[^.]*` and `?` to `.` regexp characters.
     */
    static String wildcardsToRegex(String value) {
        // in most cases, the characters `*` or `.` will be present therefore, we increase the capacity in advance
        final StringBuilder builder = new StringBuilder(value.length() * 2);

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (regexMetacharactersSet.contains(c)) {
                builder.append('\\').append(c);
            } else if (c == '*') {
                builder.append(".*");
            } else if (c == '?') {
                builder.append('.');
            } else if (c == '#') {
                builder.append("[^.]*");
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
