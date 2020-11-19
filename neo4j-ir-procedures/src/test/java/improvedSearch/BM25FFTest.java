package improvedSearch;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BM25FFTest {
    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {


        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(BM25FF.class).withProcedure(IndexRDFFieldedNew.class).withProcedure(SetParameters.class)
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
    public void shouldReturnQueryResult() {
        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String nodes = "MATCH (d:Doc) return d";
            params.put("n", nodes);
            Result result =  tx.execute( "CALL improvedSearch.indexRDFFieldedNew( $n )", params);
        }
        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String query = "blue cat rat";
            params.put("query", query);

            Result resultToPrint =  tx.execute( "CALL improvedSearch.bm25ffSearch( $query )",params);
            System.out.println(resultToPrint.resultAsString());

            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());

        }
    }
    @Test
    public void shouldUpdateParameters(){
        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String nodes = "MATCH (d:Doc) return d";
            params.put("n", nodes);
            Result result =  tx.execute( "CALL improvedSearch.indexRDFFieldedNew( $n )", params);
        }
        double[] b = new double[]{0.2,1};
        String[] fieldNames = new String[] {"field2", "field1", "hei"};
        double k1 = 2.0;
        try (var tx =embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()){
            for (int i = 0; i < b.length; i++) {
                Map<String, Object> params = new HashMap<>();
                params.put("b",b[i]);
                params.put("fieldNames", fieldNames[i]);
                params.put("k1",k1);

                tx.execute("CALL improvedSearch.setFieldParameter($k1 , $fieldNames,  $b )", params);
            }

            tx.commit();
        }

        try (var tx =embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()){

            Node parameters = (Node) tx.execute("MATCH (n:ParametersFielded) return n").columnAs("n").next();

            assertThat(parameters.getProperty("k1")).isEqualTo(k1);

            for (int i = 0; i <b.length; i++) {
                assertThat(parameters.getProperty(fieldNames[i]+"_b")).isEqualTo(b[i]);
            }

            Map<String, Object> params = new HashMap<>();
            String query = "blue cat rat";
            params.put("query", query);


            Result resultToPrint =  tx.execute( "CALL improvedSearch.bm25ffSearch( $query )",params);
            System.out.println(resultToPrint.resultAsString());

            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());
        }

    }
}
