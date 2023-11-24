package io.whitefox.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.delta.standalone.DeltaLog;
import io.whitefox.core.services.DeltaSharedTable;
import io.whitefox.core.types.predicates.BaseOp;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static io.whitefox.api.server.DeltaTestUtils.deltaTable;
import static io.whitefox.api.server.DeltaTestUtils.deltaTableUri;

public class JsonPredicatesUtilsTest {

    @Test
    void testCreateEvalContext() {
        var PTable = new SharedTable(
                "partitioned-delta-table-with-multiple-columns", "default", "share1", deltaTable("partitioned-delta-table-with-multiple-columns"));

        var log = DeltaLog.forTable(new Configuration(), deltaTableUri("partitioned-delta-table-with-multiple-columns"));
        var contexts = log.snapshot().getAllFiles().stream().map(JsonPredicatesUtils::createEvalContext).collect(Collectors.toList());
        assert(contexts.size()==2);
        var c1 = contexts.get(0);
        assert(c1.getPartitionValues().get("date").equals("2021-08-09"));

    }
}
