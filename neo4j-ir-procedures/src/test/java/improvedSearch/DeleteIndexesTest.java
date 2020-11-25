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

public class DeleteIndexesTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {


        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(IndexRDF.class)
                .withProcedure(IndexRDFFielded.class)
                .withProcedure(IndexRDFFieldedNew.class)
                .withProcedure(DeleteIndexes.class)
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
            tx.execute( "CALL improvedSearch.indexRDF( $n )", params);
            tx.execute( "CALL improvedSearch.indexRDFFielded( $n )", params);
            tx.execute( "CALL improvedSearch.indexRDFFieldedNew( $n )", params);
        }
    }

    @AfterAll
    static void stopNeo4j() {

        embeddedDatabaseServer.close();
    }

    @Test
    public void shouldDeleteIndexes() {

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Result res = tx.execute("MATCH (n) return n AS BEFORE_DELETION");
            System.out.println(res.resultAsString());

            res = tx.execute("MATCH (n) return n");
            assertThat(res.stream().count()).isGreaterThan(3);

            tx.execute("CALL improvedSearch.deleteIndexes()");

            res = tx.execute("MATCH (n) return n AS AFTER_DELETION");
            System.out.println(res.resultAsString());

            res = tx.execute("MATCH (n) return n");
            assertThat(res.stream().count()).isEqualTo(3);
        }
    }
}
