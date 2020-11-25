package resultSorter;

public class SingleResult {

    private static final String SUCCESS_RESULT = "SUCCESS";
    private static final String FAIL_RESULT = "FAIL";

    public final Object result;

    public SingleResult(Object result) {
        this.result = result;
    }

    public static SingleResult success() {
        return new SingleResult(SUCCESS_RESULT);
    }

    public static SingleResult fail() {
        return new SingleResult(FAIL_RESULT);
    }
}
