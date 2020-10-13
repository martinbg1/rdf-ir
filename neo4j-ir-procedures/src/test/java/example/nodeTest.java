package example;


import org.junit.jupiter.api.BeforeAll;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.Record;
import org.neo4j.harness.junit.rule.Neo4jRule;
//import org.neo4j.harness.TestServerBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

//import org.junit.Rule;
//import org.junit.Test;
//import org.neo4j.driver.v1.*;
//import org.neo4j.graphdb.factory.GraphDatabaseSettings;
//import org.neo4j.harness.junit.Neo4jRule;
//
//import static org.hamcrest.core.IsEqual.equalTo;
//import static org.junit.Assert.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class nodeTest {
    public Neo4jRule neo4j = new Neo4jRule()

            // This is the Procedure to test
            .withProcedure( getNodes.class );
    //private static final Config driverConfig = Config.build().withoutEncryption().toConfig();
    //private ServerControls embeddedDatabaseServer;


//    @BeforeAll
//    void initializeNeo4j() {
//
//        this.embeddedDatabaseServer = TestServerBuilders
//                .newInProcessBuilder()
//                .withProcedure(getNodes.class)
//                .newServer();
//    }

    @Test
    public void shouldFindANode() {

        // In a try-block, to make sure we close the driver and session after the test
        try(Driver driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withoutEncryption().toConfig()))
        {
            Session session = driver.session();
            String nodeId = session.run( "CREATE (d:Disease {name:'covid', desc:'blabla', altNames:'name,name,name'}) RETURN id(d)" )
                    .single()
                    .get( 0 ).toString();

            List<Record> result = session.run( "CALL example.properties({nodeId})",parameters("nodeId",nodeId)).list();//.single().get("key").asString();
            System.out.println(result);
        }
    }

}