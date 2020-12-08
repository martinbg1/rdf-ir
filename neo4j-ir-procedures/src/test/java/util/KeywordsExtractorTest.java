package util;

import model.Document;
import model.CardKeyword;
import model.corpus.CorpusRDF;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeywordsExtractorTest {


    public static final String text = "Cake is a form of sweet food made from flour, sugar, and other ingredients, that is usually baked. " +
            "In their oldest forms, cakes were modifications of bread, but cakes now cover a wide range of preparations that can be simple or elaborate, " +
            "and that share features with other desserts such as pastries, meringues, custards, and pies.\n" +
            "\n" +
            "The most commonly used cake ingredients include flour, sugar, eggs, butter or oil or margarine, a liquid, and leavening agents, " +
            "such as baking soda or baking powder. Common additional ingredients and flavourings include dried, candied, or fresh fruit, nuts, cocoa, " +
            "and extracts such as vanilla, with numerous substitutions for the primary ingredients." +
            " Cakes can also be filled with fruit preserves, nuts or dessert sauces (like pastry cream), iced with buttercream or other icings," +
            " and decorated with marzipan, piped borders, or candied fruit.";


    @Test
    void shouldExtractKeywords() throws IOException {
        String text = "Cake is a form of sweet food made from flour, sugar, and other ingredients, that is usually baked. " +
                "In their oldest forms, cakes were modifications of bread, but cakes now cover a wide range of preparations that can be simple or elaborate, " +
                "and that share features with other desserts such as pastries, meringues, custards, and pies.\n" +
                "\n" +
                "The most commonly used cake ingredients include flour, sugar, eggs, butter or oil or margarine, a liquid, and leavening agents, " +
                "such as baking soda or baking powder. Common additional ingredients and flavourings include dried, candied, or fresh fruit, nuts, cocoa, " +
                "and extracts such as vanilla, with numerous substitutions for the primary ingredients." +
                " Cakes can also be filled with fruit preserves, nuts or dessert sauces (like pastry cream), iced with buttercream or other icings," +
                " and decorated with marzipan, piped borders, or candied fruit.";
        List<CardKeyword> keywordsList = KeywordsExtractor.getKeywordsList(text);

        keywordsList.forEach(keyword -> {
            System.out.println(keyword.getStem() + ": " + keyword.getTerms());
            System.out.println("Frequency: " + keyword.getFrequency() + "\n");
        }); 
    }


    @Test
    void shouldCalculateIdf() throws IOException {
        CorpusRDF corpus = new CorpusRDF();
        String text2 = "yes i love baking very much. Cake is my favourite. I love the sugar in the cakes.";
        Map<Long, Document> documents = new HashMap<>();
        Document doc1 = new Document(text, corpus);
        Document doc2 = new Document(text2, corpus);
        documents.put((long) 123, doc1);
        documents.put((long) 321, doc2);
        corpus.calculateIDF(documents);
        doc1.keywords.forEach(k -> System.out.println(k.getStem() + "\t" + k.getIdf()));
    }
}
