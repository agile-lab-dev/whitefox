package io.whitefox.core.types.predicates;

import io.whitefox.core.ColumnRange;
import io.whitefox.core.types.DataType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public class LeafEvaluationResult {

    Optional<Pair<ColumnRange, String>> rangeEvaluationResult;
    Optional<Pair<Pair<DataType, String>, Pair<DataType, String>>> partitionEvaluationResult;

    public LeafEvaluationResult(Optional<Pair<ColumnRange, String>> rangeEvaluationResult, Optional<Pair<Pair<DataType, String>, Pair<DataType, String>>> partitionEvaluationResult) {
        this.rangeEvaluationResult = rangeEvaluationResult;
        this.partitionEvaluationResult = partitionEvaluationResult;
    }

    public static LeafEvaluationResult createFromRange(Pair<ColumnRange, String> rangeEvaluationResult) {
        return new LeafEvaluationResult(Optional.of(rangeEvaluationResult), Optional.empty());
    }

    public static LeafEvaluationResult createFromPartitionColumn(Pair<Pair<DataType, String>, Pair<DataType, String>> partitionEvaluationResult) {
        return new LeafEvaluationResult(Optional.empty(), Optional.of(partitionEvaluationResult));
    }


}
