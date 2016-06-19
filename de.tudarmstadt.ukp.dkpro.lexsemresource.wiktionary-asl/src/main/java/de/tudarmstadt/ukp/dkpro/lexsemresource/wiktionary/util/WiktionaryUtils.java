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
package de.tudarmstadt.ukp.dkpro.lexsemresource.wiktionary.util;

import java.util.HashSet;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.wiktionary.api.PartOfSpeech;
import de.tudarmstadt.ukp.wiktionary.api.WikiString;
import de.tudarmstadt.ukp.wiktionary.api.Wiktionary;
import de.tudarmstadt.ukp.wiktionary.api.WordEntry;

public class WiktionaryUtils {

    public static PoS mapPos(PartOfSpeech pos) {
        if (pos.equals(PartOfSpeech.NOUN) || pos.equals(PartOfSpeech.PROPER_NOUN)) {
            return PoS.n;
        }
        else if (pos.equals(PartOfSpeech.VERB)) {
            return PoS.v;
        }
        
        else if (pos.equals(PartOfSpeech.ADJECTIVE)) {
            return PoS.adj;
        }
        
        // adjectives and adverbs are different parts of speech
        else if (pos.equals(PartOfSpeech.ADVERB)) {
        	return PoS.adv;
        }
        else {
            return PoS.unk;
        }
    }
    
    public static Set<PartOfSpeech> mapPos(PoS pos) {
        Set<PartOfSpeech> returnPos = new HashSet<PartOfSpeech>();
        if (pos.equals(PoS.n)) {
            returnPos.add(PartOfSpeech.NOUN);
            returnPos.add(PartOfSpeech.PROPER_NOUN);
        }
        else if (pos.equals(PoS.v)) {
            returnPos.add(PartOfSpeech.VERB);
        }
        else if (pos.equals(PoS.adj)) {
            returnPos.add(PartOfSpeech.ADJECTIVE);
        }
        else if (pos.equals(PoS.adv)) {
            returnPos.add(PartOfSpeech.ADVERB);
        }
        else {
            returnPos.add(PartOfSpeech.UNKNOWN);
        }
        return returnPos;
    }
    
    public static Set<WordEntry> entityToWords (Wiktionary wkt, Entity entity) {
        Set<WordEntry> words = new HashSet<WordEntry>();
        for (PartOfSpeech wktPos : WiktionaryUtils.mapPos(entity.getPos())) {
            // a Wiktionary entity only contains one lexeme
            words.addAll(wkt.getWordEntries(entity.getFirstLexeme(), wktPos));
        }
        return words;
    }

    public static String getGlossFromEntity(Wiktionary wkt, Entity entity) {
        for (PartOfSpeech wktPos : WiktionaryUtils.mapPos(entity.getPos())) {
            for (WordEntry w : wkt.getWordEntries(entity.getFirstLexeme(), wktPos)) {
                String sense = entity.getSense(entity.getFirstLexeme());
                if (sense.equals(Entity.UNKNOWN_SENSE)) {
                    StringBuilder glossString = new StringBuilder();
                    for (WikiString gloss : w.getGlosses()) {
                        glossString.append(gloss.getPlainText());
                        glossString.append(";");
                    }
                    if (glossString != null && glossString.length() > 0) {
                        return glossString.toString();
                    }
                }
                else {
                	WikiString gloss = w.getGloss(new Integer(sense));
                    String glossString = gloss.getPlainText();
                    if (glossString != null && glossString.length() > 0) {
                        return glossString;
                    }
                }
            }
        }
        return "";
    }
}
