package io.whitefox.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.delta.standalone.actions.AddFile;
import io.whitefox.core.types.predicates.BaseOp;
import io.whitefox.core.types.predicates.EvalContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.Map;

public class JsonPredicatesUtils {

    public static BaseOp parsePredicate(String predicate) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        return mapper.readValue(predicate, BaseOp.class);
    }

    public static EvalContext createEvalContext(AddFile file){
        var statsString = file.getStats();
        var partitionValues = file.getPartitionValues();

        var mapper = new ObjectMapper();
        try {
            var fileStats = mapper.readValue(statsString, FileStats.class);
            var maxValues = fileStats.maxValues;
            var mappedMinMaxPairs = new java.util.HashMap<String, Pair<String, String>>();
            fileStats.getMinValues().forEach((minK,minV) -> {
                String maxV =  maxValues.get(minK);
                Pair<String, String> minMaxPair = Pair.of(minV, maxV);
                mappedMinMaxPairs.put(minK, minMaxPair);
            });
            return new EvalContext(partitionValues, mappedMinMaxPairs);
        }
        catch (JsonProcessingException e) {
            var message = e.getMessage();
            return new EvalContext(partitionValues, Map.of());
        }
    }
}
