package example;

import keywords.Document;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Name;

import keywords.CardKeyword;

import javax.print.Doc;


public class TF_IDF {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "example.tfidfscore")
    @Description("example.tfidfscores(altnames) - return the tf-idf score for nodes")
    public Stream<EntityField> tfidfscore(@Name("fetch") String input) throws IOException {
        try(Transaction tx = db.beginTx()){
            ArrayList<Document> docCollection = new ArrayList<>();
            Result res = tx.execute(input);
            while (res.hasNext()) {
                ArrayList<String> temp = new ArrayList<>();
                res.next().forEach((k,v)->temp.add(v.toString()));
                Document doc = new Document(temp.toString());
                docCollection.add(doc);
            }

            idf(docCollection);
            Map<CardKeyword, Double> result = new HashMap<>();
            docCollection.forEach(doc -> doc.keywords.forEach(k -> result.put(k, k.getTfIdf())));
            System.out.println(result.size());
            return result.entrySet().stream().map(EntityField::new);
        }

    }

    public static class EntityField {
        public String stem;
        public Double tfidf;

        public EntityField(Map.Entry<CardKeyword, Double> entity) {
            this.stem = entity.getKey().getStem();
            this.tfidf = entity.getValue();
        }
    }


    public static void idf(List<Document> docs) {
        int size = docs.size();
        for (Document doc : docs) {
            for (CardKeyword keyword : doc.keywords) {
                double wordCount = 0;
                for (Document docProperties : docs) {
                    Map<String, Integer> tempMap = docProperties.getWordCountMap();
                    if (tempMap.containsKey(keyword.getStem())) {
                        wordCount++;
                    }
                }
                double idf = Math.log(size / wordCount) / Math.log(2); // divide on Math.log(2) to get base 2 logarithm
                keyword.setIdf(idf);
            }
        }
    }
}
