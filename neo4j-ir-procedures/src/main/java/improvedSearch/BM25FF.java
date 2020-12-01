package improvedSearch;

import model.CardKeyword;
import model.Document;
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

import static util.ResultUtil.sortResultInfo;

public class BM25FF {
    @Context
    public GraphDatabaseService db;

    // static field to manage what term a query keyword compared to. This is needed because we use startsWith instead of
    // equals so we cannot guarantee that the CURRENT_TERM and query keyword is exactly the same
    private static String CURRENT_TERM;

    private static double k1;
    private static final Map<String, Double> b = new HashMap<>();
    private static final Map<String, Double> boost = new HashMap<>();

    @Procedure
    @Description("improvedSearch.bm25ffSearch(query) - returns bm25ff query result")
    public Stream<ResultInfo> bm25ffSearch(@Name("fetch") String query) throws IOException {
        Map<Long, Double> result = new LinkedHashMap<>();
        // Query document
        Document qDoc = new Document(query);

        try(Transaction tx = db.beginTx()) {
            // get all indexNodes for fields with (terms, idf, tf og fl)
            ResourceIterator<Object> res = tx.execute("MATCH (n:fieldNewIndexNode) return n").columnAs("n");

            Node parameters = (Node) tx.execute("MATCH (n:ParametersFielded) return n").columnAs("n").next();
            parameters.getAllProperties().forEach((k,v)->{
                if(k.equals("k1")){
                    k1 = (double) v;
                }
                else if(k.endsWith("_b")){
                    b.put(removeSuffix(k,"_b"),(double) v);
                }
                else if(k.endsWith("_boost")){
                    boost.put(removeSuffix(k,"_boost"),(double) v);
                }
            });

            // retrieve mean field length
            Node fieldDataStats = (Node) tx.execute("MATCH (n:DataStats) return n").columnAs("n").next();

            Map<String, Object> fieldAvgLength = fieldDataStats.getAllProperties();

            // fill result with a node and its corresponding bm25ff score
            while(res.hasNext()){
                Node node = (Node) res.next();
                Map<String, Object> properties = node.getAllProperties();

                Map<String,String[]> terms = new HashMap<>();
                HashedMap TF = new HashedMap();
                HashedMap IDF = new HashedMap();
                Map<String,Integer> fieldLength = new HashMap<>();

                properties.forEach((k,v) ->{
                    if(k.endsWith("Terms")){
                        terms.put(removeSuffix(k,"Terms"),(String[])v);
                    }
                    else if(k.endsWith("TF")){
                        TF.put(removeSuffix(k,"TF"), v);
                    }
                    else if(k.endsWith("LocalIDF")){
                        IDF.put(removeSuffix(k,"LocalIDF"), v);
                    }
                    else if(k.endsWith("Length")){
                        fieldLength.put(removeSuffix(k,"Length"),(int) v);
                    }
                });
                result.put((Long)node.getProperty("ref"),bm25ffScore(terms, TF, IDF, fieldLength,fieldAvgLength, qDoc));
            }
        }

        Map<String, Double> nodeMap = sortResultInfo(result, db, 10);
        return nodeMap.entrySet().stream().map(ResultInfo::new);
    }

    /**
     *
     * @param terms - Map<String, String-array> with fieldName and an array with the corresponding terms in each field.
     * @param occurrence - HashedMap with fieldName and the occurrence of a term in a that field
     * @param idf - HashedMap with fieldName and inverse term frequency (idf) scores
     * @param length - Map<String,Integer> with fieldName and the corresponding length of the field
     * @param fieldAvgLength - Map<String, Object> with fieldName and the corresponding average length
     * @param query - (Document) query document with query terms as keywords
     * @return - (double) returns the summed bm25ff score for all the terms per field in a document (node)
     */
    public double bm25ffScore(Map<String, String[]> terms, HashedMap occurrence, HashedMap idf, Map<String,Integer> length, Map<String, Object> fieldAvgLength, Document query){
        AtomicReference<Double> sum = new AtomicReference<>(0.0);
        // Map with term (String) as key and index of term (Integer) as value
        Map<String, Map<String,Integer>> fieldTermPosition = new HashMap<>();

        terms.forEach((k,v)->{
            Map<String, Integer> tempTermPos = new HashMap<>();
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
                    tf.set(tf(qkw, terms, occurrence, length, fieldAvgLength, fieldTermPosition));
                }

            });
            if (!alreadyChecked.get()) {
                alreadyChecked.set(true);
                sum.updateAndGet(v1 -> (v1 + tempIdf.get() * (tf.get() / (tf.get() + k1))));
            }
        }
        return sum.get();
    }

    public double tf(CardKeyword qkw, Map<String, String[]> terms, HashedMap occurrence, Map<String, Integer> length, Map<String, Object> fieldAvgLength, Map<String, Map<String,Integer>> termPosition){
        AtomicReference<Double> sum = new AtomicReference<>(0.0);

        terms.forEach((k,v)->{
            if(termsStartsWith(v, qkw.getStem())) {
                int[] tfField = (int[]) occurrence.get(k);
                int tempOccurrence = tfField[termPosition.get(k).get(CURRENT_TERM)];
                double tfFieldScore = tfField(length.get(k), (Double) fieldAvgLength.get(k), tempOccurrence, k);

                sum.updateAndGet(v1 -> (v1 + boost.get(k) * tfFieldScore));
            }
        });
        return sum.get();
    }

    public double tfField(int length, double avgL, int occurrence, String fieldName){
        double bField = b.get(fieldName);
        return occurrence/(1+bField*((length/avgL)-1));
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
