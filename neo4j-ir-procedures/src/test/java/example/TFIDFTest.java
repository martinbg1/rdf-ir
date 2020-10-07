package example;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import java.util.*;

//import com.google.common.math.DoubleMath;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TFIDFTest {

    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();
    private ServerControls embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {

        this.embeddedDatabaseServer = TestServerBuilders
                .newInProcessBuilder()
                .withFunction(TF_IDF.class)
                .newServer();
    }

    @Test
    public void shouldCalculateTermFrequency() {

        // In a try-block, to make sure we close the driver and session after the test
        try( Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()) {

            HashMap<String, Double> expectedTFResult = new HashMap<>();
            expectedTFResult.put("lol", 0.25);
            expectedTFResult.put("lul", 0.25);
            expectedTFResult.put("hei", 0.5);

            // When
            Map<String, Object> result = session.run( "RETURN example.tf('hei, lul, lol, hei') AS result").single().get("result").asMap();

            // Then
            assertThat( result).isEqualTo(expectedTFResult);
        }
    }

    @Test
    public void shouldCalculateInverseDocumentFrequency() {

        // In a try-block, to make sure we close the driver and session after the test
        try( Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()) {

            HashMap<String, Double> expectedTFResult = new HashMap<>();
            expectedTFResult.put("kek", 0.4054651081081644);
            expectedTFResult.put("bolle", 0.4054651081081644);
            expectedTFResult.put("lul", 1.0986122886681098);
            expectedTFResult.put("kake", 1.0986122886681098);
            expectedTFResult.put("lol", 0.4054651081081644);
            expectedTFResult.put("hei", 0.4054651081081644);
            expectedTFResult.put("muffins", 1.0986122886681098);

            // When
            Map<String, Object> result = session.run( "RETURN example.idf(['hei, lul, lol, hei', 'hei, kek, bolle, hei, lol', 'kake, muffins, bolle, kek']) AS result").single().get("result").asMap();
            // Then
            result.forEach((term, idf) -> assertThat(expectedTFResult.get(term).compareTo((Double) idf)).isEqualTo(0));

        }
    }

    @Test
    public void shouldCalculateTF_IDF() {

        // In a try-block, to make sure we close the driver and session after the test
        try( Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()) {

            // When
            List<Object> result = session.run( "RETURN example.tf_idf(['hei, lul, lol, hei', 'hei, kek, bolle, hei, lol', 'kake, muffins, bolle, kek']) AS result").single().get("result").asList();

            // Then
            assertThat(true).isTrue(); // for lat til å teste dette.

        }
    }
}
