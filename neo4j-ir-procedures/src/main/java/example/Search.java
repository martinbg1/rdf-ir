package example;

import javassist.compiler.ast.Keyword;
import keywords.CardKeyword;
import keywords.Document;
import keywords.KeywordsExtractor;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
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
            List<CardKeyword> queryKeywords = KeywordsExtractor.getKeywordsList(query);
            queryKeywords.forEach(k -> System.out.println(k.getStem()));
            res.forEachRemaining(n -> result.put((Node) n, 4.0));

            ResourceIterator<Node> index = tx.execute("MATCH (n:TFIDF) return n").columnAs("n");
            Map<String, Map<String, String[]>> indexMap = new HashedMap();
//            index.next().getAllProperties().forEach((k,v) -> indexMap.put(k, (Map<String, String[]>) v));
            String[] test = (String[]) index.next().getProperty("_0");
            System.out.println(test[0]);
//            index.next().getAllProperties().forEach((k, v) -> {
//                try {
//                    indexMap.put(k, stringToHashMap((String) v));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            System.out.println(indexMap.get("_0").get("covid"));
        }
        return result.entrySet().stream().map(ResultNode::new);

    }

    private static Map<String, Double> stringToHashMap(String str) throws IOException {

        Properties props = new Properties();
        props.load(new StringReader(str.substring(1, str.length() - 1).replace(", ", "\n")));
        Map<String, Double> map = new HashMap();
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            map.put((String)e.getKey(), Double.parseDouble((String) e.getValue()));
        }
        return map;
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
