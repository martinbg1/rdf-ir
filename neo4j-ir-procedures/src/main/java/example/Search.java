package example;

import keywords.CardKeyword;
import keywords.Document;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class Search {
    @Context
    public GraphDatabaseService db;


    @Procedure
    @Description("example.tfidfSearch(query) - returns TF-IDF query result")
    public Stream<ResultNode> TfIdfSearch(@Name("fetch") String query) throws IOException {
        Map<Node, Double> result = new HashMap<>();
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Object> res = tx.execute("MATCH (n) return n").columnAs("n");
            res.forEachRemaining(n -> result.put((Node) n, 4.0));
        }

        return result.entrySet().stream().map(ResultNode::new);

    }

    public static class ResultNode {
        public String node;
        public Double score;

        public ResultNode(Map.Entry<Node, Double> entity) {
            this.node = entity.getKey().toString();
            this.score = entity.getValue();
        }
    }

}
