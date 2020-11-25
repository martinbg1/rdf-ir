package improvedSearch;

import model.corpus.CorpusRDF;
import model.Document;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import org.neo4j.procedure.Name;

import model.corpus.CardKeyword;

import static util.indexWriter.writeIndexNode;


public class IndexRDF {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "improvedSearch.indexRDF", mode = Mode.WRITE)
    @Description("improvedSearch.indexRDF(query) - return the tf-idf score for nodes")
    public Stream<EntityField> indexRDF(@Name("fetch") String input) throws IOException {
        double documentLengthSum = 0.0;
        CorpusRDF corpus = new CorpusRDF();
        try(Transaction tx = db.beginTx()){
            Map<Long, Document> docCollection = new HashedMap();

            // Delete old index and initialize new
            // tx.execute("MATCH (i:indexNode), (c:Corpus), (idf:IDF) detach delete i, c, idf ");

            // Retrieve nodes to index
            Result res = tx.execute(input);
            Iterator<Node> d_column = res.columnAs("d");

            // TODO handle null values
            // process terms
            while (d_column.hasNext()) {
                ArrayList<String> temp = new ArrayList<>();
                Node node = d_column.next();

                node.getAllProperties().forEach((k, v) -> {
                    if (!k.equals("uri")) {
                        temp.add((String) v);
                    }
                });

                Document doc = new Document(temp.toString(), corpus);
                docCollection.put(node.getId(), doc);

                documentLengthSum += doc.getDocLength();
            }

            // Calculate idf and initialize corpus values
            corpus.calculateIDF(docCollection);
            corpus.initCorpusValues(docCollection);

            // finish result
            Map<CardKeyword, Double> result = new HashMap<>();
            docCollection.forEach((k, doc) -> doc.keywords.forEach(keyword -> result.put(keyword, keyword.getTfIdf())));

            // TODO: fikse dette på en bedre måte annet enn å laste inn på nytt.
            Result res1 = tx.execute(input);
            // start of write operation
            Iterator<Node> n_column = res1.columnAs("d");
            while(n_column.hasNext()){
                n_column.forEachRemaining(n -> {
                    writeIndexNode(n, tx, docCollection);
                });
            }
            double meanDocumentLength = documentLengthSum / docCollection.size();

            HashMap<String, Object> params = new HashMap<>();
            params.put("corpus", corpus.getBoW().toArray());
            params.put("idf", corpus.getIdf());
            params.put("k1", 1.2);
            params.put("b", 0.75);
            params.put("meanLength", meanDocumentLength);

            tx.execute("CREATE (n:IDF)");
            tx.execute("MERGE (n:Corpus) ON CREATE SET n.corpus=$corpus ON MATCH SET n.corpus=$corpus", params);
            tx.execute("MERGE (n:Parameters) ON CREATE SET n.b=$b , n.k1=$k1 ON MATCH SET n.b=$b , n.k1=$k1 ", params);
            tx.execute("MATCH (n:IDF) SET n.idf=$idf", params);
            tx.execute("MERGE (n:DataStats) ON CREATE SET n.meanDocumentLength= $meanLength ON MATCH SET n.meanDocumentLength= $meanLength", params);

            tx.commit();
            return result.entrySet().stream().map(EntityField::new);
        }

    }


    public static class EntityField {
        public String stem;
        public Double tfidf;

        public EntityField(Map.Entry<CardKeyword, Double> entity) {
            this.stem = entity.getKey().getStem();
            this.tfidf = entity.getValue();
        }
    }
}

