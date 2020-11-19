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
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BM25Test {
    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {


        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(BM25.class).withProcedure(IndexRDF.class).withProcedure(SetParameters.class)
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
            Result result =  tx.execute( "CALL improvedSearch.indexRDF( $n )", params);
        }
        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String query = "green dog rat";
            params.put("query", query);

            Result result =  tx.execute( "CALL improvedSearch.bm25Search( $query )",params);
            double[] expectedResult = new double[] {2.092, 1.437, 0.509};

            int resI = 0;
            DecimalFormat df = new DecimalFormat("#.###");
            while (result.hasNext()) {
                double score = (double) result.next().get("score");
                double v = df.parse(df.format(score)).doubleValue();
                assertThat(v).isEqualTo(expectedResult[resI]);
                resI++;
            }

            Result resultToPrint =  tx.execute( "CALL improvedSearch.bm25Search( $query )",params);
            System.out.println(resultToPrint.resultAsString());

            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldUpdateParameters(){
        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String nodes = "MATCH (d:Doc) return d";
            params.put("n", nodes);
            Result result =  tx.execute( "CALL improvedSearch.indexRDF( $n )", params);
        }
        double b = 0.2;
        double k1 = 2.0;
        try (var tx =embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()){
            Map<String, Object> params = new HashMap<>();
            params.put("b",b);
            params.put("k1",k1);

            tx.execute("CALL improvedSearch.setParameter($k1 , $b )", params);
            tx.commit();
        }

        try (var tx =embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()){

            Node parameters = (Node) tx.execute("MATCH (n:Parameters) return n").columnAs("n").next();
            assertThat(parameters.getProperty("k1")).isEqualTo(k1);
            assertThat(parameters.getProperty("b")).isEqualTo(b);

            Map<String, Object> params = new HashMap<>();
            String query = "green dog rat";
            params.put("query", query);

            Result result =  tx.execute( "CALL improvedSearch.bm25Search( $query )",params);
            double[] expectedResult = new double[] {2.092, 1.437, 0.509};

            int resI = 0;
            DecimalFormat df = new DecimalFormat("#.###");
            while (result.hasNext()) {
                double score = (double) result.next().get("score");
                double v = df.parse(df.format(score)).doubleValue();
                assertThat(v).isNotEqualTo(expectedResult[resI]);
                resI++;
            }

            Result resultToPrint =  tx.execute( "CALL improvedSearch.bm25Search( $query )",params);
            System.out.println(resultToPrint.resultAsString());

            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
