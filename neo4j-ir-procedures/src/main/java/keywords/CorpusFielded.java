package keywords;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CorpusFielded {

    private List<String> BoW;
    private ArrayList<Double> idf;
    private String fieldName;

    public CorpusFielded(Map<Long, ArrayList<Document>> fields, String fieldName) {
        this.BoW = new LinkedList<>();
        this.idf = new ArrayList<>();
        this.fieldName = fieldName;

        // TODO Ekstra loop som går gjennom hvert field
        for (Document field : fields.values()) {
            List<CardKeyword> keywords =  field.keywords;
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
