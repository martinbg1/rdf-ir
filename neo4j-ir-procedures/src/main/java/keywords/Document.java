package keywords;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Document {

    public List<CardKeyword> keywords;
    private HashMap<String, Integer> wordCountMap;
    private double[] vector;
    private String fieldName;
    private int docLength;

    public Document(String field) throws IOException {
        this.keywords = KeywordsExtractor.getKeywordsList(field);
        this.wordCountMap = new HashMap<>();

        for (CardKeyword keyword : this.keywords) {
            this.wordCountMap.put(keyword.getStem(), keyword.getFrequency());
        }
        this.docLength = this.wordCountMap.values().stream().reduce(0, Integer::sum);

    }

    public Document(String field, String fieldName) throws IOException {
        this.keywords = KeywordsExtractor.getKeywordsList(field);
        this.wordCountMap = new HashMap<>();
        this.fieldName = fieldName;

        for (CardKeyword keyword : this.keywords) {
            this.wordCountMap.put(keyword.getStem(), keyword.getFrequency());
        }
        this.docLength = this.wordCountMap.values().stream().reduce(0, Integer::sum);
    }


    public HashMap<String, Integer> getWordCountMap() {
        return this.wordCountMap;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
    }

    public double[] getVector()  {
        return this.vector;
    }

    public void initializeVector(int size) {
        this.vector = new double[size];
    }

    public void setVectorValue(double vectorValue, int index) {
        this.vector[index] = vectorValue;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public int getDocLength() {
        return this.docLength;
    }

    public String[] toStringArray(){
        String[] str = new String[this.keywords.size()];
        for (int i = 0; i < this.keywords.size(); i++) {
            str[i] = this.keywords.get(i).getStem();
        }
        return str;
    }
}

