package io.whitefox.core.types.predicates;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.whitefox.core.types.DateType;
import io.whitefox.core.types.IntegerType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class EvalHelperTest {

  @Test
  void testLessThan() throws PredicateException {
    var evalContext1 = new EvalContext(Map.of("date", "2020-10-10"), Map.of());
    var children1 =
        List.of(new ColumnOp("date", DateType.DATE), new LiteralOp("2020-10-11", DateType.DATE));
    assertTrue(EvalHelper.lessThan(children1, evalContext1));
    var evalContext2 = new EvalContext(Map.of("integer", "19"), Map.of());
    var children2 = List.of(
        new ColumnOp("integer", IntegerType.INTEGER), new LiteralOp("20", IntegerType.INTEGER));
    assertTrue(EvalHelper.lessThan(children2, evalContext2));
  }

  @Test
  void testEqual() throws PredicateException {
    var evalContext1 = new EvalContext(Map.of("date", "2020-10-11"), Map.of());
    var children1 =
        List.of(new ColumnOp("date", DateType.DATE), new LiteralOp("2020-10-11", DateType.DATE));
    assertTrue(EvalHelper.equal(children1, evalContext1));
    var evalContext2 = new EvalContext(Map.of("integer", "20"), Map.of());
    var children2 = List.of(
        new ColumnOp("integer", IntegerType.INTEGER), new LiteralOp("20", IntegerType.INTEGER));
    assertTrue(EvalHelper.equal(children2, evalContext2));
  }
}
