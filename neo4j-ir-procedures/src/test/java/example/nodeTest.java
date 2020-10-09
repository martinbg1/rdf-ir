package example;

import org.junit.jupiter.api.BeforeAll;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.Record;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class nodeTest {
    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();
    private ServerControls embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {

        this.embeddedDatabaseServer = TestServerBuilders
                .newInProcessBuilder()
                .withProcedure(getNodes.class)
                .newServer();
    }

    @Test
    public void shouldFindANode() {

        // In a try-block, to make sure we close the driver and session after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {

            // Given I've started Neo4j with the FullTextIndex procedure class
            //       which my 'neo4j' rule above does.
            // And given I have a node in the database
            String nodeId = session.run( "CREATE (d:Disease {name:'covid', desc:'blabla', altNames:'name,name,name'}) RETURN id(d)" )
                    .single()
                    .get( 0 ).toString();
            System.out.println(nodeId);
            //String result = session.run( "CALL example.nodes(nodeId)").single().get("result").asString();

            // Then I can search for that node with lucene query syntax
            //StatementResult result = session.run( "CALL example.search('User', 'name:Brook*')" );
            //assertThat(result.single().get( "nodeId" ).asLong()).isEqualTo( nodeId );

            List<Record> result = session.run( "CALL example.properties({nodeId})",parameters("nodeId",nodeId)).list();//.single().get("key").asString();
            //assertThat(result).isEqualTo("name:'covid', desc:'blabla', altNames:'name,name,name'");
            System.out.println(result);
        }
    }

}
