package model;

import model.corpus.CorpusRDF;
import util.KeywordsExtractor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Document {

    /**
     * List of all keywords in the Document
     */
    public List<CardKeyword> keywords;

    /**
     * Count of all keywords in the document
     */
    private final HashMap<String, Integer> wordCountMap;

    /**
     * TF IDF vector of the document
     */
    private double[] vector;

    /**
     * Name of the field the Document belongs to (only initilized in fielded variants)
     */
    private String fieldName;

    /**
     * Length of document
     */
    private final int docLength;


    /**
     * Constructor for a simple independent Document
     * @param field String to be converted to a document
     * @throws IOException
     */
    public Document(String field) throws IOException {
        this.keywords = KeywordsExtractor.getKeywordsList(field);
        this.wordCountMap = new HashMap<>();

        for (CardKeyword keyword : this.keywords) {
            this.wordCountMap.put(keyword.getStem(), keyword.getFrequency());
        }
        this.docLength = this.wordCountMap.values().stream().reduce(0, Integer::sum);

    }

    /**
     * Constructor for a Document that belongs to a corpus
     * @param field String to be converted to a document
     * @param corpus corpus the Document belongs to
     * @throws IOException
     */
    public Document(String field, CorpusRDF corpus) throws IOException {
        this.keywords = KeywordsExtractor.getKeywordsList(field);
        this.wordCountMap = new HashMap<>();

        for (CardKeyword keyword : this.keywords) {
            this.wordCountMap.put(keyword.getStem(), keyword.getFrequency());
            corpus.updateWordCount(keyword);
            corpus.updateMaxFrequency(keyword.getFrequency());
        }
        this.docLength = this.wordCountMap.values().stream().reduce(0, Integer::sum);

    }


    /**
     * Fielded variant of the simple constructor. May belong to a corpus defined in the NodeFields class
     * @param field String to be converted to a document
     * @param fieldName Name of the field
     * @throws IOException
     */
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

