package improvedSearch;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;
import result.SingleResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class SetParameters {

    @Context
    public GraphDatabaseService db;


    @Procedure(value ="improvedSearch.setParameter", mode = Mode.WRITE)
    @Description("improvedSearch.setParameter()")
    public Stream<SingleResult> setParameter(@Name("k1") double k1, @Name("b") double b) {
        try(Transaction tx = db.beginTx()){
            HashMap<String, Object> params = new HashMap<>();
            params.put("k1", k1);
            params.put("b", b);


            tx.execute("MERGE (n:Parameters) ON CREATE SET n.b=$b , n.k1=$k1 ON MATCH SET n.b=$b , n.k1=$k1 ", params);
            tx.commit();

        }
        return Stream.of(SingleResult.success());
    }

    @Procedure(value ="improvedSearch.setFieldParameter", mode = Mode.WRITE)
    @Description("improvedSearch.setFieldParameter()")
    public Stream<SingleResult> setFieldParameter(@Name("k1") double k1, @Name("fieldNames") String fieldName, @Name("b") double b) {
        try(Transaction tx = db.beginTx()){
            HashMap<String, Object> params = new HashMap<>();
            params.put("k1", k1);
            params.put("b", b);

            String[] corpusFieldNames = (String[]) tx.execute("MATCH (n:Corpus) return n.fieldName").columnAs("n.fieldName").next();

            for(String fieldNameToCompare : corpusFieldNames){
                if(fieldNameToCompare.equals(fieldName)){
                    tx.execute("MERGE (n:ParametersFielded) ON CREATE SET n."+ fieldName+"_b=$b , n.k1=$k1 ON MATCH SET n."+ fieldName +"_b=$b , n.k1=$k1 ", params);

                    tx.commit();
                    return Stream.of((SingleResult.success()));
                }
            }


        }
        return Stream.of(SingleResult.fail());
    }



}
