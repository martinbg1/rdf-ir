package keywords;

import java.util.ArrayList;

public class NodeFields {

    private final ArrayList<Document> fields;
    private final ArrayList<String> fieldNames;


    public NodeFields(ArrayList<Document> fields, ArrayList<String> fieldNames, CorpusFielded corpus) {
        this.fields = fields;
        this.fieldNames = fieldNames;
        corpus.updateWordCount(this.fields);
    }

    public ArrayList<Document> getFields() {
        return fields;
    }

    public ArrayList<String> getFieldNames() {
        return fieldNames;
    }

    public String getFieldName(int i) {
        return this.fieldNames.get(i);
    }
}
