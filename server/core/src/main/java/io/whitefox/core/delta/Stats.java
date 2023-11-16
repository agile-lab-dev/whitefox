package io.whitefox.core.delta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
public class Stats {
    private final long numRecords;
    private final Set<ColumnStat<Double>> minValues = Set.of();
    private final Set<ColumnStat<Double>> maxValues = Set.of();
    private final Set<ColumnStat<Long>> nullCount = Set.of();
    @Data
    public static class ColumnStat<T> {
        private final List<String> columnPath;
        private final T value;
    }
}
