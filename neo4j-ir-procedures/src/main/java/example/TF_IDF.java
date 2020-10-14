package example;

import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import javax.ws.rs.core.Context;


public class TF_IDF {


    @Context
    public GraphDatabaseService db;

    @Procedure
    @Description("example.tfidfscores() - return the tf-idf score for nodes")
    public Stream<score> tfidfscores(){
        try(Transaction tx = db.beginTx()){
            var result = tx.execute("MATCH (d:Disease) RETURN d.name, d.altNames");
            return tx.noe();
        }
    }

    public static class score{

    }

    /**
     * UserFunction to calculate term frequency for terms
     * @param altnames
     * @return
     */
    @UserFunction
    public Map<String,Double> tf(
            @Name("altnames") String altnames) {
        Map<String, Double> termFreq = new HashMap<>();
        String[] terms = altnames.split(", ", 0);

        for (String term : terms) {
            double tf = tf(Arrays.asList(terms), term);
            termFreq.put(term, tf);
        }
        return termFreq;
    }

    /**
     * Helper function to find term frequency for a single term
     * @param doc
     * @param term
     * @return
     */
    public double tf(List<String> doc, String term) {
        double result = 0.0;
        for (String word : doc) {
            if (term.equalsIgnoreCase(word))
                result++;
        }
        return result / doc.size();
    }

    /**
     * UserFunction to calculate inverse document frequency for terms
     * @param documents
     * @return
     */
    @UserFunction
    public Map<String, Double> idf(
            @Name("documents") List<String> documents) {

        Map<String,Double> inverseDocFreqMap = new HashMap<>();
        List<String[]> parsedDocuments = new ArrayList<>();
        for (String doc : documents) {
            parsedDocuments.add(doc.split(", ", 0));
        }

        for (String[] doc : parsedDocuments) {
            for (String term : doc) {
                inverseDocFreqMap.put(term, idf(parsedDocuments, term));
            }
        }

        return inverseDocFreqMap;
    }


    /**
     * Helper function to calculate inverse document frequency for a single term
     * @param docs
     * @param term
     * @return
     */
    public double idf(List<String[]> docs, String term) {
        double n = 0;

        for(String[] doc : docs) {
            for (String word : doc) {
                if (term.equalsIgnoreCase(word)) {
                    n++;
                    break;
                }
            }
        }
        return Math.log(docs.size() / n);
    }


    /**
     * Funciton to calculate tf-idf score
     * @param documents
     * @return
     */
    @UserFunction
    public List<Map<String, Map<String, Double>>> tf_idf(
            @Name("documents") List<String> documents) {

        ArrayList<Map<String, Map<String, Double>>> result = new ArrayList<>();
        List<String[]> parsedDocuments = new ArrayList<>();

        for (String doc : documents) {
            parsedDocuments.add(doc.split(", ", 0));
        }

        for (String[] doc : parsedDocuments) {
            Map<String, Map<String, Double>> docResult = new HashMap<>();
            for (String term : doc) {
                Map<String, Double> tf_idf = new HashMap<>();

                // calculate tf and idf values
                // calculate tf and idf values
                Double tf = tf(Arrays.asList(doc), term);
                Double idf = idf(parsedDocuments, term);

                // Add values to terms in the document
                tf_idf.put("tf", tf);
                tf_idf.put("idf", idf);
                tf_idf.put("tf_idf", tf*idf);
                docResult.put(term, tf_idf);
            }
            // Add all term values for the document
            result.add(docResult);
        }
        return result;
    }
}
