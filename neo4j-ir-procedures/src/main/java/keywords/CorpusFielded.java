package keywords;

import org.apache.commons.collections.map.HashedMap;

import java.lang.reflect.Array;
import java.util.*;

public class CorpusFielded {

    private Map<String, List<String>> BoW;
    private Map<String, ArrayList<Double>> idf;
    private ArrayList<String> fieldNames;
    private int corpusSize;
    private int fieldSize;

    public CorpusFielded(Map<String, ArrayList<Document>> fields, ArrayList<String> fieldNames) {
        this.BoW = new HashedMap();
        this.idf = new HashedMap();
        this.fieldNames = fieldNames;
        this.corpusSize = fields.size();
        this.fieldSize = fieldNames.size();

        fields.forEach((k, Node) -> {
            if (!BoW.containsKey(k)) {
                BoW.put(k, new LinkedList<>());
            }
            if (!idf.containsKey(k)) {
                idf.put(k, new ArrayList<>());
            }
            for (Document field : Node) {
                List<CardKeyword> keywords =  field.keywords;
                for (CardKeyword kw : keywords) {
                    if (!BoW.get(k).contains(kw.getStem())) {
                        BoW.get(k).add(kw.getStem());
                        idf.get(k).add(kw.getIdf());
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


}
