package dbUtil;

import keywords.Corpus;
import keywords.Document;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class indexWriter {

    public static void writeIndex(Node node, Transaction tx, Map<Long, Document> docCollection, Corpus corpus) {
        Document doc = docCollection.get(node.getId());


        List<String> terms= new ArrayList<>();
        doc.keywords.forEach((k) ->terms.add(k.getStem()));

        List<Double> idf = new ArrayList<>();
        doc.keywords.forEach((k) ->idf.add(k.getIdf()));

        List<Integer> tf = new ArrayList<>();
        doc.keywords.forEach((k) ->tf.add(k.getFrequency()));


        HashMap<String, Object> params = new HashMap();
//        params.put("vector", doc.getVector());
        params.put("documentLength", doc.keywords.size());
        params.put("terms", terms.toArray());
        params.put("idf", idf.toArray());
        params.put("tf", tf.toArray());
        params.put("name", node.getId());

        tx.execute("CREATE (n:indexNode {name: $name, vector: $vector, dl:$documentLength, terms: $terms, idf: $idf, tf: $tf})", params);
    }

}
