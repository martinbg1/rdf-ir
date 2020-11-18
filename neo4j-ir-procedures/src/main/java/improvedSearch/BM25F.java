package improvedSearch;

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
import result.ResultInfo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static result.ResultUtil.sortResultInfo;

public class BM25F {
    @Context
    public GraphDatabaseService db;

    // static field to manage what term a query keyword compared to. This is needed because we use startsWith instead of
    // equals so we cannot guarantee that the CURRENT_TERM and query keyword is exactly the same
    private static String CURRENT_TERM;

    @Procedure
    @Description("improvedSearch.bm25fSearch(query) - returns bm25f query result")
    public Stream<ResultInfo> bm25fSearch(@Name("fetch") String query) throws IOException {
        Map<Long, Double> result = new LinkedHashMap<>();
        // Query document
        Document qDoc = new Document(query);

        try(Transaction tx = db.beginTx()) {
            // get all indexNodes for fields with (terms, idf, tf og fl)
            ResourceIterator<Object> res = tx.execute("MATCH (n:fieldIndexNode) return n").columnAs("n");


            // retrieve mean field length
            Node fieldDataStats = (Node) tx.execute("MATCH (n:DataStats) return n").columnAs("n").next();
            String[] fieldNames = (String[]) tx.execute("MATCH (n:Corpus) return n.fieldName").columnAs("n.fieldName").next();

            Map<String, Object> fieldAvgLength = fieldDataStats.getAllProperties();

            // fill result with a node and its corresponding bm25f score
            while(res.hasNext()){
                Node node = (Node) res.next();
                Map<String, Object> properties = node.getAllProperties();

                Map<String,String[]> terms = new HashedMap();
                HashedMap TF = new HashedMap();
                HashedMap IDF = new HashedMap();
                Map<String,Integer> fieldLength = new HashedMap();

                properties.forEach((k,v) ->{
                    if(k.endsWith("Terms")){
                        terms.put(removeSuffix(k,"Terms"),(String[])v);
                    }
                    else if(k.endsWith("TF")){
                        TF.put(removeSuffix(k,"TF"), v);
                    }
                    else if(k.endsWith("IDF")){
                        IDF.put(removeSuffix(k,"IDF"), v);
                    }
                    else if(k.endsWith("Length")){
                        fieldLength.put(removeSuffix(k,"Length"),(int) v);
                    }
                });
                result.put((Long)node.getProperty("ref"),bm25fScore(terms, TF, IDF, fieldLength,fieldAvgLength, fieldNames,qDoc));
            }
        }

        Map<String, Double> nodeMap = sortResultInfo(result, db, 10);
        return nodeMap.entrySet().stream().map(ResultInfo::new);
    }

    public double tfField(int length, double avgL, int occurrence){
        double b = 0.75;
        return occurrence/(1+b*((length/avgL)-1));
    }

    public double tf(CardKeyword qkw, Map<String, String[]> terms, HashedMap occurrence, double boost, Map<String, Integer> length, Map<String, Object> fieldAvgLength, Map<String, Map<String,Integer>> termPosition, String[] fieldNames){
        AtomicReference<Double> sum = new AtomicReference<>(0.0);

        terms.forEach((k,v)->{
            if(termsStartsWith(v, qkw.getStem())) {
                int[] tfField = (int[]) occurrence.get(k);
                int tempOccurrence = tfField[termPosition.get(k).get(CURRENT_TERM)];

                double tfFieldScore = tfField(length.get(k), (Double) fieldAvgLength.get(k), tempOccurrence);

                sum.updateAndGet(v1 -> (v1 + boost * tfFieldScore));
            }
        });
        return sum.get();
    }

    public double bm25fScore(Map<String, String[]> terms, HashedMap occurrence, HashedMap idf, Map<String,Integer> length, Map<String, Object> fieldAvgLength, String[] fieldNames, Document query){
        double k1 = 1.2;
        AtomicReference<Double> sum = new AtomicReference<>(0.0);
        // Map with term (String) as key and index of term (Integer) as value
        Map<String, Map<String,Integer>> fieldTermPosition = new HashedMap();

        terms.forEach((k,v)->{
            Map<String, Integer> tempTermPos = new HashedMap();
            // Fill termPosition with terms and their corresponding index
            for (int i = 0; i < v.length; i++) {
                tempTermPos.put(terms.get(k)[i], i);
            }
            fieldTermPosition.put(k,tempTermPos);
        });


        for(CardKeyword qkw : query.keywords){
            AtomicReference<Double> tempIdf = new AtomicReference<>(0.0);
            AtomicBoolean alreadyChecked = new AtomicBoolean(false);

            AtomicReference<Double> tf = new AtomicReference<>((double) 0);
            terms.forEach((k,v)->{

                if(termsStartsWith(v, qkw.getStem())) {
                    double[] idfField = (double[]) idf.get(k);
                    tempIdf.getAndSet(idfField[fieldTermPosition.get(k).get(CURRENT_TERM)]);
                    tf.set(tf(qkw, terms, occurrence, 1, length, fieldAvgLength, fieldTermPosition, fieldNames));
                }

            });
            if (!alreadyChecked.get()) {
                alreadyChecked.set(true);
                sum.updateAndGet(v1 -> (v1 + tempIdf.get() * (tf.get() / (tf.get() + k1))));
            }
        }
        return sum.get();
    }

    private static String removeSuffix(final String s, final String suffix) {
        if (s != null && suffix != null && s.endsWith(suffix)){
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
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
