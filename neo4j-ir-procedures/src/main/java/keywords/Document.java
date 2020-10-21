package keywords;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Document {

    public List<CardKeyword> keywords;
    private HashMap<String, Integer> wordCountMap;

    public Document(String field) throws IOException {
        this.keywords = KeywordsExtractor.getKeywordsList(field);
        this.wordCountMap = new HashMap<>();

        for (CardKeyword keyword : this.keywords) {
            this.wordCountMap.put(keyword.getStem(), keyword.getFrequency());
        }
    }

    public HashMap<String, Integer> getWordCountMap() {
        return this.wordCountMap;
    }

}

