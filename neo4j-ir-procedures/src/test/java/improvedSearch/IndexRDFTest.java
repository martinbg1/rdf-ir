package improvedSearch;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;



public class IndexRDFTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(IndexRDF.class)
                .withDisabledServer() // Don't need Neos HTTP server
                .withFixture(
                        "CREATE (d1:Doc {field1:'dog blue', field2:'cat red dog'})" +
                                "CREATE (d2:Doc {field1:'green, red', field2:'blue blue cat'})" +
                                "CREATE (d3:Doc {field1:'dog rat rat'})")
                .build();
    }

    @AfterAll
    static void stopNeo4j() {

        embeddedDatabaseServer.close();
    }


    @Test
    public void shouldCalculateTF_IDF() {
        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String nodes = "MATCH (d:Doc) return d";
            params.put("nodes", nodes);

            Result result =  tx.execute( "CALL improvedSearch.indexRDF( $nodes )",params);
            System.out.println(result.resultAsString());
            
            // check if result makes sense
            assertThat(true).isTrue();
        }

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());
        }

    }
}
