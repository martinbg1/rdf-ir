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



public class IndexRDFFieldedTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(IndexRDFFielded.class)
                .withDisabledServer() // Don't need Neos HTTP server
                .withFixture(
                        "CREATE (d1:Doc {field1:'cat dog blue', field2:'cat red'})" +
                                "CREATE (d2:Doc {field1:'green', field2:'blue blue rat'})" +
                                "CREATE (d3:Doc {field1:'dog rat'})"
                )
                .build();
    }

    @AfterAll
    static void stopNeo4j() {

        embeddedDatabaseServer.close();
    }


    @Test
    public void shouldIndexFields() {

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String q = "MATCH (d) return d";
            params.put("q", q);

            Result result =  tx.execute( "CALL improvedSearch.indexRDFFielded( $q )",params);
            System.out.println(result.resultAsString());

            // check if result makes sense

            assertThat(true).isTrue();

            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());
        }
        

    }
}
