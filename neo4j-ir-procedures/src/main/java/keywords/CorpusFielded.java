package keywords;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CorpusFielded {

    private List<List<String>> BoW;
    private ArrayList<ArrayList<Double>> idf;

    public CorpusFielded(Map<Long, ArrayList<Document>> docCollection) {
        this.BoW = new LinkedList<>();
        this.idf = new ArrayList<>();
        for(ArrayList<Document> doc : docCollection.values()){
            for (Document field : doc) {
                List<CardKeyword> keywords =  field.keywords;
                for (CardKeyword kw : keywords) {
                    ArrayList<String> fieldkw = new ArrayList<>();
                    ArrayList<Double> fieldidf = new ArrayList<>();
                    for(List<String> f : BoW){
                        if(!f.contains(kw.getStem())){
                            fieldkw.add(kw.getStem());
                            fieldidf.add(kw.getIdf());
                        }
                    }
                    BoW.add(fieldkw);
                    idf.add(fieldidf);
                }
            }

        }
    }

    public List<List<String>> getBoW() {
        return BoW;
    }

    public ArrayList<ArrayList<Double>> getIdf() {
        return idf;
    }
}
