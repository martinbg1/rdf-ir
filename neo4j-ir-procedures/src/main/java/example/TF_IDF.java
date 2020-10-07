package example;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.*;

public class TF_IDF {


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
        double i = 10 / 3;
        return Math.log(docs.size() / n);
    }
}
