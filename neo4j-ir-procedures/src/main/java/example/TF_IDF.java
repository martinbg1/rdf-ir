package example;

import keywords.Corpus;
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
            tx.execute("CREATE (n:Vectors)");
            tx.execute("CREATE (n:Corpus)");
            tx.execute("CREATE (n:IDF)");


            // Retrieve nodes to index
            Result res = tx.execute(input);
            Iterator<Node> d_column = res.columnAs("d");

            // process terms
            while (d_column.hasNext()) {
                ArrayList<String> temp = new ArrayList<>();
                Node node = d_column.next();
                if (!node.getLabels().toString().equals("[Vectors]") && !node.getLabels().toString().equals("[Corpus]") && !node.getLabels().toString().equals("[IDF]")) {
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
            Corpus corpus = new Corpus(docCollection);
            // finish result
            Map<CardKeyword, Double> result = new HashMap<>();
            docCollection.forEach((k, doc) -> doc.keywords.forEach(keyword -> result.put(keyword, keyword.getTfIdf())));

            // TODO: fikse dette på en bedre måte annet enn å laste inn på nytt.
            Result res1 = tx.execute(input);
            // start of write operation
            Iterator<Node> n_column = res1.columnAs("d");
            while(n_column.hasNext()){
                n_column.forEachRemaining(n -> {
                    if (!n.getLabels().toString().equals("[Vectors]") && !n.getLabels().toString().equals("[Corpus]") && !n.getLabels().toString().equals("[IDF]")) {
                        writeTFIDF(n, tx, docCollection, corpus);
                    }
                });
            }

            HashMap<String, Object> paramsCorpus = new HashMap<>();
            paramsCorpus.put("corpus", corpus.getBoW().toArray());
            paramsCorpus.put("idf", corpus.getIdf());
            tx.execute("MATCH (n:Corpus) SET n.corpus=$corpus", paramsCorpus);
            tx.execute("MATCH (n:IDF) SET n.idf=$idf", paramsCorpus);
            tx.commit();
            return result.entrySet().stream().map(EntityField::new);
        }

    }

    private static void writeTFIDF(Node node, Transaction tx, Map<Long, Document> docCollection, Corpus corpus) {
        Document doc = docCollection.get(node.getId());

        doc.setVector(corpus);

        List<String> terms= new ArrayList<>();
        doc.keywords.forEach((k) ->terms.add(k.getStem()));

        List<Double> idf = new ArrayList<>();
        doc.keywords.forEach((k) ->idf.add(k.getIdf()));

        List<Integer> tf = new ArrayList<>();
        doc.keywords.forEach((k) ->tf.add(k.getFrequency()));


        HashMap<String, Object> paramsVector = new HashMap();
        paramsVector.put("vector", doc.getVector());
        paramsVector.put("documentLength", doc.keywords.size());
        paramsVector.put("terms", terms.toArray());
        paramsVector.put("idf", idf.toArray());
        paramsVector.put("tf", tf.toArray());
        paramsVector.put("name", node.getId());

//        tx.execute("MATCH (n:Vectors) SET n._"+node.getId() +"=$vector", paramsVector);
        tx.execute("CREATE (n:indexNode {name: $name, vector: $vector, dl:$documentLength, terms: $terms, idf: $idf, tf: $tf})", paramsVector);
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
