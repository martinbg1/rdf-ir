package util;

import model.Document;
import model.NodeFields;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.TFIDF_variations;

public class IndexWriter {

    /**
     * Write indexNode to database
     *
     * @param node - Node to index
     * @param tx - transaction to write
     * @param docCollection - Collection of all documents. Used to retrieve node document
     */
    public static void writeIndexNode(Node node, Transaction tx, Map<Long, Document> docCollection, int maxFrequency) {
        Document doc = docCollection.get(node.getId());


        List<String> terms= new ArrayList<>();
        doc.keywords.forEach((k) ->terms.add(k.getStem()));

        List<Double> idf = new ArrayList<>();
        doc.keywords.forEach((k) ->idf.add(k.getIdf()));

        List<Double> tf = new ArrayList<>();
        doc.keywords.forEach((k) ->tf.add((double)k.getFrequency()));
        // log normalization tf
//                    field.keywords.forEach((k) ->tf.add(TFIDF_variations.TF_log_normalization(k.getFrequency())));
        // double normalization 0.5
//        doc.keywords.forEach((k) ->tf.add(TFIDF_variations.TF_double_normalization_05(k.getFrequency(), maxFrequency)));
        // double normalization K
        // TODO: finne ut hva K kan være / pleier å være - legge til en bra måte å regulere denne
//        doc.keywords.forEach((k) ->tf.add(TFIDF_variations.TF_double_normalization_K(k.getFrequency(), maxFrequency, 1)));


        HashMap<String, Object> params = new HashMap();
        params.put("documentLength", doc.getDocLength());
        params.put("terms", terms.toArray());
        params.put("idf", idf.toArray());
        params.put("tf", tf.toArray());
        params.put("ref", node.getId());

        tx.execute("CREATE (n:indexNode {ref: $ref, dl:$documentLength, terms: $terms, idf: $idf, tf: $tf})", params);
    }


    public static void writeFieldIndexNodeTest(Node node, Transaction tx, Map<Long, ArrayList<Document>> fieldNameCollection, String prefix, int maxFrequency) {
        fieldNameCollection.forEach((ref, doc) -> {
            if (ref.equals(node.getId())) {
                HashMap<String, Object> params = new HashMap();
                params.put("ref", node.getId());

                for (Document field : doc) {
                    String fieldName = field.getFieldName();

                    List<String> terms= new ArrayList<>();
                    field.keywords.forEach((k) ->terms.add(k.getStem()));

                    List<Double> idf = new ArrayList<>();
                    field.keywords.forEach((k) ->idf.add(k.getIdf()));

                    // Choose which TF to use
                    List<Double> tf = new ArrayList<>();
                    // raw frequency
                    field.keywords.forEach((k) ->tf.add((double)k.getFrequency()));
                    // log normalization tf
//                    field.keywords.forEach((k) ->tf.add(TFIDF_variations.TF_log_normalization(k.getFrequency())));
                    // double normalization 0.5
//                    field.keywords.forEach((k) ->tf.add(TFIDF_variations.TF_double_normalization_05(k.getFrequency(), maxFrequency)));
                    // double normalization K
                    // TODO: finne ut hva K kan være / pleier å være - legge til en bra måte å regulere denne
//                    field.keywords.forEach((k) ->tf.add(TFIDF_variations.TF_double_normalization_K(k.getFrequency(), maxFrequency, 1)));



                    HashMap<String, Object> paramsField = new HashMap();
                    paramsField.put("fieldLength", field.getDocLength());
                    paramsField.put("terms", terms.toArray());
                    paramsField.put("idf", idf.toArray());
                    paramsField.put("tf", tf.toArray());
                    paramsField.put("ref", ref);


                    tx.execute("MERGE (n:fieldNewIndexNode {ref: $ref})" +
                            " ON CREATE SET " +
                            "n." + fieldName + "Terms=$terms,"+
                            "n." + fieldName + prefix +"IDF=$idf,"+
                            "n." + fieldName + "TF=$tf," +
                            "n." + fieldName + "Length=$fieldLength" +
                            " ON MATCH SET " +
                            "n." + fieldName + "Terms=$terms,"+
                            "n." + fieldName + prefix +"IDF=$idf,"+
                            "n." + fieldName + "TF=$tf," +
                            "n." + fieldName + "Length=$fieldLength", paramsField);
                }
            }

        });
    }

    public static void writeFieldIndexNode(Node node, Transaction tx, Map<Long, NodeFields> fieldNameCollection, String prefix, int maxFrequency) {
        fieldNameCollection.forEach((ref, doc) -> {
            if (ref.equals(node.getId())) {
                HashMap<String, Object> params = new HashMap();
                params.put("ref", node.getId());

                for (Document field : doc.getFields()) {
                    String fieldName = field.getFieldName();

                    List<String> terms= new ArrayList<>();
                    field.keywords.forEach((k) ->terms.add(k.getStem()));

                    List<Double> idf = new ArrayList<>();
                    field.keywords.forEach((k) ->idf.add(k.getIdf()));

                    List<Double> tf = new ArrayList<>();
                    field.keywords.forEach((k) ->tf.add((double)k.getFrequency()));
                    // log normalization tf
//                    field.keywords.forEach((k) ->tf.add(TFIDF_variations.TF_log_normalization(k.getFrequency())));
                    // double normalization 0.5
//                    field.keywords.forEach((k) ->tf.add(TFIDF_variations.TF_double_normalization_05(k.getFrequency(), maxFrequency)));
                    // double normalization K
                    // TODO: finne ut hva K kan være / pleier å være - legge til en bra måte å regulere denne
//                    field.keywords.forEach((k) ->tf.add(TFIDF_variations.TF_double_normalization_K(k.getFrequency(), maxFrequency, 1)));

                    HashMap<String, Object> paramsField = new HashMap();
                    paramsField.put("fieldLength", field.getDocLength());
                    paramsField.put("terms", terms.toArray());
                    paramsField.put("idf", idf.toArray());
                    paramsField.put("tf", tf.toArray());
                    paramsField.put("ref", ref);


                    tx.execute("MERGE (n:fieldIndexNode {ref: $ref})" +
                            " ON CREATE SET " +
                            "n." + fieldName + "Terms=$terms,"+
                            "n." + fieldName + prefix +"IDF=$idf,"+
                            "n." + fieldName + "TF=$tf," +
                            "n." + fieldName + "Length=$fieldLength" +
                            " ON MATCH SET " +
                            "n." + fieldName + "Terms=$terms,"+
                            "n." + fieldName + prefix +"IDF=$idf,"+
                            "n." + fieldName + "TF=$tf," +
                            "n." + fieldName + "Length=$fieldLength", paramsField);
                }
            }

        });
    }
}
