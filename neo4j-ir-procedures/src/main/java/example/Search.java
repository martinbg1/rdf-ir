package example;

import keywords.Document;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Search {
    @Context
    public GraphDatabaseService db;


    @Procedure
    @Description("example.tfidfSearch(query) - returns TF-IDF query result")
    public Stream<ResultNode> TfIdfSearch(@Name("fetch") String query) throws IOException {
        Map<Node, Double> result = new LinkedHashMap<>();

        try(Transaction tx = db.beginTx()){
            ResourceIterator<Object> res = tx.execute("MATCH (n) WHERE NOT n:Vectors AND NOT n:Corpus AND not n:IDF return n").columnAs("n");
            Document qDoc = new Document(query);

            ResourceIterator<Node> vectorNode = tx.execute("MATCH (n:Vectors) return n").columnAs("n");
            Map<Long, double[]> vectors = new HashedMap();
            vectorNode.next().getAllProperties().forEach((k, v) -> vectors.put(Long.parseLong(k.substring(1)), (double[]) v));

            ResourceIterator<Node> corpusNode = tx.execute("MATCH (n:Corpus) return n").columnAs("n");
            String[] corpus = (String[]) corpusNode.next().getProperty("corpus");


            ResourceIterator<Node> idfNode = tx.execute("MATCH (n:IDF) return n").columnAs("n");
            double[] idf = (double[]) idfNode.next().getProperty("idf");

            setQueryIdf(qDoc, corpus, idf);
            setQueryVector(qDoc, corpus);


            res.forEachRemaining(n -> result.put((Node) n, cosineSimilarity(qDoc.getVector(), vectors.get(((Node) n).getId()))));
        }
        return result.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(ResultNode::new);

    }


    public static void setQueryIdf(Document query, String[] corpus, double[] idf) {

        // idf[n] corresponds to term corpus[n]
        for (int i = 0; i < corpus.length; i++) {
            for (int j = 0; j < query.keywords.size(); j++) {
//                System.out.println("corpus: " + corpus[i] + "\t" + "query: " + (query.keywords.get(j).getStem()));
                if (corpus[i].equals(query.keywords.get(j).getStem())) {
                    query.keywords.get(j).setIdf(idf[i]);
                }
            }
        }
    }

    // TODO gjør Document.setVector mer generell slik at den kan bli brukt her
    public static void setQueryVector(Document query, String[] corpus) {
        query.initializeVector(corpus.length);
        for (int i = 0; i < corpus.length; i++) {
            double vectorValue = 0.0;
            for (int j = 0; j < query.keywords.size(); j++) {
                if (corpus[i].equals(query.keywords.get(j).getStem())) {
                    vectorValue = query.keywords.get(j).getTfIdf();
                }
            }
            query.setQueryValue(vectorValue, i);
        }
    }


    public static class ResultNode {
        public String node;
        public Double score;

        public ResultNode(Map.Entry<Node, Double> entity) {
            this.node = entity.getKey().toString();
            this.score = entity.getValue();
        }
    }

    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
