package example;

import keywords.Document;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class VectorModel {
    @Context
    public GraphDatabaseService db;


    @Procedure
    @Description("example.vectorModelSearch(query) - returns TF-IDF query result")
    public Stream<ResultNode> vectorModelSearch(@Name("fetch") String query) throws IOException {
        Map<Long, Double> result = new LinkedHashMap<>();
        List<Map.Entry<Node, Double>> entries = new ArrayList<>();
        Document queryDoc = new Document(query);

        try (Transaction tx = db.beginTx()) {
            // retrieve all index nodes
            ResourceIterator<Object> res = tx.execute("MATCH (n:indexNode) return n").columnAs("n");

            ResourceIterator<Node> corpusNode = tx.execute("MATCH (n:Corpus) return n").columnAs("n");
            String[] corpus = (String[]) corpusNode.next().getProperty("corpus");


            ResourceIterator<Node> idfNode = tx.execute("MATCH (n:IDF) return n").columnAs("n");
            double[] idf = (double[]) idfNode.next().getProperty("idf");

            setQueryIdf(queryDoc, corpus, idf);
            setQueryVector(queryDoc, corpus);


//            res.forEachRemaining(n -> result.put((Node) n, 4.0));
            while (res.hasNext()) {
                Node tempNode = (Node) res.next();
                String[] indexTerms = (String[]) ((Node) tempNode).getProperty("terms");
                int[] indexTF = (int[]) ((Node) tempNode).getProperty("tf");
                double[] indexIDF = (double[]) ((Node) tempNode).getProperty("idf");
                Long nodeID = (Long) ((Node) tempNode).getProperty("name");
                double[] documentVector = setDocumentVector(queryDoc, indexTerms, indexTF, indexIDF);

                result.put(nodeID, cosineSimilarity(queryDoc.getVector(), documentVector));
            }
        }

        // Convert to ArrayList and sort by value
        List<Map.Entry<Long, Double>> sortedResult = new ArrayList<>(result.entrySet());
        sortedResult.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // Return top 5 results if possible
        if (sortedResult.size() < 5) {
            return sortedResult.stream().map(ResultNode::new);
        }
        return sortedResult.subList(sortedResult.size() - 5, sortedResult.size()).stream().map(ResultNode::new);
    }


    public static void setQueryIdf(Document query, String[] corpus, double[] idf) {
        // idf[n] corresponds to term corpus[n]
        for (int i = 0; i < corpus.length; i++) {
            for (int j = 0; j < query.keywords.size(); j++) {
                if (corpus[i].equals(query.keywords.get(j).getStem())) {
                    query.keywords.get(j).setIdf(idf[i]);
                }
            }
        }
    }

    public static void setQueryVector(Document query, String[] corpus) {
        query.initializeVector(query.keywords.size());

        for (int i = 0; i < query.keywords.size(); i++) {
            double vectorValue = 0.0;
            for (String s : corpus) {
                if (s.equals(query.keywords.get(i).getStem())) {
                    vectorValue = query.keywords.get(i).getTfIdf();
                }
            }
            query.setVectorValue(vectorValue, i);
        }
    }

    public static double[] setDocumentVector(Document query, String[] terms, int[] tf, double[] idf) {
        double[] documentVector = new double[query.keywords.size()];

        for (int i = 0; i < query.keywords.size(); i++) {
            double vectorValue = 0.0;
            for (int j = 0; j < terms.length; j++) {
                if (terms[j].equals(query.keywords.get(i).getStem())) {
                    vectorValue = tf[j] * idf[j];
                }
                documentVector[i] = vectorValue;
            }
        }
        System.out.println(Arrays.toString(documentVector));
        return documentVector;
    }


    public static class ResultNode {
        public Long node;
        public Double score;

        public ResultNode(Map.Entry<Long, Double> entity) {
            this.node = entity.getKey();
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
