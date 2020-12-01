package util;

import org.neo4j.graphdb.Transaction;

public class IndexDeleter {

    /**
     * Prepares db for RDF index
     * @param tx Neo4j Transaction to write to db
     */
    public static void prepareRDFIndex(Transaction tx) {
        tx.execute("MATCH (i:indexNode) DETACH DELETE i");
        tx.execute("MATCH (i:IDF) DETACH DELETE i");
        tx.commit();
    }

    /**
     * Prepares db for fielded RDF index
     * @param tx Neo4j Transaction to write to db
     */
    public static void prepareRDFFieldedIndex(Transaction tx) {
        tx.execute("MATCH (i:fieldIndexNode) DETACH DELETE i");
        tx.commit();
    }

    /**
     * Prepares db for fieldedNew RDF index
     * @param tx Neo4j Transaction to write to db
     */
    public static void prepareRDFFieldedNewIndex(Transaction tx) {
        tx.execute("MATCH (i:fieldNewIndexNode) DETACH DELETE i");
        tx.commit();
    }

    /**
     * Completely deletes all improvedSearch indexes
     * @param tx Neo4j Transaction to write to db
     */
    public static void deleteRDFIndexes(Transaction tx) {
        tx.execute("MATCH (i:indexNode) DETACH DELETE i");
        tx.execute("MATCH (i:fieldIndexNode) DETACH DELETE i");
        tx.execute("MATCH (i:fieldNewIndexNode) DETACH DELETE i");
        tx.execute("MATCH (i:IDF) DETACH DELETE i");
        tx.execute("MATCH (c:Corpus) DETACH DELETE c");
        tx.execute("MATCH (d:DataStats) DETACH DELETE d");
        tx.execute("MATCH (p:Parameters) DETACH DELETE p");
        tx.execute("MATCH (p:ParametersFielded) DETACH DELETE p");
        tx.commit();
    }
}
