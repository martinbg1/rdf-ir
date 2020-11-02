package example;

import keywords.CardKeyword;
import keywords.Document;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;


public class BM25 {
    @Context
    public GraphDatabaseService db;

    @Procedure
    @Description("example.bm25Search(query) - returns bm25 query result")
    public Stream<ResultNode> bm25Search(@Name("fetch") String query) throws IOException{
        Map<Node, Double> result = new LinkedHashMap<>();
        Document qDoc = new Document(query);

        try(Transaction tx = db.beginTx()) {
            // get all nodes that are not vectors, corpus or idf
            ResourceIterator<Object> res = tx.execute("MATCH (n:indexNode) return n").columnAs("n");


            res.forEachRemaining(n -> result.put((Node) n, bm25Score(
                    (String[])((Node) n).getProperty("terms"),
                    (double[])((Node) n).getProperty("idf"),
                    (int[])((Node) n).getProperty("tf"),
                    (int)((Node) n).getProperty("dl"),
                    qDoc)));
        }

        return result.entrySet().stream().map(BM25.ResultNode::new);
    }

    // Node returned as a Stream by procedure with node and bm25 score
    public static class ResultNode {
        public String node;
        public Double score;
        public ResultNode(Map.Entry<Node,Double> entity){
            this.node = entity.getKey().toString();
            this.score = entity.getValue();
        }
    }

    // math for bm25
    // take in documents and query, return their bm25 score
    public static double bm25Score(String[] docTerms, double[] idf, int[] tf, int dl, Document query){
        // raw term frequency, should be between 1.2 and 2.0
        // smaller value = each term occurrence counts for less
        double k1 = 1.2;
        // scale term weight by document length, usually 0.75
        double b = 0.75;
        // average document length
        double adl = 12;
        Map<String, Integer> termPosition = new HashedMap();

        for (int i = 0; i < docTerms.length; i++) {
            termPosition.put(docTerms[i],i);
        }

        List<String> docTermList = Arrays.asList(docTerms);

        double sum = 0.0;
        for(CardKeyword kw : query.keywords){
            if(Arrays.stream(docTerms).anyMatch(kw.getStem()::equals)){
                double tempidf = idf[termPosition.get(kw.getStem())];
                int temptf = tf[termPosition.get(kw.getStem())];
                sum += tempidf*(temptf*(k1+1)/temptf+k1*(1-b+(b*(dl/adl))));
            }
        }

        return sum;
    }
}
