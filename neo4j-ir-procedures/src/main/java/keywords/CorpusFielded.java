package keywords;

import org.apache.commons.collections.map.HashedMap;

import java.lang.reflect.Array;
import java.util.*;

public class CorpusFielded {

    private final Map<String, List<String>> BoW;
    private final Map<String, ArrayList<Double>> idf;
    private ArrayList<String> fieldNames;
    private int corpusSize;
    private int fieldSize;
    private final HashMap<String, Double> documentWordCount;

    public CorpusFielded() {
        this.BoW = new HashMap<>();
        this.idf = new HashMap<>();

//        this.fieldNames = fieldNames;
//        this.fieldSize = fieldNames.size();
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
    public Map<String, List<String>> getBoW() {
        return null;
    }
    public Map<String, ArrayList<Double>> getIdf() {
        return null;
    }
    public HashMap<String, Double> getDocumentWordCount() {
        return this.documentWordCount;
    }

    public void setFieldNames(ArrayList<String> fieldNames) {
        this.fieldNames = fieldNames;
    }



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

    public void initCourpusValues(Map<String, ArrayList<Document>> fields) {
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
                }
            }
        });
    }

    public List<String> getBoWByIndex(int i) {
        return BoW.get(this.fieldNames.get(i));
    }

    public List<String> getBoWByKey(String key) {
        return BoW.get(key);
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

    public int getCorpusSize() {
        return this.corpusSize;
    }

    public void calculateIDF(Map<Long, NodeFields> docs) {
        int size = docs.size();
        docs.forEach((k, d) -> {
            for (NodeFields fields : docs.values()) {
                for (Document field : fields.getFields()) {
                    for (CardKeyword keyword : field.keywords) {
                        double wordCount = this.getDocumentWordCount().get(keyword.getStem());
                        double idf = Math.log((size / wordCount)) / Math.log(2); // divide on Math.log(2) to get base 2 logarithm
                        keyword.setIdf(idf);
                    }
                }
            }
        });
    }
}
