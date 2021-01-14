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

    /**
     * IDF versions
     */

    public static double IDF_standard(double wordCount, int numOfDocs){
        return Math.log((numOfDocs / wordCount)) / Math.log(2);
    }

    public static double IDF_smooth(double wordCount, int numOfDocs){
        return Math.log((1+(numOfDocs/wordCount)))/ Math.log(2);
    }

    public static double IDF_max(double wordCount, int maxFrequency){
        return Math.log((1+(maxFrequency/wordCount)))/ Math.log(2);
    }

    public static double IDF_probabilitic(double wordCount, int numOfDocs){
        return Math.log(((numOfDocs-wordCount)/wordCount))/ Math.log(2);
    }
}
