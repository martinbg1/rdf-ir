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



public class SearchTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {


        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(Search.class).withProcedure(TF_IDF.class)
                .withDisabledServer() // Don't need Neos HTTP server
                .withFixture(
                        "CREATE (d1:Disease {name:'covid', description:'blabla, hei hei hei, kake er godt, masse tekst.', altNames:'name,name,name covid, covids', uri:'klokke, hei hei hei, kake er '})" +
                        "CREATE (d2:Disease {name:'influenza', description:'influenza hei. veldig godt', altNames:'lol, name, influenza influenzas hei'})" +
                        "CREATE (d3:Disease {name:'lul', description:'lol, hei hei hei, lol lul lel ahaha', altNames:'automobile, name,name covid, covids'})"
//                        "CREATE (q:Disease {q:'influenza veldig blabla hei godt'})"
//                                "CREATE (i:TFIDF {_0:['[0.0, 1.7548875021634687, 0.0, 1.5849625007211563, 1.5849625007211563, 1.5849625007211563, 0.5849625007211562, 1.5849625007211563, 1.5849625007211563]','{covid=0.5849625007211562, kake=1.5849625007211563, mass=1.5849625007211563, name=0.0, godt=0.5849625007211562, tekst=1.5849625007211563, blabla=1.5849625007211563, hei=0.0, er=1.5849625007211563}'],_1:['[6.339850002884625, 0.0, 0.5849625007211562, 0.0, 1.5849625007211563, 0.5849625007211562]','{influenza=1.5849625007211563, lol=0.5849625007211562, name=0.0, veldig=1.5849625007211563, godt=0.5849625007211562, hei=0.0}'],_3:['[0.0, 0.0, 1.1699250014423124, 3.1699250014423126, 1.1699250014423124, 1.5849625007211563, 1.5849625007211563, 1.5849625007211563]','{covid=0.5849625007211562, lul=1.5849625007211563, lel=1.5849625007211563, ahaha=1.5849625007211563, name=0.0, lol=0.5849625007211562, automobil=1.5849625007211563, hei=0.0}']})"
//                                "CREATE (i:TFIDF {vec_3:[0.0,0.0,1.1699250014423124,3.1699250014423126,1.1699250014423124,1.5849625007211563,1.5849625007211563,1.5849625007211563],vec_1:[6.339850002884625,0.0,0.5849625007211562,0.0,1.5849625007211563,0.5849625007211562],idf_3:'{covid=0.5849625007211562, lul=1.5849625007211563, lel=1.5849625007211563, ahaha=1.5849625007211563, name=0.0, lol=0.5849625007211562, automobil=1.5849625007211563, hei=0.0}',idf_0:'{covid=0.5849625007211562, kake=1.5849625007211563, mass=1.5849625007211563, name=0.0, godt=0.5849625007211562, tekst=1.5849625007211563, blabla=1.5849625007211563, hei=0.0, er=1.5849625007211563}',vec_0:[0.0,1.7548875021634687,0.0,1.5849625007211563,1.5849625007211563,1.5849625007211563,0.5849625007211562,1.5849625007211563,1.5849625007211563],idf_1:'{influenza=1.5849625007211563, lol=0.5849625007211562, name=0.0, veldig=1.5849625007211563, godt=0.5849625007211562, hei=0.0}'})"
                )
                .build();

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String covid = "MATCH (d) return d";
            params.put("q", covid);
            Result result =  tx.execute( "CALL example.tfidfscore( $q )",params);
        }
    }

    @AfterAll
    static void stopNeo4j() {

        embeddedDatabaseServer.close();
    }


    @Test
    public void shouldReturnQueryResult() {
//        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
//            tx.execute("CREATE (q:Disease {q:'influenza veldig blabla hei godt'})");
//            tx.commit();
//        }

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String query = "hei lul influenza";
            params.put("query", query);

            Result result =  tx.execute( "CALL example.TfIdfSearch( $query )",params);
            System.out.println(result.resultAsString());

            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());

            // check if result makes sense

            assertThat(true).isTrue();
        }
    }
}
