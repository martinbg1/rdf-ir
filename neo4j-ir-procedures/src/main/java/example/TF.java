package example;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.*;

public class TF {


    @UserFunction
    public Map<String,Double> terms(
            @Name("altnames") String altnames) {
        Map<String, Double> termFreq = new HashMap<>();
        String[] terms = altnames.split(", ", 0);

        for (String term : terms) {
            double tf = tf(Arrays.asList(terms), term);
            termFreq.put(term, tf);
        }
        return termFreq;
    }

    public double tf(List<String> doc, String term) {
        double result = 0.0;
        for (String word : doc) {
            if (term.equalsIgnoreCase(word))
                result++;
        }
        return result / doc.size();
    }

}
