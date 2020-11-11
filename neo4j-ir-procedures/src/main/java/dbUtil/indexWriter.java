package dbUtil;

import keywords.CardKeyword;
import keywords.Document;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class indexWriter {

    /**
     *
     * Write indexNode to database
     *
     * @param node - Node to index
     * @param tx - transaction to write
     * @param docCollection - Collection of all documents. Used to retrieve node document
     */
    public static void writeIndexNode(Node node, Transaction tx, Map<Long, Document> docCollection) {
        Document doc = docCollection.get(node.getId());


        List<String> terms= new ArrayList<>();
        doc.keywords.forEach((k) ->terms.add(k.getStem()));

        List<Double> idf = new ArrayList<>();
        doc.keywords.forEach((k) ->idf.add(k.getIdf()));

        List<Integer> tf = new ArrayList<>();
        doc.keywords.forEach((k) ->tf.add(k.getFrequency()));


        HashMap<String, Object> params = new HashMap();
        params.put("documentLength", doc.keywords.size());
        params.put("terms", terms.toArray());
        params.put("idf", idf.toArray());
        params.put("tf", tf.toArray());
        params.put("name", node.getId());

        tx.execute("CREATE (n:indexNode {name: $name, dl:$documentLength, terms: $terms, idf: $idf, tf: $tf})", params);
    }


    public static void writeFieldIndexNode(Node node, Transaction tx, Map<Long, ArrayList<Document>> fieldNameCollection) {
        fieldNameCollection.forEach((ref, doc) -> {
            if (ref.equals(node.getId())) {
                HashMap<String, Object> params = new HashMap();
                params.put("ref", node.getId());
                tx.execute("CREATE (n:indexNode {ref: $ref})", params);

                for (Document field : doc) {
                    String fieldName = field.getFieldName();

                    List<String> terms= new ArrayList<>();
                    field.keywords.forEach((k) ->terms.add(k.getStem()));

                    List<Double> idf = new ArrayList<>();
                    field.keywords.forEach((k) ->idf.add(k.getIdf()));

                    List<Integer> tf = new ArrayList<>();
                    field.keywords.forEach((k) ->tf.add(k.getFrequency()));

                    HashMap<String, Object> paramsField = new HashMap();
                    paramsField.put("fieldLength", field.keywords.size());
                    paramsField.put("terms", terms.toArray());
                    paramsField.put("idf", idf.toArray());
                    paramsField.put("tf", tf.toArray());
                    paramsField.put("ref", ref);

                    tx.execute("MATCH (n:indexNode)" +
                            "WHERE n.ref=$ref SET " +
                            "n." + fieldName + "Terms=$terms," +
                            "n." + fieldName + "IDF=$idf," +
                            "n." + fieldName + "TF=$tf," +
                            "n." + fieldName + "Length=$fieldLength", paramsField);
                }
            }

        });
    }
}
