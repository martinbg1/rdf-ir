package example;

import keywords.Document;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Name;

import keywords.CardKeyword;



public class TF_IDF {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "example.tfidfscore")
    @Description("example.tfidfscores(altnames) - return the tf-idf score for nodes")
    public Stream<EntityField> tfidfscore(@Name("fetch") String input) throws IOException {
        try(Transaction tx = db.beginTx()){
            String altNames = tx.execute(input).resultAsString();
            Document doc = new Document(altNames);
            List<Document> docCollection = new ArrayList<>();
            docCollection.add(doc);
            idf(docCollection);
            Map<String, Double> result = new HashMap<>();
            doc.keywords.forEach(k -> result.put(k.getStem(), k.getTfIdf()));
            return result.entrySet().stream().map(EntityField::new);
        }

    }

    public static class EntityField {
        public String stem;
        public Object tfidf;

        public EntityField(Map.Entry<String, Double> entity) {
            this.stem = entity.getKey();
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
                double idf = 1 + Math.log(size / wordCount);
                keyword.setIdf(idf);
            }
        }
    }
}
