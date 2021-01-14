package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.*;

public class ResultUtil {

    /**
     * Sort search result based on score
     * @param result - Map with node id pointing to search score
     * @param db - db service in use
     * @param topN - amount of results to return. TopN=5 returns top 5 scores
     * @return - Map with Node pointing to search score in sorted order
     */
    public static Map<Node, Double> sortResult(Map<Long, Double> result, GraphDatabaseService db, int topN) {
        // Sort result list based on cosine similarity
        List<Map.Entry<Long, Double>> sortedResult = new ArrayList<>(result.entrySet());
        sortedResult.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // to store result node and bm25 score
        Map<Node, Double> nodeMap = new LinkedHashMap<>();
        // to store top n results
        List<Map.Entry<Long, Double>> topRes;

        try(Transaction tx1 = db.beginTx()){
            // return top 5 results if possible
            if (sortedResult.size() > topN) {
                topRes = sortedResult.subList(0, topN);
            } else {
                topRes = sortedResult;
            }
            // loop through top results and query result Node
            for(Map.Entry<Long, Double> entry : topRes){
                HashMap<String, Object> params = new HashMap<>();
                params.put("nodeId", entry.getKey());
                Node tempNode = (Node)(tx1.execute("MATCH (n) WHERE ID(n) =$nodeId return n", params).columnAs("n").next());
                nodeMap.put(tempNode, entry.getValue());
            }
        }
        return nodeMap;
    }

    public static Map<String, Double> sortResultInfo(Map<Long, Double> result, GraphDatabaseService db, int topN) {
        // Sort result list based on cosine similarity
        List<Map.Entry<Long, Double>> sortedResult = new ArrayList<>(result.entrySet());
        sortedResult.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // to store result node and bm25 score
        Map<String, Double> nodeMap = new LinkedHashMap<>();
        // to store top n results
        List<Map.Entry<Long, Double>> topRes;

        try(Transaction tx1 = db.beginTx()){
            // return top 5 results if possible
            if (sortedResult.size() > topN) {
                topRes = sortedResult.subList(0, topN);
            } else {
                topRes = sortedResult;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            // loop through top results and query result Node
            for(Map.Entry<Long, Double> entry : topRes){
                HashMap<String, Object> params = new HashMap<>();
                params.put("nodeId", entry.getKey());
                Node tempNode = (Node)(tx1.execute("MATCH (n) WHERE ID(n) =$nodeId return n", params).columnAs("n").next());
                String jsonResult = "";
                try {
                    jsonResult = objectMapper.writeValueAsString(tempNode.getAllProperties());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                nodeMap.put(jsonResult, entry.getValue());
            }
        }
        return nodeMap;
    }
}
