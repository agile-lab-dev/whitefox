package io.whitefox.core.types.predicates;

import io.whitefox.core.ColumnRange;

public class PartitionEvaluationResult {

  ColumnRange partitionValue;
  ColumnRange literalValue;

  public PartitionEvaluationResult(ColumnRange partitionValue, ColumnRange literalValue) {
    this.partitionValue = partitionValue;
    this.literalValue = literalValue;
  }

  public ColumnRange getPartitionValue() {
    return partitionValue;
  }

  public ColumnRange getLiteralValue() {
    return literalValue;
  }
}
