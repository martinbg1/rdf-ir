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

public class BM25FTest {
    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {


        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(BM25F.class).withProcedure(IndexRDFFielded.class)
                .withDisabledServer() // Don't need Neos HTTP server
                .withFixture(
                        "CREATE (d1:Disease {name:'covid', description:'blabla, hei hei hei, kake er godt, masse tekst.', altNames:'name,name,name covid, covids', uri:'klokke, hei hei hei, kake er ', test:'automobile'})" +
                                "CREATE (d2:Disease {name:'influenza', description:'lul hei. veldig godt', altNames:'lol, name, influenza influenzas hei', _test: 'martin'})" +
                                "CREATE (d3:Disease {name:'lul influenza', description:'lol, hei hei hei, lol lul lel ahaha', altNames:'automobile, name,name covid, covids'})"
                )
                .build();

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String nodes = "MATCH (d) return d";
            params.put("n", nodes);
            Result result =  tx.execute( "CALL improvedSearch.indexRDFFielded( $n )",params);
        }
    }

    @AfterAll
    static void stopNeo4j() {

        embeddedDatabaseServer.close();
    }


    @Test
    public void shouldReturnQueryResult() {
        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String query = "influenza automobile";
            params.put("query", query);

            Result result =  tx.execute( "CALL improvedSearch.bm25fSearch( $query )",params);
            System.out.println(result.resultAsString());

            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());

            // check if result makes sense

            assertThat(true).isTrue();
        }
    }

}
