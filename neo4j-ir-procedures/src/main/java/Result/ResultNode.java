package Result;

import org.neo4j.graphdb.Node;

import java.util.Map;

public class ResultNode {
    public String node;
    public Double score;

    public ResultNode(Map.Entry<Node, Double> entity) {
        this.node = entity.getKey().toString();
        this.score = entity.getValue();
    }
}