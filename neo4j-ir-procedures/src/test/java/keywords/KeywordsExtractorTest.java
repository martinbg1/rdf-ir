package keywords;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KeywordsExtractorTest {

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
        String alias = "Wuhan pneumonia, 2019 novel coronavirus respiratory syndrome, 2019-nCoV acute respiratory disease, coronavirus disease 2019, Coronavirus disease 2019, SARS-CoV-2 infection," +
                " seafood market pneumonia, severe acute respiratory syndrome type 2, Covid-19, 2019 NCP, 2019 novel coronavirus pneumonia, COVID 19, COVID-2019, COVID19, nCOVD-19, nCOVD19, Wuhan respiratory syndrome, " +
                "WuRS, CD-19, nCOVD 19";
        List<CardKeyword> keywordsList = KeywordsExtractor.getKeywordsList(alias);

        keywordsList.forEach(keyword -> {
            System.out.println(keyword.getStem() + ": " + keyword.getTerms());
            System.out.println("Frequency: " + keyword.getFrequency() + "\n");
        }); 
    }
}
