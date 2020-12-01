package util;

public class TFIDF_variations {

    /**
     * TF versions
     */

    public static double TF_log_normalization(int frequency){
        return 1+Math.log(frequency);
    }

    public static double TF_double_normalization_05(int frequency, int maxFrequency){
        return  0.5+0.5*((double)frequency/maxFrequency);
    }

    public static double TF_double_normalization_K(int frequency, int maxFrequency, int K){
        return K+(1-K)*((double)frequency/maxFrequency);
    }

}
