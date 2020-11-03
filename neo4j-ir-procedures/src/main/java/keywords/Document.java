package keywords;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Document {

    public List<CardKeyword> keywords;
    private HashMap<String, Integer> wordCountMap;
    private double[] vector;

    public Document(String field) throws IOException {
        this.keywords = KeywordsExtractor.getKeywordsList(field);
        this.wordCountMap = new HashMap<>();

        for (CardKeyword keyword : this.keywords) {
            this.wordCountMap.put(keyword.getStem(), keyword.getFrequency());
        }
    }

    public HashMap<String, Integer> getWordCountMap() {
        return this.wordCountMap;
    }

    public void setVector(Corpus corpus) {
        List<String> BoW = corpus.getBoW();
        this.vector = new double[BoW.size()];
        for (int i = 0; i < BoW.size(); i++) {
            double vectorValue = 0.0;
            for (int j = 0; j < this.keywords.size(); j++) {
                if (BoW.get(i).equals(this.keywords.get(j).getStem())) {
                    vectorValue = this.keywords.get(j).getTfIdf();
                }
            }
            this.vector[i] = vectorValue;
        }
    }

    public double[] getVector()  {
        return new double[] {};
    }

    public void initializeVector(int size) {
        this.vector = new double[size];
    }

    public void setQueryValue(double vectorValue, int index) {
        this.vector[index] = vectorValue;
    }
}

