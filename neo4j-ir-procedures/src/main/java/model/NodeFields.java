package model;

import model.corpus.CorpusFielded;

import java.util.ArrayList;

public class NodeFields {

    /**
     * All the fields in the node
     */
    private final ArrayList<Document> fields;

    /**
     * Names of the fields in the node
     */
    private final ArrayList<String> fieldNames;


    /**
     * Constructs a NodeField
     * @param fields Document fields in the Node
     * @param fieldNames Field names in the node
     * @param corpus corpus the node belongs to
     */
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
