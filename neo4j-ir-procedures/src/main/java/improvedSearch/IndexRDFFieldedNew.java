package improvedSearch;

import model.corpus.CorpusFielded;
import model.Document;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.neo4j.procedure.Name;

import model.corpus.CardKeyword;
import result.SingleResult;

import static util.IndexDeleter.prepareRDFFieldedNewIndex;
import static util.IndexWriter.writeFieldIndexNodeTest;


public class IndexRDFFieldedNew {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "improvedSearch.indexRDFFieldedNew", mode = Mode.WRITE)
    @Description("improvedSearch.indexRDFFieldedNew(query) - return information of if indexing was a success or failure")
    public Stream<SingleResult> indexRDFFieldedNew(@Name("fetch") String input) throws IOException {

        // Prepare db to be indexed by deleting old indexNodes
        try(Transaction tx = db.beginTx()) {
            prepareRDFFieldedNewIndex(tx);
        }
        Map<String, Double> fieldLengthSum = new HashMap<>();
        Map<String, Double> meanFieldLengths = new HashMap<>();
        try(Transaction tx = db.beginTx()){
            // ArrayList<Document> accounts to a list of documents for each field.
            Map<Long, ArrayList<Document>> docCollection = new HashMap<>();
            Map<Long, ArrayList<String>> docFieldNames = new HashMap<>();

            // Retrieve nodes to index
            Result res = tx.execute(input);
            Iterator<Node> d_column = res.columnAs("d");

            // TODO handle null values
            // process terms
            while (d_column.hasNext()) {
                ArrayList<Document> tempArrayDocument = new ArrayList<>();
                ArrayList<String> tempArrayField = new ArrayList<>();
                Node node = d_column.next();
                node.getAllProperties().forEach((k, v) -> {
                    if (!k.equals("uri")) {
                        try {
                            Document field = new Document((String)v, k);
                            tempArrayDocument.add(field);
                            tempArrayField.add(k);
                            if (!fieldLengthSum.containsKey(k)) {
                                fieldLengthSum.put(k, 0.0);
                            }
                            fieldLengthSum.put(k, fieldLengthSum.get(k) + field.getDocLength());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                docCollection.put(node.getId(), tempArrayDocument);
                docFieldNames.put(node.getId(), tempArrayField);
            }

            idf(docCollection);
            Map<String, ArrayList<Document>> fieldNameCollection = new HashMap<>();
            ArrayList<String> fieldNames = new ArrayList<>();
            docCollection.forEach((k, v) -> {

                String fieldName = "";
                for (int i = 0; i < docFieldNames.get(k).size(); i++) {

                    fieldName = docFieldNames.get(k).get(i);
                    fieldNames.add(fieldName);
                    if (!fieldNameCollection.containsKey(fieldName)) {
                        fieldNameCollection.put(fieldName, new ArrayList<>());
                    }
                    fieldNameCollection.get(fieldName).add(v.get(i));
                }
            });
            CorpusFielded fieldedCorpus = new CorpusFielded(fieldNameCollection, fieldNames);
//            Corpus corpus = new Corpus()

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
                    writeFieldIndexNodeTest(n, tx, docCollection, "Local");
                });
            }
            fieldLengthSum.forEach((k, v) -> meanFieldLengths.put(k, v / fieldNameCollection.get(k).size()));

            Map<String, Object> fieldParams = new HashMap<>();
            fieldParams.put("fieldName", fieldNameCollection.keySet().toArray());
            fieldParams.put("k1", 1.2);

            tx.execute("MERGE (n:Corpus) ON CREATE SET n.fieldName= $fieldName ON MATCH SET n.fieldName= $fieldName", fieldParams);
            tx.execute("MERGE (n:ParametersFielded) ON CREATE SET n.k1= $k1 ON MATCH SET n.k1=$k1", fieldParams);

            for (int i = 0; i < fieldedCorpus.getFieldSize(); i++) {

                Map<String, Object> params = new HashMap<>();
                String fieldName = fieldedCorpus.getFieldName(i);
                params.put("fieldedCorpus", fieldedCorpus.getBoWByIndex(i).toArray());
                params.put("meanLength", meanFieldLengths.get(fieldName));
                params.put("b", 0.75);

                tx.execute("MERGE (n:DataStats) ON CREATE SET n." + fieldName + "=$meanLength ON MATCH SET n." + fieldName + "=$meanLength", params);
                tx.execute("MERGE (n:ParametersFielded) ON CREATE SET n." + fieldName + "_b=$b ON MATCH SET n." + fieldName + "_b=$b", params);
            }


            tx.commit();
        }catch(Exception e){
            return Stream.of(SingleResult.fail());
        }
        return Stream.of(SingleResult.success());
    }


    public static void idf(Map<Long, ArrayList<Document>> docs) {
        Map<String, Double> checkedStems = new HashMap<>();
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
                        if (checkedStems.containsKey(keyword.getStem())) {
                            wordCount.getAndSet(checkedStems.get(keyword.getStem()));
                        }
                        else {
                            docs.forEach((k2, d2) -> {
                                for (Document fieldToCompare : d2) {
                                    Map<String, Integer> tempMap = fieldToCompare.getWordCountMap();
                                    if (field.getFieldName().equals(fieldToCompare.getFieldName()) && tempMap.containsKey(keyword.getStem())) {
                                        wordCount.getAndSet(wordCount.get() + 1);
                                    }
                                }

                            });
                            checkedStems.put(keyword.getStem(), wordCount.get());
                        }

                        double idf = Math.log(size.get() / wordCount.get()) / Math.log(2); // divide on Math.log(2) to get base 2 logarithm
                        keyword.setIdf(idf);
                    }
                }

            }
        });
    }
}
