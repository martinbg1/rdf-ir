package improvedSearch;

import model.*;
import model.corpus.CorpusFielded;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import org.neo4j.procedure.Name;
import result.SingleResult;

import static util.IndexDeleter.prepareRDFFieldedIndex;
import static util.IndexWriter.writeFieldIndexNode;


public class IndexRDFFielded {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "improvedSearch.indexRDFFielded", mode = Mode.WRITE)
    @Description("improvedSearch.indexRDFFielded(query) - return information of if indexing was a success or failure")

    public Stream<SingleResult> indexRDFFielded(@Name("fetch") String input) throws IOException {
        // Prepare db to be indexed by deleting old indexNodes
        try(Transaction tx = db.beginTx()) {
            prepareRDFFieldedIndex(tx);
        }

        CorpusFielded corpus = new CorpusFielded();
        Map<String, Double> fieldLengthSum = new HashMap<>();
        Map<String, Double> meanFieldLengths = new HashMap<>();
        try(Transaction tx = db.beginTx()){
            // ArrayList<Document> accounts to a list of documents for each field.
            Map<Long, NodeFields> docCollection = new HashMap<>();

            // Retrieve nodes to index
            Result res = tx.execute(input);
            Iterator<Node> d_column_fielded = res.columnAs("d");

            // TODO handle null values
            // process terms
            while (d_column_fielded.hasNext()) {
                ArrayList<Document> tempArrayDocument = new ArrayList<>();
                ArrayList<String> tempArrayField = new ArrayList<>();
                Node node = d_column_fielded.next();
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
                NodeFields nodeFields = new NodeFields(tempArrayDocument, tempArrayField, corpus);
                docCollection.put(node.getId(),nodeFields);

            }

            Map<String, ArrayList<Document>> fieldNameCollection = new HashMap<>();
            docCollection.forEach((k, v) -> {

                String fieldName = "";
                for (int i = 0; i < docCollection.get(k).getFieldNames().size(); i++) {

                    fieldName = docCollection.get(k).getFieldName(i);
                    if (!fieldNameCollection.containsKey(fieldName)) {
                        fieldNameCollection.put(fieldName, new ArrayList<>());
                    }
                    fieldNameCollection.get(fieldName).add(v.getFields().get(i));
                }
            });

            // Calculate idfInitialize corpus values
            corpus.calculateIDF(docCollection);
            corpus.initCorpusValues(fieldNameCollection);


            // TODO: fikse dette på en bedre måte annet enn å laste inn på nytt.
            Result res1 = tx.execute(input);

            // start of write operation
            Iterator<Node> n_column = res1.columnAs("d");
            while(n_column.hasNext()){
                n_column.forEachRemaining(n -> {
                    writeFieldIndexNode(n, tx, docCollection, "Global");
                });
            }
            fieldLengthSum.forEach((k, v) -> meanFieldLengths.put(k, v / fieldNameCollection.get(k).size()));

            Map<String, Object> fieldParams = new HashMap<>();
            fieldParams.put("fieldName", fieldNameCollection.keySet().toArray());
            fieldParams.put("k1", 1.2);

            tx.execute("MERGE (n:Corpus) ON CREATE SET n.fieldName= $fieldName ON MATCH SET n.fieldName= $fieldName", fieldParams);
            tx.execute("MERGE (n:ParametersFielded) ON CREATE SET n.k1= $k1 ON MATCH SET n.k1=$k1", fieldParams);

            for (int i = 0; i < corpus.getFieldSize(); i++) {
                Map<String, Object> params = new HashMap<>();
                String fieldName = corpus.getFieldName(i);
                params.put("meanLength", meanFieldLengths.get(fieldName));
                params.put("b", 0.75);
                params.put("boost", 1.0);

                tx.execute("MERGE (n:DataStats) ON CREATE SET n." + fieldName + "=$meanLength ON MATCH SET n." + fieldName + "=$meanLength", params);
                tx.execute("MERGE (n:ParametersFielded) ON CREATE SET n." + fieldName + "_b=$b , n." + fieldName + "_boost=$boost ON MATCH SET n." + fieldName + "_b=$b , n." + fieldName + "_boost=$boost ", params);
            }
            tx.commit();
        }catch(Exception e){
            return Stream.of(SingleResult.fail());
        }
        return Stream.of(SingleResult.success());
    }
}
