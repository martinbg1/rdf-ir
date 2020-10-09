package example;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.stream.Stream;


public class getNodes{

    @Context
    public GraphDatabaseService db;

    @Procedure(value ="example.properties")
    @Description("example.nodes(node|id|[ids]) - goal: quickly returns all properties from nodes with with these ids")
    public Stream<property> properties(@Name("node") String nodeId) {
        //Stream<Map> stream = Stream.empty();

        return db.getNodeById(Long.parseLong(nodeId)).getAllProperties().entrySet().stream().map(property::new);
    }

    public static class property {
        // This records contain a single field named 'nodeId'
        public Map<String,Object> properties;

        public String key;
        public Object value;

        public property(Map.Entry<String, Object> entity) {
            this.key = entity.getKey();
            this.value = entity.getValue();
        }
    }

}
