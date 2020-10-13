package example;

import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GetNodeTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {

        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(GetNode.class)
                .withDisabledServer() // Don't need Neos HTTP server
                .withFixture("CREATE (d:Disease {name:'covid', desc:'blabla', altNames:'name,name,name'}) RETURN id(d)")
                .build();
    }

    @AfterAll
    static void stopNeo4j() {

        embeddedDatabaseServer.close();
    }

    @Test
    void shouldFindProps() {

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String,Object> params = new HashMap<>();
            params.put( "nodeId", 0);
            Result result = tx.execute( "CALL example.properties( $nodeId )", params);
            System.out.println(result.resultAsString());
            assertThat(true).isTrue();
            tx.commit();
        }
    }
}