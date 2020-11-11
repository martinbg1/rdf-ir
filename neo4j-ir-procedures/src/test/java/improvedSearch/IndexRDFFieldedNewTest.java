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



public class IndexRDFFieldedNewTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(IndexRDFFieldedNew.class)
                .withDisabledServer() // Don't need Neos HTTP server
                .withFixture(
                        "CREATE (d1:Disease {name:'covid hei', description:'blabla, hei hei hei, kake er godt, masse tekst.', altNames:'name,name,name covid, covids', uri:'klokke, hei hei hei, kake er '})" +
                                "CREATE (d2:Disease {name:'influenza hei', description:'influenza hei. veldig godt', altNames:'lol, name, influenza influenzas hei', shit:'automobile', test:'yoyo lol hei'})" +
                                "CREATE (d3:Disease {name:'lul', description:'lol, hei hei hei, lol lul lel ahaha', altNames:'automobile, name,name covid, covids', shit:'automobile covids'})")
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
            String text = "Cake is a form of sweet food made from flour, sugar, and other ingredients, that is usually baked. " +
                    "In their oldest forms, cakes were modifications of bread, but cakes now cover a wide range of preparations that can be simple or elaborate, " +
                    "and that share features with other desserts such as pastries, meringues, custards, and pies.\n" +
                    "\n" +
                    "The most commonly used cake ingredients include flour, sugar, eggs, butter or oil or margarine, a liquid, and leavening agents, " +
                    "such as baking soda or baking powder. Common additional ingredients and flavourings include dried, candied, or fresh fruit, nuts, cocoa, " +
                    "and extracts such as vanilla, with numerous substitutions for the primary ingredients." +
                    " Cakes can also be filled with fruit preserves, nuts or dessert sauces (like pastry cream), iced with buttercream or other icings," +
                    " and decorated with marzipan, piped borders, or candied fruit.";
            params.put("text", text);
            tx.commit();
        }
        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String, Object> params = new HashMap<>();
            String covid = "MATCH (d) return d, d.altNames, d.description";
            params.put("covid", covid);

            Result result =  tx.execute( "CALL improvedSearch.indexRDFFieldedNew( $covid )",params);
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
