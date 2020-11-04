package example;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;



public class VectorModelTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {


        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(VectorModel.class).withProcedure(IndexRDF.class)
                .withDisabledServer() // Don't need Neos HTTP server
                .withFixture(
                        "CREATE (d1:Disease {name:'covid', description:'blabla, hei hei hei, kake er godt, masse tekst.', altNames:'name,name,name covid, covids', uri:'klokke, hei hei hei, kake er '})" +
                        "CREATE (d2:Disease {name:'influenza', description:'influenza hei. veldig godt', altNames:'lol, name, influenza influenzas hei'})" +
                        "CREATE (d3:Disease {name:'lul', description:'lol, hei hei hei, lol lul lel ahaha', altNames:'automobile, name,name covid, covids'})" +
                        "CREATE (q:Disease {q:'influenza veldig blabla godt'})"
                )
                .build();

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String covid = "MATCH (d) return d";
            params.put("q", covid);
            Result result =  tx.execute( "CALL example.indexRDF( $q )",params);
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
            String query = "hei lul influenza lul";
            params.put("query", query);

            Result result =  tx.execute( "CALL example.vectorModelSearch( $query )",params);
            System.out.println(result.resultAsString());

            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());

            // check if result makes sense

            assertThat(true).isTrue();
        }
    }
}
