package io.whitefox.core.types.predicates;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

class EvalContext {

    public EvalContext(
            Map<String, String> partitionValues, Map<String, Pair<String, String>> statsValues) {
        this.partitionValues = partitionValues;
        this.statsValues = statsValues;
    }

    Map<String, String> partitionValues;
    Map<String, Pair<String, String>> statsValues;
}