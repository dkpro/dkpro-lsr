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
package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import static org.junit.Assert.fail;
import net.sf.extjwnl.data.POS;

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
