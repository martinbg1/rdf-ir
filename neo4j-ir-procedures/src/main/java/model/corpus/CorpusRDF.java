package model.corpus;

import model.CardKeyword;
import model.Document;

import java.util.*;

import util.TFIDF_variations;

public class CorpusRDF {

    /**
     * Bag of words
     */
    private final List<String> BoW;

    /**
     * All idf values
     */
    private final ArrayList<Double> idf;

    /**
     * Key corresponds to term, value corresponds to how many documents containing that term.
     * Used to calculate idf values
     */
    private final HashMap<String, Double> documentWordCount;

    private int maxFrequency = 0;

    /**
     * Constructs an empty corpus
     */
    public CorpusRDF() {
        this.BoW = new LinkedList<>();
        this.idf = new ArrayList<>();
        this.documentWordCount = new HashMap<>();
    }

    /**
     * Initializing bag of words and idf values. Assumes that idf is already calculated
     * @param docCollection Every document in the corpus
     */
    public void initCorpusValues(Map<Long, Document> docCollection) {
        for (Document doc : docCollection.values()) {
            List<CardKeyword> keywords =  doc.keywords;
            for (CardKeyword kw : keywords) {
                if (!BoW.contains(kw.getStem())) {
                    BoW.add(kw.getStem());
                    idf.add(kw.getIdf());
                }
                if(kw.getFrequency() > maxFrequency){
                    maxFrequency = kw.getFrequency();
                }
            }
        }
    }

    /**
     * Calculate idf values for all the terms in all the Documents
     * @param docs Every document in the corpus
     */
    public void calculateIDF(Map<Long, Document> docs) {
        int size = docs.size();
        docs.forEach((k, d) -> {
            for (CardKeyword keyword : d.keywords) {
                double wordCount = this.getDocumentWordCount().get(keyword.getStem());
                double idf = TFIDF_variations.IDF_standard(wordCount, size); // divide on Math.log(2) to get base 2 logarithm
                System.out.println("Wordcount + size :\t" + wordCount + "\t" + size);
                System.out.println("Standard, " + keyword.getStem() + ": \t" + TFIDF_variations.IDF_standard(wordCount, size));
                System.out.println("Smooth, " + keyword.getStem() + ": \t" + TFIDF_variations.IDF_smooth(wordCount, size));
                System.out.println("Probabilistic, " + keyword.getStem() + ": \t" + TFIDF_variations.IDF_probabilitic(wordCount, size));
                keyword.setIdf(idf);
            }
        });
    }

    /**
     * Updates corpus word count
     * @param keyword keyword to update count
     */
    public void updateWordCount(CardKeyword keyword) {
        this.documentWordCount.merge(keyword.getStem(), 1.0, Double::sum);
    }

    public List<String> getBoW() {
        return this.BoW;
    }

    public ArrayList<Double> getIdf() {
        return this.idf;
    }

    public int getMaxFrequency(){
        return this.maxFrequency;
    }

    public HashMap<String, Double> getDocumentWordCount() {
        return this.documentWordCount;
    }

}
