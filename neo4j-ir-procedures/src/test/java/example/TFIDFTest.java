package example;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;



public class TFIDFTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {

        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().withProcedure(TF_IDF.class)
                .withDisabledServer()
                .build();
    }

    @AfterAll
    static void stopNeo4j() {

        embeddedDatabaseServer.close();
    }

//    @Test
//    public void shouldCalculateTermFrequency() {
//
//        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
//
//            HashMap<String, Double> expectedTFResult = new HashMap<>();
//            expectedTFResult.put("lol", 0.25);
//            expectedTFResult.put("lul", 0.25);
//            expectedTFResult.put("hei", 0.5);
//
//            Object result = tx.execute( "RETURN example.tf('hei, lul, lol, hei') AS result").next().get("result");
//
//            assertThat(result).isEqualTo(expectedTFResult);
//        }
//    }

//    @Test
//    public void shouldCalculateInverseDocumentFrequency() {
//
//        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
//
//            HashMap<String, Double> expectedTFResult = new HashMap<>();
//            expectedTFResult.put("kek", 0.4054651081081644);
//            expectedTFResult.put("bolle", 0.4054651081081644);
//            expectedTFResult.put("lul", 1.0986122886681098);
//            expectedTFResult.put("kake", 1.0986122886681098);
//            expectedTFResult.put("lol", 0.4054651081081644);
//            expectedTFResult.put("hei", 0.4054651081081644);
//            expectedTFResult.put("muffins", 1.0986122886681098);
//
//            @SuppressWarnings("unchecked") // (:
//            Map<String, Object> result = (Map<String, Object>) tx.execute( "RETURN example.idf(['hei, lul, lol, hei', 'hei, kek, bolle, hei, lol', 'kake, muffins, bolle, kek']) AS result")
//                    .next().get("result");
//
//            // loops through all terms and asserts equality
//            result.forEach((term, idf) -> assertThat(expectedTFResult.get(term).compareTo((Double) idf)).isEqualTo(0));
//
//        }
//    }

    @Test
    public void shouldCalculateTF_IDF() {

        try(var tx = embeddedDatabaseServer.databaseManagementService().database("neo4j").beginTx()) {
            Map<String,Object> params = new HashMap<>();
            // ['hei, lul, lol, hei', 'hei, kek, bolle, hei, lol', 'kake, muffins, bolle, kek']
            params.put("altNames", 0);
            @SuppressWarnings("unchecked") // (:
            List<Map<String, Map<String, Double>>> result = (List<Map<String, Map<String, Double>>>) tx.execute( "CALL example.tfidfscores($ altNames )",params);

            // check if result makes sense
            result.forEach(doc -> doc.forEach((term, score) -> System.out.printf("%s: %s%n", term, score.entrySet())));
        }
    }
}
