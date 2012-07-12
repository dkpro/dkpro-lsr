package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import static org.junit.Assert.fail;
import net.didion.jwnl.data.POS;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.WordNetResource;

public class WordNetUtilsTest
{

    private static LexicalSemanticResource wordnet;

    @BeforeClass
    public static void initializeWordNet()
    {
        try {
            wordnet = new WordNetResource(
                    "src/main/resources/resource/WordNet_3/wordnet_properties.xml");
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void wordNetUtilsTest() throws LexicalSemanticResourceException {
        
        System.out.println(
                WordNetUtils.getExactEntity(((WordNetResource) wordnet).getDict(), POS.VERB, 1025455)
        );
    }
}
