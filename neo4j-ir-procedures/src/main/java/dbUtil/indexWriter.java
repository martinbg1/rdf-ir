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


    public static void writeFieldIndexNode(Node node, Transaction tx, Map<Long, ArrayList<Document>> docCollection) {
        ArrayList<Document> doc = docCollection.get(node.getId());


        List<String[]> terms= new ArrayList<>();
        List<String> fieldTerms = new ArrayList<>();
//        String[][] strr = new String[doc.size()][];
//        for (int i = 0; i <doc.size() ; i++) {
//            strr[i] = doc.get(i).toStringArray();
//        }


        String[] str1 = new String[doc.get(0).keywords.size()];

//        for (CardKeyword kw : doc.get(0).keywords) {
//            str1[0] = kw.getStem();
//        }

//        Bare masse rot (:

//        for (int i = 0; i < doc.get(0).keywords.size(); i++) {
//            for(CardKeyword kw: doc.get(0).keywords){
//                str1[i] = i + " : " + kw.getStem();
//            }
//        }
//        for(String n : str1){
//            System.out.println(n);
//        }

//        for (int i = 0; i < doc.size(); i++) {
//            List<String> temp = new ArrayList<>();
//            for(CardKeyword kw :doc.get(i).keywords){
//                temp.add(kw.getStem());
//            }
//            str1[i] = temp.toString();
//        }
//        for(Document field : doc){
//            field.keywords.forEach((k) ->fieldTerms.add(k.getStem()));
//        }

//        String[] str = fieldTerms.toArray(new String[fieldTerms.size()]);
//        terms.add(strr);


//        List<Object[]> idf = new ArrayList<>();
//        List<Object> idfTerms = new ArrayList<>();
//        for(Document field : doc){
//            field.keywords.forEach((k) ->idfTerms.add(k.getIdf()));
//        }
//        idf.add(idfTerms.toArray());
//
//
//        List<Object[]> tf = new ArrayList<>();
//        List<Object> tfTerms = new ArrayList<>();
//        for(Document field : doc){
//            field.keywords.forEach((k) ->tfTerms.add((int)k.getFrequency()));
//        }
//        tf.add(tfTerms.toArray());


        HashMap<String, Object> params = new HashMap();
//        params.put("documentLength", doc.keywords.size());
        params.put("terms", str1);
//        params.put("idf", idf.toArray());
//        params.put("tf", tf.toArray());
        params.put("name", node.getId());
//        dl:$documentLength,
//        , idf: $idf, tf: $tf
        tx.execute("CREATE (n:indexNode {name: $name, terms: $terms})", params);
    }
}
