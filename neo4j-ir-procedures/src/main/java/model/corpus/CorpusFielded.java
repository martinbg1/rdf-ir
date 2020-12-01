package model.corpus;

import model.CardKeyword;
import model.Document;
import model.NodeFields;
import util.TFIDF_variations;

import java.util.*;

public class CorpusFielded {

    /**
     * Bag of words
     */
    private final Map<String, List<String>> BoW;
    /**
     * All idf values
     */
    private final Map<String, ArrayList<Double>> idf;
    /**
     * List of all field names
     */
    private ArrayList<String> fieldNames;

    /**
     * Size of corpus
     */
    private int corpusSize;

    /**
     * Quantity of different field names
     */
    private int fieldSize;
    /**
     * Constructs an empty corpus
     */
    private final HashMap<String, Double> documentWordCount;
    private int maxFrequency;


    /**
     * Constructs empty corpus
     */
    public CorpusFielded() {
        this.BoW = new HashMap<>();
        this.idf = new HashMap<>();
        this.documentWordCount = new HashMap<>();

    }


    //TODO delete me :)
    public CorpusFielded(Map<String, ArrayList<Document>> fields, ArrayList<String> fieldNames) {
        this.BoW = new HashMap<>();
        this.idf = new HashMap<>();
        this.fieldNames = fieldNames;
        this.fieldSize = fieldNames.size();
        this.documentWordCount = new HashMap<>();

        fields.forEach((k, Node) -> {
            if (!this.BoW.containsKey(k)) {
                this.BoW.put(k, new LinkedList<>());
            }
            if (!this.idf.containsKey(k)) {
                this.idf.put(k, new ArrayList<>());
            }
            for (Document field : Node) {
                List<CardKeyword> keywords =  field.keywords;
                for (CardKeyword kw : keywords) {
                    if (!this.BoW.get(k).contains(kw.getStem())) {
                        this.BoW.get(k).add(kw.getStem());
                        this.idf.get(k).add(kw.getIdf());
                    }
                }
            }
        });

    }

    /**
     * Calculate idf values for all the terms in all the Documents
     * @param docs Every document in the corpus
     */
    public void calculateIDF(Map<Long, NodeFields> docs) {
        int size = docs.size();
        for (NodeFields fields : docs.values()) {
            for (Document field : fields.getFields()) {
                for (CardKeyword keyword : field.keywords) {
                    double wordCount = this.getDocumentWordCount().get(keyword.getStem());
                    double idf = TFIDF_variations.IDF_standard(wordCount, size); // divide on Math.log(2) to get base 2 logarithm
                    keyword.setIdf(idf);
                }
            }
        }
    }

    /**
     * Initializing bag of words and idf values. Assumes that idf is already calculate
     * @param fields Every field in the corpus
     */
    public void initCorpusValues(Map<String, ArrayList<Document>> fields) {
        this.corpusSize = fields.size();
        this.fieldNames = new ArrayList<>();
        fieldNames.addAll(fields.keySet());
        this.fieldSize = fieldNames.size();

        fields.forEach((k, Node) -> {
            if (!this.BoW.containsKey(k)) {
                this.BoW.put(k, new LinkedList<>());
            }
            if (!this.idf.containsKey(k)) {
                this.idf.put(k, new ArrayList<>());
            }
            for (Document field : Node) {
                List<CardKeyword> keywords =  field.keywords;
                for (CardKeyword kw : keywords) {
                    if (!this.BoW.get(k).contains(kw.getStem())) {
                        this.BoW.get(k).add(kw.getStem());
                        this.idf.get(k).add(kw.getIdf());
                    }
                    if(kw.getFrequency() > this.maxFrequency){
                        this.maxFrequency = kw.getFrequency();
                    }
                }
            }
        });
    }

    /**
     * Updates corpus word count
     * @param fields fields containing the keywords to update count
     */
    public void updateWordCount(ArrayList<Document> fields) {
        ArrayList<String> checkedTerms = new ArrayList<>();

        for (Document field : fields) {
            for (CardKeyword keyword : field.keywords) {
                if (!checkedTerms.contains(keyword.getStem())) {
                    this.documentWordCount.merge(keyword.getStem(), 1.0, Double::sum);
                    checkedTerms.add(keyword.getStem());
                }
            }
        }
    }

    public Map<String, List<String>> getBoW() {
        return this.BoW;
    }

    public Map<String, ArrayList<Double>> getIdf() {
        return this.idf;
    }

    public HashMap<String, Double> getDocumentWordCount() {
        return this.documentWordCount;
    }

    public void setFieldNames(ArrayList<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public List<String> getBoWByIndex(int i) {
        return BoW.get(this.fieldNames.get(i));
    }

    public List<String> getBoWByKey(String key) {
        return BoW.get(key);
    }

    public int getMaxFrequency(){
        return this.maxFrequency;
    }

    public void updateMaxFrequency(int freq){
        if(freq > this.maxFrequency){
            this.maxFrequency = freq;
        }
    }

    public List<Double> getIdfByKey(String key) {
        return idf.get(key);
    }

    public List<Double> getIdfByIndex(int i) {
        return idf.get(this.fieldNames.get(i));
    }

    public String getFieldName(int i) {
        return this.fieldNames.get(i);
    }

    public int getFieldSize() {
        return this.fieldSize;
    }

}
