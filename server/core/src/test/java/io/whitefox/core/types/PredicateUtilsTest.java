package io.whitefox.core.types;

import static io.whitefox.DeltaTestUtils.deltaTable;
import static io.whitefox.DeltaTestUtils.deltaTableUri;

import io.delta.standalone.DeltaLog;
import io.delta.standalone.actions.AddFile;
import io.whitefox.core.PredicateUtils;
import io.whitefox.core.SharedTable;
import io.whitefox.core.types.predicates.*;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;

public class PredicateUtilsTest {

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
      EvalContext evalContext = PredicateUtils.createEvalContext(file);
      contexts.add(evalContext);
    }
    assert (contexts.size() == 2);
    var c1 = contexts.get(0);
    assert (c1.getPartitionValues().get("date").equals("2021-08-09"));
  }
}
