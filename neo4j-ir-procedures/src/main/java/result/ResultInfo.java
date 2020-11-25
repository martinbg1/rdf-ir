package result;

import java.util.Map;

public class ResultInfo {

    public String node;
    public Double score;

    public ResultInfo(Map.Entry<String, Double> entity) {
        this.node = entity.getKey();
        this.score = entity.getValue();
    }
}
