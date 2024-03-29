package improvedSearch;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;
import result.SingleResult;

import java.util.HashMap;
import java.util.stream.Stream;


public class SetParameters {

    @Context
    public GraphDatabaseService db;


    @Procedure(value ="improvedSearch.setParameter", mode = Mode.WRITE)
    @Description("improvedSearch.setParameter(<k1-value>, <b-value>) - used to set the free parameters of BM25")
    public Stream<SingleResult> setParameter(@Name("k1") double k1, @Name("b") double b) {
        try(Transaction tx = db.beginTx()){
            HashMap<String, Object> params = new HashMap<>();
            params.put("k1", k1);
            params.put("b", b);

            tx.execute("MERGE (n:Parameters) ON CREATE SET n.b=$b , n.k1=$k1 ON MATCH SET n.b=$b , n.k1=$k1 ", params);
            tx.commit();
        }catch(Exception e){
            return Stream.of(SingleResult.fail());
        }
        return Stream.of(SingleResult.success());
    }

    @Procedure(value ="improvedSearch.setFieldParameter", mode = Mode.WRITE)
    @Description("improvedSearch.setFieldParameter(<k1-value>, <fieldName>, <b-value>, <boost-value>) - used to set the free parameters for BM25F and BM25FF")
    public Stream<SingleResult> setFieldParameter(@Name("k1") double k1, @Name("fieldNames") String fieldName, @Name("b") double b, @Name("boost") double boost) {
        try(Transaction tx = db.beginTx()){
            HashMap<String, Object> params = new HashMap<>();
            params.put("k1", k1);
            params.put("b", b);
            params.put("boost", boost);

            String[] corpusFieldNames = (String[]) tx.execute("MATCH (n:Corpus) return n.fieldName").columnAs("n.fieldName").next();

            for(String fieldNameToCompare : corpusFieldNames){
                if(fieldNameToCompare.equals(fieldName)){
                    tx.execute("MERGE (n:ParametersFielded) ON CREATE SET n."+ fieldName+"_b=$b , n."+ fieldName+"_boost=$boost , n.k1=$k1 ON MATCH SET n."+ fieldName +"_b=$b , n."+ fieldName+"_boost=$boost , n.k1=$k1 ", params);

                    tx.commit();
                    return Stream.of((SingleResult.success()));
                }
            }
        }catch(Exception e){
            return Stream.of(SingleResult.fail());
        }
        return Stream.of(SingleResult.success());
    }

}
