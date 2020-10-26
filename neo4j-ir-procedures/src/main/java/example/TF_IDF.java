package example;

import keywords.Document;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import org.neo4j.procedure.Name;

import keywords.CardKeyword;

import javax.print.Doc;


public class TF_IDF {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "example.tfidfscore", mode = Mode.WRITE)
    @Description("example.tfidfscores(altnames) - return the tf-idf score for nodes")
    public Stream<EntityField> tfidfscore(@Name("fetch") String input) throws IOException {
        try(Transaction tx = db.beginTx()){
            ArrayList<Document> docCollection = new ArrayList<>();
            Result res = tx.execute(input);
//            System.out.println("res 1: " +res.resultAsString());
//            Iterator<Node> n_column = res.columnAs("d");
//            System.out.println("ID: " + n_column.next().getId());

            while (res.hasNext()) {
                ArrayList<String> temp = new ArrayList<>();
                res.next().forEach((k,v)->temp.add(v.toString()));
                Document doc = new Document(temp.toString());
                docCollection.add(doc);
            }

            idf(docCollection);
            // finish result
            Map<CardKeyword, Double> result = new HashMap<>();
            docCollection.forEach(doc -> doc.keywords.forEach(k -> result.put(k, k.getTfIdf())));

            // TODO: fikse dette på en bedre måte annet enn å laste inn på nytt.
            Result res1 = tx.execute(input);
            // start of write operation
            Iterator<Node> n_column = res1.columnAs("d");
            while(n_column.hasNext()){
                n_column.forEachRemaining(n -> writeTFIDF(n, tx, docCollection));
            }
            tx.commit();
            return result.entrySet().stream().map(EntityField::new);
        }

    }

    public void writeTFIDF(Node node, Transaction tx, List<Document> docCollection) {
        Document doc = docCollection.get((int) node.getId());

        Map<String, Double> tfidfValues = new HashMap<>();
        // prepare the values
        doc.keywords.forEach(k -> tfidfValues.put(k.getStem(), k.getTfIdf()));
        Map<String, Object> params = new HashMap<>();
        params.put("id", node.getId());
        params.put("tfidf", tfidfValues.toString());

        tx.execute("MATCH (n) WHERE ID(n)=$id SET n.tfidf=$tfidf", params);
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
