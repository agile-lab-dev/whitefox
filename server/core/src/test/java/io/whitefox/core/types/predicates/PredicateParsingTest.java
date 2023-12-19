package io.whitefox.core.types.predicates;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.whitefox.core.JsonPredicatesUtils;
import org.junit.jupiter.api.Test;

public class PredicateParsingTest {

  @Test
  void testParsingOfEqual() throws PredicateException {

    var predicate = "{\n" + "  \"op\": \"equal\",\n"
        + "  \"children\": [\n"
        + "    {\"op\": \"column\", \"name\":\"hireDate\", \"valueType\":\"date\"},\n"
        + "    {\"op\":\"literal\",\"value\":\"2021-04-29\",\"valueType\":\"date\"}\n"
        + "  ]\n"
        + "}";
    var op = JsonPredicatesUtils.parseJsonPredicate(predicate);
    op.validate();
    assert (op instanceof EqualOp);
    assert (((EqualOp) op).children.size() == 2);
  }

  @Test
  void testParsingOfNested() throws PredicateException {
    var predicate = "{\n" + "  \"op\":\"and\",\n"
        + "  \"children\":[\n"
        + "    {\n"
        + "      \"op\":\"equal\",\n"
        + "      \"children\":[\n"
        + "        {\"op\":\"column\",\"name\":\"hireDate\",\"valueType\":\"date\"},\n"
        + "        {\"op\":\"literal\",\"value\":\"2021-04-29\",\"valueType\":\"date\"}\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"op\":\"lessThan\",\"children\":[\n"
        + "        {\"op\":\"column\",\"name\":\"id\",\"valueType\":\"int\"},\n"
        + "        {\"op\":\"literal\",\"value\":\"25\",\"valueType\":\"int\"}\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}";
    var op = JsonPredicatesUtils.parseJsonPredicate(predicate);
    op.validate();
    assert (op instanceof AndOp);
    assert (((AndOp) op).children.size() == 2);
    assert (((AndOp) op).children.get(0) instanceof EqualOp);
  }

  @Test
  void testCustomExceptionOnBadJson() {
    var predicate = "{\n" + "  \"op\":\"and\",\n"
        + "  \"children\":[\n"
        + "    {\n"
        + "      \"op\":\"equals\",\n"
        + "      \"children\":[\n"
        + "        {\"op\":\"columna\",\"name\":\"hireDate\",\"valueType\":\"date\"},\n"
        + "        {\"op\":\"literal\",\"value\":\"2021-04-29\",\"valueType\":\"date\"}\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"op\":\"lessThans\",\"children\":[\n"
        + "        {\"op\":\"column\",\"name\":\"id\",\"valueType\":\"int\"},\n"
        + "        {\"op\":\"literal\",\"value\":\"25\",\"valueType\":\"int\"}\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}";
    assertThrows(
        PredicateParsingException.class, () -> JsonPredicatesUtils.parseJsonPredicate(predicate));
  }
}
