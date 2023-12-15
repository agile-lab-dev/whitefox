package io.whitefox.core.types;

import static io.whitefox.DeltaTestUtils.deltaTable;
import static io.whitefox.DeltaTestUtils.deltaTableUri;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.delta.standalone.DeltaLog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.delta.standalone.actions.AddFile;
import io.whitefox.core.JsonPredicatesUtils;
import io.whitefox.core.SharedTable;
import io.whitefox.core.types.predicates.*;
import jakarta.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;

public class JsonPredicatesUtilsTest {

  @Test
  void testCreateEvalContext() throws PredicateParsingException {
    var PTable = new SharedTable(
        "partitioned-delta-table-with-multiple-columns",
        "default",
        "share1",
        deltaTable("partitioned-delta-table-with-multiple-columns"));


    var log = DeltaLog.forTable(
        new Configuration(), deltaTableUri("partitioned-delta-table-with-multiple-columns"));
    var contexts = new ArrayList<EvalContext>();
    for (AddFile file : log.snapshot().getAllFiles()) {
      EvalContext evalContext = JsonPredicatesUtils.createEvalContext(file);
      contexts.add(evalContext);
    }
    assert (contexts.size() == 2);
    var c1 = contexts.get(0);
    assert (c1.getPartitionValues().get("date").equals("2021-08-09"));
  }
}
