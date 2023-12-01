package io.whitefox.core.types.predicates;

import static io.whitefox.core.services.DeltaSharedTable.parsePredicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

public class PredicateParsingTest {

  @Test
  void testParsingOfEqual() throws JsonProcessingException {
    var predicate = "{\n" + "  \"op\": \"equal\",\n"
        + "  \"children\": [\n"
        + "    {\"op\": \"column\", \"name\":\"hireDate\", \"valueType\":\"date\"},\n"
        + "    {\"op\":\"literal\",\"value\":\"2021-04-29\",\"valueType\":\"date\"}\n"
        + "  ]\n"
        + "}";
    var op = parsePredicate(predicate);
    op.validate();
    assert (op instanceof EqualOp);
    assert (((EqualOp) op).children.size() == 2);
  }

  @Test
  void testParsingOfNested() throws JsonProcessingException {
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
    var op = parsePredicate(predicate);
    op.validate();
    assert (op instanceof AndOp);
    assert (((AndOp) op).children.size() == 2);
    assert (((AndOp) op).children.get(0) instanceof EqualOp);
  }
}
