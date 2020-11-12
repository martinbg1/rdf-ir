package improvedSearch;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.codegen.bytecode.While;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BM25Test {
    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {


        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(BM25.class).withProcedure(IndexRDF.class)
                .withDisabledServer() // Don't need Neos HTTP server
                .withFixture(
                        "CREATE (d1:Doc {field1:'cat dog blue cat red'})" +
                                "CREATE (d2:Doc {field1:'green blue blue rat'})" +
                                "CREATE (d3:Doc {field1:'dog rat'})"
                )
                .build();

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String covid = "MATCH (d) return d";
            params.put("q", covid);
            Result result =  tx.execute( "CALL improvedSearch.indexRDF( $q )",params);
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

}
