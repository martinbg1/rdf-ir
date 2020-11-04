package example;

import keywords.Corpus;
import keywords.CorpusFielded;
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

import static dbUtil.indexWriter.writeIndexNode;
import static dbUtil.indexWriter.writeFieldIndexNode;


public class IndexRDFFielded {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "example.indexRDFFielded", mode = Mode.WRITE)
    @Description("example.indexRDFFielded(query) - return the tf-idf score for nodes")
    public Stream<EntityField> indexRDFFielded(@Name("fetch") String input) throws IOException {
        double documentLengthSum = 0.0  ;
        try(Transaction tx = db.beginTx()){
            Map<Long, ArrayList<Document>> docCollection = new HashedMap();

            // Delete old index and initialize new
            tx.execute("MATCH (i:indexNode), (c:Corpus), (idf:IDF) detach delete i, c, idf ");
            tx.execute("CREATE (n:Corpus)");
            tx.execute("CREATE (n:IDF)");


            // Retrieve nodes to index
            Result res = tx.execute(input);
            Iterator<Node> d_column = res.columnAs("d");

            // TODO handle null values
            // process terms
            while (d_column.hasNext()) {
//                ArrayList<ArrayList<Document>> temp = new ArrayList<>();
                ArrayList<Document> tempArray = new ArrayList<>();
                Node node = d_column.next();
                if (!node.getLabels().toString().equals("[Corpus]") && !node.getLabels().toString().equals("[IDF]")) {
                    node.getAllProperties().forEach((k, v) -> {
                        if (!k.equals("uri")) {
                            try {
                                tempArray.add(new Document((String)v));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            temp.add(tempArray);
                        }
                    });

//                    Document doc = new Document(temp.toString());
                    docCollection.put(node.getId(), tempArray);

//                    documentLengthSum += doc.keywords.size();
                }
            }

            idf(docCollection);
            CorpusFielded corpusfielded = new CorpusFielded(docCollection);
            // finish result
            Map<CardKeyword, Double> result = new HashMap<>();
            docCollection.forEach((k, docs) -> {
                for(Document field : docs){
                    field.keywords.forEach(keyword -> result.put(keyword, keyword.getTfIdf()));
                }
            });

            // TODO: fikse dette på en bedre måte annet enn å laste inn på nytt.
            Result res1 = tx.execute(input);
            // start of write operation
            Iterator<Node> n_column = res1.columnAs("d");
            while(n_column.hasNext()){
                n_column.forEachRemaining(n -> {
                    if (!n.getLabels().toString().equals("[Corpus]") && !n.getLabels().toString().equals("[IDF]")) {
                        writeFieldIndexNode(n, tx, docCollection);
                    }
                });
            }
//            double meanDocumentLength = documentLengthSum / docCollection.size();

//            HashMap<String, Object> params = new HashMap<>();
//            params.put("corpus", corpus.getBoW().toArray());
//            params.put("idf", corpus.getIdf());
//            params.put("meanLength", meanDocumentLength);
//            tx.execute("MATCH (n:Corpus) SET n.corpus=$corpus", params);
//            tx.execute("MATCH (n:IDF) SET n.idf=$idf", params);
//            tx.execute("CREATE (n:DataStats {meanDocumentLength: $meanLength})", params);



            tx.commit();
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


    public static void idf(Map<Long, ArrayList<Document>> docs) {
        int size = docs.size();
        docs.forEach((k, d) -> {
            for(Document field : d){
                for (CardKeyword keyword : field.keywords) {
                    AtomicReference<Double> wordCount = new AtomicReference<>((double) 0);
                    docs.forEach((k2, d2) -> {
                        d2.forEach(d3->{
                            Map<String, Integer> tempMap = d3.getWordCountMap();
                            if (tempMap.containsKey(keyword.getStem())) {
                                wordCount.getAndSet(wordCount.get() + 1);
                            }
                        });
                    });
                    double idf = Math.log(size / wordCount.get()) / Math.log(2); // divide on Math.log(2) to get base 2 logarithm
                    keyword.setIdf(idf);
                }
            }
        });
    }
}
