package improvedSearch;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;
import result.SingleResult;

import java.util.stream.Stream;

import static dbUtil.indexDeleter.deleteRDFIndexes;

public class DeleteIndexes {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "improvedSearch.deleteIndexes", mode = Mode.WRITE)
    @Description("improvedSearch.deleteIndexes() - Delete all improvedSearch indexes")
    public Stream<SingleResult> deleteIndexes() {
        try(Transaction tx = db.beginTx()) {
            deleteRDFIndexes(tx);
        } catch (Exception e) {
            return Stream.of(SingleResult.fail());
        }
        return Stream.of(SingleResult.success());
    }
}
