package example;

import keywords.CorpusFielded;
import keywords.Document;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.neo4j.procedure.Name;

import keywords.CardKeyword;

import javax.print.Doc;


public class IndexRDFFielded {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "example.indexRDFFielded", mode = Mode.WRITE)
    @Description("example.indexRDFFielded(query) - return the tf-idf score for nodes")
    public Stream<EntityField> indexRDFFielded(@Name("fetch") String input) throws IOException {
        double documentLengthSum = 0.0  ;
        try(Transaction tx = db.beginTx()){
            // ArrayList<Document> accounts to a list of documents for each field.
            Map<Long, ArrayList<Document>> docCollection = new HashedMap();
            Map<Long, ArrayList<String>> docFieldNames = new HashedMap();

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
                ArrayList<Document> tempArrayDocument = new ArrayList<>();
                ArrayList<String> tempArrayField = new ArrayList<>();
                Node node = d_column.next();
                if (!node.getLabels().toString().equals("[Corpus]") && !node.getLabels().toString().equals("[IDF]")) {
                    node.getAllProperties().forEach((k, v) -> {
                        if (!k.equals("uri")) {
                            try {
                                tempArrayDocument.add(new Document((String)v, k));
                                tempArrayField.add(k);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            temp.add(tempArray);
                        }
                    });

//                    Document doc = new Document(temp.toString());
                    docCollection.put(node.getId(), tempArrayDocument);
                    docFieldNames.put(node.getId(), tempArrayField);

//                    documentLengthSum += doc.keywords.size();
                }
            }

            idf(docCollection);
            Map<String, ArrayList<Document>> fieldNameCollection = new HashMap<>();
            ArrayList<String> fieldNames = new ArrayList<>();
            docCollection.forEach((k, v) -> {

                ArrayList<Document> fields = new ArrayList<>();

                String fieldName = "";
                for (int i = 0; i < docFieldNames.get(k).size(); i++) {
                    fields.add(v.get(i));

                    fieldName = docFieldNames.get(k).get(i);
                    fieldNames.add(fieldName);
                    if (!fieldNameCollection.containsKey(fieldName)) {
                        fieldNameCollection.put(fieldName, new ArrayList<>());
                    }
                    fieldNameCollection.get(fieldName).add(v.get(i));
                }
            });
            CorpusFielded corpus = new CorpusFielded(fieldNameCollection, fieldNames);

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
//            while(n_column.hasNext()){
//                n_column.forEachRemaining(n -> {
//                    if (!n.getLabels().toString().equals("[Corpus]") && !n.getLabels().toString().equals("[IDF]")) {
//                        writeFieldIndexNode(n, tx, docCollection);
//                    }
//                });
//            }
//            double meanDocumentLength = documentLengthSum / docCollection.size();
//
//            HashMap<String, Object> params = new HashMap<>();
//            params.put("corpus", corpusfielded.getBoW().toArray());
//            params.put("idf", corpusfielded.getIdf());
//            params.put("meanLength", meanDocumentLength);



                for (int i = 0; i < corpus.getSize(); i++) {

                Map<String, Object> params = new HashedMap();
                params.put("corpus", corpus.getBoWByIndex(i).toArray());
                params.put("idf", corpus.getIdfByIndex(i).toArray());

                String fieldName = corpus.getFieldName(i);
                tx.execute("MATCH (n:Corpus) SET n." + fieldName + "=$corpus", params);
                tx.execute("MATCH (n:IDF) SET n." + fieldName + "=$idf", params);
                }
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
        docs.forEach((k, d) -> {
            for (ArrayList<Document> nodes : docs.values()) {
                for (Document field : nodes) {
                    AtomicInteger size = new AtomicInteger(0);
                    for (ArrayList<Document> nodesToCompare : docs.values()) {
                        for (Document fieldsToCompare : nodesToCompare) {
                            if (field.getFieldName().equals(fieldsToCompare.getFieldName())) {
                                size.getAndIncrement();
                            }
                        }
                    }

                    for(CardKeyword keyword : field.keywords) {
                        AtomicReference<Double> wordCount = new AtomicReference<>((double) 0);
                        docs.forEach((k2, d2) -> {
                            for (Document fieldToCompare : d2) {
                                Map<String, Integer> tempMap = fieldToCompare.getWordCountMap();
                                if (field.getFieldName().equals(fieldToCompare.getFieldName()) && tempMap.containsKey(keyword.getStem())) {
                                    wordCount.getAndSet(wordCount.get() + 1);
                                }
                            }

                        });

                        double idf = Math.log(size.get() / wordCount.get()) / Math.log(2); // divide on Math.log(2) to get base 2 logarithm
                        keyword.setIdf(idf);
                    }
                }

            }
        });
    }
}
