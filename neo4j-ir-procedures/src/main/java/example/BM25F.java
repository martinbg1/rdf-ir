package example;

import keywords.CardKeyword;
import keywords.Document;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import result.ResultNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static result.ResultUtil.sortResult;

public class BM25F {
    @Context
    public GraphDatabaseService db;

    // WIP copied from BM25
    @Procedure
    @Description("example.bm25fSearch(query) - returns bm25f query result")
    public Stream<ResultNode> bm25fSearch(@Name("fetch") String query) throws IOException {
        Map<Long, Double> result = new LinkedHashMap<>();
        // Query document
        Document qDoc = new Document(query);

        try(Transaction tx = db.beginTx()) {
            // get all indexNodes for fields with (terms, idf, tf og fl)
            ResourceIterator<Object> res = tx.execute("MATCH (n:indexNode) return n").columnAs("n");


            // retrieve mean field length
            double meanFieldLength = tx.execute("MATCH (n:DataStats) return n."+ FieldName +"Length").columnAs("n.meanFieldLength").next();

            // fill result with a node and its corresponding bm25f score
            // TODO sende inn ArrayList<Documents> med fields til bm25fScore
//            res.forEachRemaining(n -> result.put((Long)((Node) n).getProperty("name"),bm25fScore(,qDoc)));
        }

        Map<Node, Double> nodeMap = sortResult(result, db, 10);
        return nodeMap.entrySet().stream().map(ResultNode::new);
    }

    public double tfField(CardKeyword term, Document Field){
        double b = 0.75;
        double fieldLength = Field.keywords.size();
        // TODO lage avglength basert på fields, ikke hardcoded
        double avgl = 12;
        int occurence = term.getFrequency();

        return occurence/(1+b*((fieldLength/avgl)-1));
    }

    public double tf(CardKeyword kw, List<Document> fields, double boost){
        double sum = 0.0;

        for (Document field : fields) {
            for (CardKeyword k1 : field.keywords) {
                if (k1.equals(kw)) {
                    sum += boost * tfField(kw, field);
                }
            }
        }
//        for (int i = 0; i < fields.size(); i++) {
//            for(CardKeyword k1 : fields.get(i).keywords){
//                if(k1.equals(kw)){
//                    sum += boost*tfField(kw, fields.get(i));
//                }
//            }
//        }

        return sum;
    }

    public double bm25fScore(ArrayList<Document> fields, Document query){
        double k1 = 1.2;
        double sum = 0.0;

        for(CardKeyword qkw : query.keywords){
            double idf = qkw.getIdf();
            double tf = tf(qkw, fields, 1);
            sum += idf*(tf/(tf+k1));
        }

        return sum;
    }

}
