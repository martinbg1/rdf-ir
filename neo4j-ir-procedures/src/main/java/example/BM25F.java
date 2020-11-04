package example;

import keywords.CardKeyword;
import keywords.Document;

import java.util.ArrayList;
import java.util.List;

public class BM25F {

    public double tfField(CardKeyword term, Document Field){
        double b = 0.75;
        double fieldLength = Field.keywords.size();
        double avgl = 12;
        int occurence = term.getFrequency();

        return occurence/(1+b*((fieldLength/avgl)-1));
    }

    public double tf(CardKeyword kw, List<Document> fields, double boost){
        double sum = 0.0;

        for (int i = 0; i < fields.size(); i++) {
            for(CardKeyword k1 : fields.get(i).keywords){
                if(k1.equals(kw)){
                    sum += boost*tfField(kw, fields.get(i));
                }
            }
        }

        return sum;
    }

    public double BM25F(ArrayList<Document> fields, Document query){
        double k1 = 1.2;
        double sum = 0.0;

        for(CardKeyword qkw : query.keywords){
            double idf = qkw.getIdf();
            double tf = tf(qkw, fields, 1);
            sum += idf*(tf/(tf+k1));
        }

        return sum;
    }

}
