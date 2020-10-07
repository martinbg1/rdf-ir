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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TFTest {

    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();
    private ServerControls embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {

        this.embeddedDatabaseServer = TestServerBuilders
                .newInProcessBuilder()
                .withFunction(TF.class)
                .newServer();
    }

    @Test
    public void shouldCalculateTermFrequency() {
        try( Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()) {

            HashMap<String, Double> expectedTFResult = new HashMap<>();
            expectedTFResult.put("lol", 0.25);
            expectedTFResult.put("lul", 0.25);
            expectedTFResult.put("hei", 0.5);

            // When
            Map<String, Object> result = session.run( "RETURN example.terms('hei, lul, lol, hei') AS result").single().get("result").asMap();

            // Then
            assertThat( result).isEqualTo(expectedTFResult);
        }
    }
}
