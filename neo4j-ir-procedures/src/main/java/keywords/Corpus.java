package keywords;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Corpus {

    private List<String> BoW;
    private ArrayList<Double> idf;

    public Corpus(Map<Long, Document> docCollection) {
        this.BoW = new LinkedList<>();
        this.idf = new ArrayList<>();

        for (Document doc : docCollection.values()) {
            List<CardKeyword> keywords =  doc.keywords;
            for (CardKeyword kw : keywords) {
                if (!BoW.contains(kw.getStem())) {
                    BoW.add(kw.getStem());
                    idf.add(kw.getIdf());
                }
            }
        }
    }

    public List<String> getBoW() {
        return BoW;
    }

    public List<Double> getIdf() {
        return idf;
    }
}
