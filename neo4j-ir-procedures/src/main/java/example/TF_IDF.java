package example;

import keywords.Document;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.neo4j.procedure.Name;

import keywords.CardKeyword;


public class TF_IDF {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "example.tfidfscore", mode = Mode.WRITE)
    @Description("example.tfidfscores(altnames) - return the tf-idf score for nodes")
    public Stream<EntityField> tfidfscore(@Name("fetch") String input) throws IOException {
        try(Transaction tx = db.beginTx()){
            Map<Long, Document> docCollection = new HashedMap();

            // Delete old index and initialize new
            tx.execute("MATCH (n:TFIDF) detach delete n");
            tx.execute("CREATE (n:TFIDF)");


            // Retrieve nodes to index
            Result res = tx.execute(input);
            Iterator<Node> d_column = res.columnAs("d");

            // process terms
            while (d_column.hasNext()) {
                ArrayList<String> temp = new ArrayList<>();
                Node node = d_column.next();
                if (!node.getLabels().toString().equals("[TFIDF]")) {
                    node.getAllProperties().forEach((k, v) -> {
                        if (!k.equals("uri")) {
                            temp.add((String) v);
                        }
                    });

                    Document doc = new Document(temp.toString());
                    docCollection.put(node.getId(), doc);
                }
            }

            idf(docCollection);
            // finish result
            Map<CardKeyword, Double> result = new HashMap<>();
            docCollection.forEach((k, doc) -> doc.keywords.forEach(keyword -> result.put(keyword, keyword.getTfIdf())));

            // TODO: fikse dette på en bedre måte annet enn å laste inn på nytt.
            Result res1 = tx.execute(input);
            // start of write operation
            Iterator<Node> n_column = res1.columnAs("d");
            while(n_column.hasNext()){
                n_column.forEachRemaining(n -> {
                    if (!n.getLabels().toString().equals("[TFIDF]")) {
                        writeTFIDF(n, tx, docCollection);
                    }
                });
            }
            tx.commit();
            return result.entrySet().stream().map(EntityField::new);
        }

    }

    public void writeTFIDF(Node node, Transaction tx, Map<Long, Document> docCollection) {
        Document doc = docCollection.get(node.getId());

        Map<String, Double> tfidfValues = new HashMap<>();
        // prepare the values
        doc.keywords.forEach(k -> tfidfValues.put(k.getStem(), k.getTfIdf()));

        Map<String, Object> params = new HashMap<>();
        params.put("id", node.getId());
        params.put("tfidf", tfidfValues.toString());
        tx.execute("MATCH (n:TFIDF) SET n._"+node.getId() +"=$tfidf",params);

    }

    public static class EntityField {
        public String stem;
        public Double tfidf;

        public EntityField(Map.Entry<CardKeyword, Double> entity) {
            this.stem = entity.getKey().getStem();
            this.tfidf = entity.getValue();
        }
    }


    public static void idf(Map<Long, Document> docs) {
        int size = docs.size();
        docs.forEach((k, d) -> {

            for (CardKeyword keyword : d.keywords) {
                AtomicReference<Double> wordCount = new AtomicReference<>((double) 0);
                docs.forEach((k2, d2) -> {
                    Map<String, Integer> tempMap = d2.getWordCountMap();
                    if (tempMap.containsKey(keyword.getStem())) {
                        wordCount.getAndSet(wordCount.get() + 1);
                    }
                });
                double idf = Math.log(size / wordCount.get()) / Math.log(2); // divide on Math.log(2) to get base 2 logarithm
                keyword.setIdf(idf);
            }
        });
    }
}
