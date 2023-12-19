package io.whitefox.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.delta.standalone.actions.AddFile;
import io.delta.standalone.expressions.IsNull;
import io.whitefox.core.types.DataType;
import io.whitefox.core.types.DateType;
import io.whitefox.core.types.predicates.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.commons.lang3.tuple.Pair;

public class JsonPredicatesUtils {

  private static final ObjectMapper objectMapper = DeltaObjectMapper.getInstance();

  public static BaseOp parseJsonPredicate(String predicate) throws PredicateParsingException {
    try {
      return objectMapper.readValue(predicate, BaseOp.class);
    } catch (JsonProcessingException e) {
      throw new PredicateParsingException(e);
    }
  }

  public static BaseOp parseSqlPredicate(String predicate, DataType dataType) throws JSQLParserException, PredicateException {
    var expression = CCJSqlParserUtil.parseCondExpression(predicate);
    if (expression instanceof IsNullExpression){
      var isNullExpression = (IsNullExpression) expression;
      String column = isNullExpression.getLeftExpression().getASTNode().jjtGetFirstToken().toString();
      var colOp = new ColumnOp(column, dataType);
      var children = List.of((LeafOp) colOp);
      var operator = "isnull";
      return NonLeafOp.createPartitionFilter(children, operator);
    }
    else if (expression instanceof BinaryExpression) {
      BinaryExpression binaryExpression = (BinaryExpression) expression;
      String column = binaryExpression.getLeftExpression().toString();
      String operator = binaryExpression.getStringExpression();
      String value = binaryExpression.getRightExpression().toString();
      var colOp = new ColumnOp(column, dataType);
      var litOp = new LiteralOp(value, dataType);
      var children = List.of(colOp, litOp);
      return NonLeafOp.createPartitionFilter(children, operator);
    }
    // TODO: PARSING FAIL on sql;
    else
      throw new PredicateException();
  }



  public static ColumnRange createColumnRange(String name, EvalContext ctx, DataType valueType)
      throws NonExistingColumnException {
    var fileStats = ctx.getStatsValues();
    var values = Optional.ofNullable(fileStats.get(name))
        .orElseThrow(() -> new NonExistingColumnException(name));
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
