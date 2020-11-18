package improvedSearch;

import result.ResultInfo;
import keywords.CardKeyword;
import keywords.Document;
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

import static result.ResultUtil.sortResultInfo;


public class BM25 {
    @Context
    public GraphDatabaseService db;

    // static field to manage what term a query keyword compared to. This is needed because we use startsWith instead of
    // equals so we cannot guarantee that the CURRENT_TERM and query keyword is exactly the same
    private static String CURRENT_TERM;

    @Procedure
    @Description("improvedSearch.bm25Search(query) - returns bm25 query result")
    public Stream<ResultInfo> bm25Search(@Name("fetch") String query) throws IOException{
        Map<Long, Double> result = new LinkedHashMap<>();
        // Query document
        Document qDoc = new Document(query);

        try(Transaction tx = db.beginTx()) {
            // get all indexNodes with (terms, idf, tf og dl)
            ResourceIterator<Object> res = tx.execute("MATCH (n:indexNode) return n").columnAs("n");

            // retrieve mean document length
            double meanDocumentLength = (double) tx.execute("MATCH (n:DataStats) return n.meanDocumentLength").columnAs("n.meanDocumentLength").next();

            // fill result with a node and its corresponding BM25 score
            res.forEachRemaining(n -> result.put((Long)((Node) n).getProperty("ref"), bm25Score(
                    (String[])((Node) n).getProperty("terms"),
                    (double[])((Node) n).getProperty("idf"),
                    (int[])((Node) n).getProperty("tf"),
                    (int)((Node) n).getProperty("dl"),
                    meanDocumentLength,
                    qDoc)));

            };
            Map<String, Double> nodeMap = sortResultInfo(result, db, 10);
            return nodeMap.entrySet().stream().map(ResultInfo::new);
        }


    // math for bm25
    // take in documents and query, return their bm25 score
    public static double bm25Score(String[] docTerms, double[] idf, int[] tf, int dl, double avgDl, Document query){
        // raw term frequency, should be between 1.2 and 2.0, smaller value = each term occurrence counts for less
        double k1 = 1.2;

        // scale term weight by document length, usually 0.75
        double b = 0.75;

        // Map with term (String) as key and index of term (Integer) as value
        Map<String, Integer> termPosition = new HashMap<>();

        // Fill termPosition with terms and their corresponding index
        for (int i = 0; i < docTerms.length; i++) {
            termPosition.put(docTerms[i],i);
        }

        // BM25 score
        double sum = 0.0;

        // calculate the BM25 score for every document
        for(CardKeyword kw : query.keywords){
            if(termsStartsWith(docTerms, kw.getStem())){
                double tempIdf = idf[termPosition.get(CURRENT_TERM)];
                int tempTf = tf[termPosition.get(CURRENT_TERM)];
                sum += tempIdf*((tempTf*(k1+1))/(tempTf+k1*(1-b+(b*(dl/avgDl)))));

            }
        }
        return sum;
    }

    private static boolean termsStartsWith(String[] terms, String queryKeyword) {
        for (String s: terms) {
            if (s.startsWith(queryKeyword)) {
                // update CURRENT_TERM return true
                CURRENT_TERM = s;
                return true;
            }
        }
        return false;
    }
}
