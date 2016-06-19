/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;

/**
 * Some more WordNet tests including the computation of some statistics.
 *
 * @author zesch
 *
 */
public class WordNetResourceTestStatistics {

    private static LexicalSemanticResource wordnet;

	@BeforeClass
	public static void initializeWordNet(){
		try{
			wordnet = new WordNetResource("src/main/resources/resource/WordNet_3/wordnet_properties.xml");
//            wordnet = ResourceFactory.getInstance().get("wordnet", "en");
		} catch(Exception e){
            fail(e.getMessage());
		}
	}

    @Test
    public void mweTest() throws Exception {
        wordnet.setIsCaseSensitive(true);

        Set<String> nouns      = new TreeSet<String>();
        Set<String> verbs      = new TreeSet<String>();
        Set<String> adjectives = new TreeSet<String>();
        Set<String> adverbs    = new TreeSet<String>();
        Set<String> other      = new TreeSet<String>();

        int i = 0;
        int mweCount = 0;

        for (Entity entity : wordnet.getEntities()) {
            Set<String> lexemes = entity.getLexemes();
            for (String lexeme : lexemes) {
                i++;
                lexeme = lexeme.trim();
                if (lexeme.contains("_")) {
                    if (entity.getPos().equals(PoS.n)) {
                        nouns.add(lexeme);
                    }
                    else if (entity.getPos().equals(PoS.v)) {
                        verbs.add(lexeme);
                    }
                    else if (entity.getPos().equals(PoS.adj)) {
                        adjectives.add(lexeme);
                    }
                    else if (entity.getPos().equals(PoS.adv)) {
                        adverbs.add(lexeme);
                    }
                    else {
                        other.add(lexeme);
                    }
                    mweCount++;
                }
            }
        }

        assertEquals(206978, i);
        assertEquals(68082, mweCount);

        assertEquals(60344, nouns.size());
        assertEquals(2829,  verbs.size());
        assertEquals(496,   adjectives.size());
        assertEquals(714,   adverbs.size());
        assertEquals(0,     other.size());

        System.out.println("# lexemes: " + i);
        System.out.println("# MWEs: " + mweCount);
        System.out.println("ratio: " + (double) mweCount/i);
        System.out.println("");
        System.out.println("# MWE nouns: " + nouns.size());
        System.out.println("# MWE verbs: " + verbs.size());
        System.out.println("# MWE adjectives: " + adjectives.size());
        System.out.println("# MWE adverbs: " + adverbs.size());
        System.out.println("# MWE other: " + other.size());

        System.out.println("NOUNS");
        for (String item : nouns) {
            System.out.println(item);
        }
        System.out.println();
        System.out.println("VERBS");
        for (String item : verbs) {
            System.out.println(item);
        }
        System.out.println();
        System.out.println("ADJECTIVES");
        for (String item : adjectives) {
            System.out.println(item);
        }
        System.out.println();
        System.out.println("ADVERBS");
        for (String item : adverbs) {
            System.out.println(item);
        }
        System.out.println();
        System.out.println("OTHER");
        for (String item : other) {
            System.out.println(item);
        }
        System.out.println();
    }
}
