package example;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.*;

public class TF {

    public HashMap<String,Double> termfreq;
    @UserFunction
    public HashMap<String,Double> terms(
            @Name("altnames") String altnames) {
        HashMap<String,Double> termFreq = new HashMap<String,Double>();
        String[] terms = altnames.split("[,]", 0);

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
