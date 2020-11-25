package improvedSearch;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DoubleBM25SearchTest {
    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {


        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(BM25FF.class).withProcedure(IndexRDFFieldedNew.class).withProcedure(IndexRDFFielded.class).withProcedure(BM25F.class)
                .withDisabledServer() // Don't need Neos HTTP server
                .withFixture(
                        "CREATE (d1:Doc {field1:'cat dog blue', field2:'cat red'})" +
                                "CREATE (d2:Doc {field1:'green', field2:'blue blue rat'})" +
                                "CREATE (d3:Doc {field1:'dog rat'})"
                )
                .build();

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String nodes = "MATCH (d:Doc) return d";
            params.put("n", nodes);
            tx.execute( "CALL improvedSearch.indexRDFFieldedNew( $n )", params);
            tx.execute( "CALL improvedSearch.indexRDFFielded( $n )", params);
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
            String query = "blue cat rat";
            params.put("query", query);

            Result result =  tx.execute( "CALL improvedSearch.bm25fSearch( $query )",params);
            Result fieldResult =  tx.execute( "CALL improvedSearch.bm25ffSearch( $query )",params);
            double[] expectedResult = new double[] {1.193, 0.592, 0.266};

            int resI = 0;
            DecimalFormat df = new DecimalFormat("#.###");
            while (result.hasNext()) {
                double score = (double) result.next().get("score");
                double v = df.parse(df.format(score)).doubleValue();
                assertThat(v).isEqualTo(expectedResult[resI]);
                resI++;
            }

            Result resultToPrint =  tx.execute( "CALL improvedSearch.bm25fSearch( $query )",params);
            System.out.println(resultToPrint.resultAsString());

            Result fieldResultToPrint =  tx.execute( "CALL improvedSearch.bm25ffSearch( $query )",params);
            System.out.println(fieldResultToPrint.resultAsString());

            Result res = tx.execute("MATCH (n) return n");
            System.out.println(res.resultAsString());

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
