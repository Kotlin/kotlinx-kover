/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm;

import com.intellij.rt.coverage.aggregate.api.AggregatorApi;
import com.intellij.rt.coverage.aggregate.api.Request;
import com.intellij.rt.coverage.report.api.Filters;
import com.intellij.rt.coverage.verify.Verifier;
import com.intellij.rt.coverage.verify.api.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

class LegacyVerification {
    private LegacyVerification() {
        // no-op
    }

    static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static List<KoverLegacyFeatures.RuleViolations> verify(List<KoverLegacyFeatures.Rule> rules, File tempDir, KoverLegacyFeatures.ClassFilters filters, List<File> reports, List<File> outputs) throws IOException {
        Filters intellijFilters = Wildcards.convertFilters(filters);

        final ArrayList<Rule> rulesArray = new ArrayList<>();

        File ic = new File(tempDir, "agg-ic.ic");
        File smap = new File(tempDir, "agg-smap.smap");

        Request requests = new Request(intellijFilters, ic, smap);
        AggregatorApi.aggregate(Collections.singletonList(requests), reports, outputs);

        for (int ruleIndex = 0; ruleIndex < rules.size(); ruleIndex++) {
            KoverLegacyFeatures.Rule rule = rules.get(ruleIndex);

            final List<Bound> bounds = new ArrayList<>();
            for (int boundIndex = 0; boundIndex < rule.bounds.size(); boundIndex++) {
                KoverLegacyFeatures.Bound b = rule.bounds.get(boundIndex);

                bounds.add(new Bound(boundIndex, counterToIntellij(b), valueTypeToIntellij(b), valueToIntellij(b, b.minValue), valueToIntellij(b, b.maxValue)));
            }

            rulesArray.add(new Rule(ruleIndex, ic, targetToIntellij(rule), bounds));
        }


        final Verifier verifier = new Verifier(rulesArray);
        verifier.processRules();
        List<RuleViolation> violations = VerificationApi.verify(rulesArray);

        ArrayList<KoverLegacyFeatures.RuleViolations> ruleViolations = new ArrayList<>();
        for (RuleViolation ruleViolation : violations) {
            TreeMap<ViolationId, KoverLegacyFeatures.BoundViolation> resultBounds = new TreeMap<>();

            KoverLegacyFeatures.Rule rule = rules.get(ruleViolation.id);
            for (int boundIndex = 0; boundIndex < ruleViolation.violations.size(); boundIndex++) {
                BoundViolation boundViolation = ruleViolation.violations.get(boundIndex);
                KoverLegacyFeatures.Bound bound = rule.bounds.get(boundViolation.id);

                for (Violation maxViolation : boundViolation.maxViolations) {
                    String entityName = rule.groupBy != KoverLegacyFeatures.GroupingBy.APPLICATION ? maxViolation.targetName : null;
                    resultBounds.put(
                            new ViolationId(boundViolation.id, entityName),
                            new KoverLegacyFeatures.BoundViolation(bound, true, intellijToValue(maxViolation.targetValue, bound), entityName)
                    );
                }
                for (Violation minViolation : boundViolation.minViolations) {
                    String entityName = rule.groupBy != KoverLegacyFeatures.GroupingBy.APPLICATION ? minViolation.targetName : null;
                    resultBounds.put(
                            new ViolationId(boundViolation.id, entityName),
                            new KoverLegacyFeatures.BoundViolation(bound, false, intellijToValue(minViolation.targetValue, bound), entityName)
                    );
                }
            }

            ruleViolations.add(new KoverLegacyFeatures.RuleViolations(rule, new ArrayList<>(resultBounds.values())));
        }

        return ruleViolations;
    }

    private static class ViolationId implements Comparable<ViolationId> {
        private final int index;
        private final String entityName;

        private ViolationId(int index, String entityName) {
            this.index = index;
            this.entityName = entityName;
        }

        @Override
        public int compareTo(@NotNull LegacyVerification.ViolationId other) {
            // first compared by index
            if (index != other.index) {
                return Integer.compare(index, other.index);
            }

            // if indexes are equals then compare by entity name
            if (entityName == null) {
                // bounds with empty entity names goes first
                return (other.entityName == null) ? 0 : -1;
            }

            if (other.entityName == null) return 1;

            return entityName.compareTo(other.entityName);
        }
    }

    private static BigDecimal intellijToValue(BigDecimal intellijValue, KoverLegacyFeatures.Bound bound) {
        if (isPercentage(bound.aggregationForGroup)) {
            return intellijValue.multiply(ONE_HUNDRED);
        } else {
            return intellijValue;
        }
    }

    private static Target targetToIntellij(KoverLegacyFeatures.Rule rule) {
        switch (rule.groupBy) {
            case APPLICATION:
                return Target.ALL;
            case CLASS:
                return Target.CLASS;
            case PACKAGE:
                return Target.PACKAGE;
        }
        return null;
    }

    private static Counter counterToIntellij(KoverLegacyFeatures.Bound bound) {
        switch (bound.coverageUnits) {
            case LINE:
                return Counter.LINE;
            case INSTRUCTION:
                return Counter.INSTRUCTION;
            case BRANCH:
                return Counter.BRANCH;
        }
        return null;
    }

    private static ValueType valueTypeToIntellij(KoverLegacyFeatures.Bound bound) {
        switch (bound.aggregationForGroup) {
            case COVERED_COUNT:
                return ValueType.COVERED;
            case MISSED_COUNT:
                return ValueType.MISSED;
            case COVERED_PERCENTAGE:
                return ValueType.COVERED_RATE;
            case MISSED_PERCENTAGE:
                return ValueType.MISSED_RATE;
        }
        return null;
    }

    private static BigDecimal valueToIntellij(KoverLegacyFeatures.Bound bound, BigDecimal value) {
        if (value == null) return null;

        if (isPercentage(bound.aggregationForGroup)) {
            return value.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP);
        } else {
            return value;
        }
    }

    private static boolean isPercentage(KoverLegacyFeatures.AggregationType aggregationType) {
        return aggregationType == KoverLegacyFeatures.AggregationType.COVERED_PERCENTAGE || aggregationType == KoverLegacyFeatures.AggregationType.MISSED_PERCENTAGE;
    }
}
