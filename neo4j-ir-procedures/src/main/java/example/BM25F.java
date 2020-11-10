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
import result.ResultNode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static result.ResultUtil.sortResult;

public class BM25F {
    @Context
    public GraphDatabaseService db;

    @Procedure
    @Description("example.bm25fSearch(query) - returns bm25f query result")
    public Stream<ResultNode> bm25fSearch(@Name("fetch") String query) throws IOException {
        Map<Long, Double> result = new LinkedHashMap<>();
        // Query document
        Document qDoc = new Document(query);

        try(Transaction tx = db.beginTx()) {
            // get all indexNodes for fields with (terms, idf, tf og fl)
            ResourceIterator<Object> res = tx.execute("MATCH (n:indexNode) return n").columnAs("n");


            // retrieve mean field length
            Node fieldDataStats = (Node) tx.execute("MATCH (n:DataStats) return n").columnAs("n").next();
            String[] fieldNames = (String[]) tx.execute("MATCH (n:Corpus) return n.fieldName").columnAs("n.fieldName").next();

            Map<String, Object> fieldAvgLength = fieldDataStats.getAllProperties();

            // fill result with a node and its corresponding bm25f score
            while(res.hasNext()){
                Node node = (Node) res.next();
                Map<String, Object> properties = node.getAllProperties();

                Map<String,String[]> terms = new HashedMap();
                Map<String,int[]> TF = new HashedMap();
                Map<String,double[]> IDF = new HashedMap();
                Map<String,Integer> fieldLength = new HashedMap();

                properties.forEach((k,v) ->{
                    if(k.endsWith("Terms")){
                        terms.put(removeSuffix(k,"Terms"),(String[])v);
                    }
                    else if(k.endsWith("TF")){
                        TF.put(removeSuffix(k,"TF"),(int[])v);
                    }
                    else if(k.endsWith("IDF")){
                        IDF.put(removeSuffix(k,"IDF"),(double[])v);
                    }
                    else if(k.endsWith("Length")){
                        fieldLength.put(removeSuffix(k,"Length"),(int)v);
                    }
                });
                result.put((Long)node.getProperty("ref"),bm25fScore(terms, TF, IDF, fieldLength,fieldAvgLength, fieldNames,qDoc));
            }
        }

        Map<Node, Double> nodeMap = sortResult(result, db, 10);
        return nodeMap.entrySet().stream().map(ResultNode::new);
    }

    public double tfField(int length, double avgL, int occurrence){
        double b = 0.75;
        // TODO fiks sånn at det fungerer med fields som ikke er i alle noder
        return occurrence/(1+b*((length/avgL)-1));
    }

    public double tf(CardKeyword qkw, Map<String, String[]> terms, Map<String,int[]> occurrence, double boost, Map<String, Integer> length, Map<String, Object> fieldAvgLength, Map<String, Map<String,Integer>> termPosition, String[] fieldNames){
        AtomicReference<Double> sum = new AtomicReference<>(0.0);

        terms.forEach((k,v)->{
            if(Arrays.asList(v).contains(qkw.getStem())) {
                int tempOccurrence = occurrence.get(k)[termPosition.get(k).get(qkw.getStem())];

                // TODO legge inn (Double) fieldAvgLength.get(k) for avgLength
                double tfField = tfField(length.get(k), 12, tempOccurrence);

                sum.updateAndGet(v1 -> (v1 + boost * tfField));
            }
        });
        return sum.get();
    }

    public double bm25fScore(Map<String, String[]> terms, Map<String,int[]> occurrence, Map<String,double[]> idf, Map<String,Integer> length, Map<String, Object> fieldAvgLength, String[] fieldNames, Document query){
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

            terms.forEach((k,v)->{

                if(Arrays.asList(v).contains(qkw.getStem())) {
                    tempIdf.getAndSet(idf.get(k)[fieldTermPosition.get(k).get(qkw.getStem())]);
                }
                double tf = tf(qkw, terms, occurrence, 1, length, fieldAvgLength, fieldTermPosition, fieldNames);
                sum.updateAndGet(v1 -> (v1 + tempIdf.get() * (tf / (tf + k1))));
            });
        }
        return sum.get();
    }

    public static String removeSuffix(final String s, final String suffix) {
        if (s != null && suffix != null && s.endsWith(suffix)){
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }

}
