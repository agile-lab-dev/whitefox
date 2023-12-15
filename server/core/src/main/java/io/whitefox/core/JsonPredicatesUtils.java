package io.whitefox.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.delta.standalone.actions.AddFile;
import io.whitefox.core.types.DataType;
import io.whitefox.core.types.DateType;
import io.whitefox.core.types.IntegerType;
import io.whitefox.core.types.predicates.BaseOp;
import io.whitefox.core.types.predicates.EvalContext;
import java.util.Map;

import io.whitefox.core.types.predicates.PredicateParsingException;
import org.apache.commons.lang3.tuple.Pair;

import static io.whitefox.core.types.DateType.DATE;

public class JsonPredicatesUtils {

  private static final ObjectMapper objectMapper = DeltaObjectMapper.getInstance();

  public static BaseOp parsePredicate(String predicate) throws PredicateParsingException {
    try {
      return objectMapper.readValue(predicate, BaseOp.class);
    }
    catch (JsonProcessingException e){
      throw new PredicateParsingException(e);
    }
  }

  public static ColumnRange createColumnRange(String name, EvalContext ctx, DataType valueType) {
    var fileStats = ctx.getStatsValues();
    var values = fileStats.get(name);
    return new ColumnRange(values.getLeft(), values.getRight(), valueType);
  }

  public static EvalContext createEvalContext(AddFile file) throws PredicateParsingException {
    var statsString = file.getStats();
    var partitionValues = file.getPartitionValues();

    try {
      var fileStats = objectMapper.readValue(statsString, FileStats.class);
      var maxValues = fileStats.maxValues;
      var mappedMinMaxPairs = new java.util.HashMap<String, Pair<String, String>>();
      fileStats.getMinValues().forEach((minK, minV) -> {
        String maxV = maxValues.get(minK);
        Pair<String, String> minMaxPair = Pair.of(minV, maxV);
        mappedMinMaxPairs.put(minK, minMaxPair);
      });
      return new EvalContext(partitionValues, mappedMinMaxPairs);
    } catch (JsonProcessingException e) {
      // should never happen, depends on if the delta implementation changes
      throw new PredicateParsingException(e);
    }
  }
}
