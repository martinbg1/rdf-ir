package keywords;

import java.util.*;

public class CorpusRDF {

    private final List<String> BoW;
    private final ArrayList<Double> idf;
    private final HashMap<String, Double> documentWordCount;

    public CorpusRDF() {
        this.BoW = new LinkedList<>();
        this.idf = new ArrayList<>();
        this.documentWordCount = new HashMap<>();
    }

    public List<String> getBoW() {
        return this.BoW;
    }


    public ArrayList<Double> getIdf() {
        return this.idf;
    }

    public HashMap<String, Double> getDocumentWordCount() {
        return this.documentWordCount;
    }


    public String getBoWByIndex(int i) {
        return this.BoW.get(i);
    }


    public int getCorpusSize() {
        return this.BoW.size();
    }

    public void updateWordCount(CardKeyword keyword) {
        this.documentWordCount.merge(keyword.getStem(), 1.0, Double::sum);
    }

    public void initCorpusValues(Map<Long, Document> docCollection) {
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

    public void calculateIDF(Map<Long, Document> docs) {
        int size = docs.size();
        docs.forEach((k, d) -> {
            for (CardKeyword keyword : d.keywords) {
                double wordCount = this.getDocumentWordCount().get(keyword.getStem());
                double idf = Math.log((size / wordCount)) / Math.log(2); // divide on Math.log(2) to get base 2 logarithm
                keyword.setIdf(idf);
            }
        });
    }
}
