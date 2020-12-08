package improvedSearch;

import result.ResultNode;
import model.Document;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static util.ResultUtil.sortResult;

public class VectorModel {
    @Context
    public GraphDatabaseService db;


    @Procedure
    @Description("improvedSearch.vectorModelSearch(query) - returns TF-IDF query result")
    public Stream<ResultNode> vectorModelSearch(@Name("fetch") String query) throws IOException {
        Map<Long, Double> result = new LinkedHashMap<>();
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

            while (res.hasNext()) {
                Node tempNode = (Node) res.next();
                String[] indexTerms = (String[]) tempNode.getProperty("terms");
                double[] indexTF = (double[]) tempNode.getProperty("tf");
                double[] indexIDF = (double[]) tempNode.getProperty("idf");
                Long nodeID = (Long) tempNode.getProperty("ref");
                double[] documentVector = setDocumentVector(queryDoc, indexTerms, indexTF, indexIDF);

                result.put(nodeID, cosineSimilarity(queryDoc.getVector(), documentVector));
            }
        }

        Map<Node, Double> nodeMap = sortResult(result, db, 10);
        return nodeMap.entrySet().stream().map(ResultNode::new);

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

    public static double[] setDocumentVector(Document query, String[] terms, double[] tf, double[] idf) {
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
        return documentVector;
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
        double denominator = (Math.sqrt(normA) * Math.sqrt(normB));

        if (denominator == 0.0) {
            return 0.0;
        }
        return dotProduct / denominator;
    }
}
