/*******************************************************************************
 * Copyright 2012
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.IndexWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Synset;

public class WordNetWindowsUtils {
    /**
     * @param synsets A set of WordNet synsets.
     * @return a set of Entities from a set of synsets.
     */
    public static Set<Entity> synsetsToEntities(Set<Synset> synsets) {
        Set<Entity> entities = new HashSet<Entity>();
        for (Synset synset : synsets) {
            entities.add(synsetToEntity(synset));
        }
        return entities;
    }


    /**
     * @param synset A WordNet synset.
     * @return Creates an Entity from a synset.
     */
    public static Entity synsetToEntity(Synset synset) {
        if (synset == null) {
            return null;
        }
        return new Entity(getSynsetLexemes(synset), mapPos(synset.getPOS()));
    }

    public static Map<String,String> getSynsetLexemes(Synset synset) {
        Map<String,String> result = new HashMap<String,String>();

        long sense = synset.getOffset();

        List<IWord> words = synset.getWords();
        for (IWord word : words) {
            String lexeme = word.getLemma();
            lexeme = cleanLexeme(lexeme);
            result.put(lexeme, new Long(sense).toString());
        }
        return result;
    }



    // remove some suffixes that might be added to the synset representation
    // due to errors in WordNet (?)
    private static String cleanLexeme(String lexeme) {
        if (lexeme.endsWith("(n)") || lexeme.endsWith("(a)") || lexeme.endsWith("(v)") || lexeme.endsWith("(p)")) {
            return lexeme.substring(0, lexeme.length()-3);
        }
        else if (lexeme.endsWith("(ip)")) {
            return lexeme.substring(0, lexeme.length()-4);
        }
        return lexeme;
    }

    public static PoS mapPos(POS pos) {
        if (pos.equals(POS.NOUN)) {
            return PoS.n;
        }
        else if (pos.equals(POS.VERB)) {
            return PoS.v;
        }
        else if (pos.equals(POS.ADJECTIVE)) {
            return PoS.adj;
        }
        else if (pos.equals(POS.ADVERB)) {
        	return PoS.adv;
        }
        else {
            return PoS.unk;
        }
    }

    public static Set<POS> mapPos(PoS lsrPos) {
        Set<POS> pos = new HashSet<POS>();
        if (lsrPos.equals(PoS.n)) {
            pos.add(POS.NOUN);
        }
        else if (lsrPos.equals(PoS.v)) {
            pos.add(POS.VERB);
        }
        else if (lsrPos.equals(PoS.adj)) {
            pos.add(POS.ADJECTIVE);
        }
        else if (lsrPos.equals(PoS.adv)) {
            pos.add(POS.ADVERB);
        }
        else {
            // defaults to noun
            pos.add(POS.NOUN);
        }
        return pos;
    }



    public static Set<Synset> toSynset(Dictionary dict, String lexeme, boolean isCaseSensitive) throws LexicalSemanticResourceException {

            Set<IIndexWord> indexWords = new HashSet<IIndexWord>();
            Set<Synset> resultsSynsets = new HashSet<Synset>();
            for (POS pos : POS.values()) {
            	IIndexWord indexWord = dict.getIndexWord(lexeme, pos);
            	if (indexWord != null) {
	            	if (!isCaseSensitive) {
	            		indexWords.add(indexWord);
	            	}
	            	else {
	            		if(indexWord.getLemma().equals(lexeme)) {
                            indexWords.add(indexWord);
	            		}
	            	}
            	}
            }


            for (IIndexWord indexWord : indexWords) {
                if (indexWord != null) {
                    List<IWordID> iwordIDs = indexWord.getWordIDs();
                    for (IWordID iwordID : iwordIDs) {
                        resultsSynsets.add((Synset) dict.getSynset(iwordID.getSynsetID()));
                    }
                }
            }
            return resultsSynsets;
    }


    public static Set<Synset> toSynset(Dictionary dict, String lexeme, PoS pos, boolean isCaseSensitive) {

        Set<IndexWord> indexWords = new HashSet<IndexWord>();
        for (POS wnPOS : mapPos(pos)) {
        	IndexWord indexWord = (IndexWord)dict.getIndexWord(lexeme, wnPOS);
            if (indexWord != null) {
	        	if(!isCaseSensitive) {
                    indexWords.add(indexWord);
	        	}
	            else {
	            	if (indexWord.getLemma().equals(lexeme)) {
                        indexWords.add(indexWord);
	            	}
	            }
            }
        }
        Set<Synset> resultsSynsets = new HashSet<Synset>();
        for (IndexWord indexWord : indexWords) {
            if (indexWord != null) {
                List<IWordID> iwordIDs = indexWord.getWordIDs();
                for (IWordID iwordID : iwordIDs) {
                    resultsSynsets.add((Synset) dict.getSynset(iwordID.getSynsetID()));
                }
            }
        }
        return resultsSynsets;
    }



    public static Synset toSynset(Dictionary dict, String lexeme, PoS pos, String sense, boolean isCaseSensitive) {
        Set<Synset> possibleSynsets = toSynset(dict, lexeme, pos, isCaseSensitive);
        for (Synset synset : possibleSynsets) {
            Integer senseNumber = synset.getOffset();

            // if the sense number matches, then we have found the correct synset
            if (senseNumber.toString().equals(sense)) {
                return synset;
            }
        }
        // if we get here, there is no matching synset
        return null;
    }



    public static Set<Synset> entityToSynsets(Dictionary dict, Entity entity, boolean isCaseSensitive) throws LexicalSemanticResourceException {
        Set<Synset> results = new HashSet<Synset>();

        Set<String> lexemes = entity.getLexemes();
        PoS pos = entity.getPos();
        String sense = entity.getSense(entity.getFirstLexeme());
        Long senseNumber = Long.MIN_VALUE;
        if (sense != Entity.UNKNOWN_SENSE) {
            try {
                senseNumber = new Long(sense);
            } catch (NumberFormatException e) {
                throw new LexicalSemanticResourceException(e);
            }
        }

        // we can directly get the synset - a WordNet is synset is disambiguated by a POS and a sense number
        if (senseNumber != Long.MIN_VALUE && pos != Entity.UNKNOWN_POS) {
            // it does not matter which lexeme, so simply take the first
            Synset synset = toSynset(dict, lexemes.iterator().next(), pos, sense, isCaseSensitive);
            if (synset != null) {
                results.add(synset);
            }
        }
        else if (pos.equals(Entity.UNKNOWN_POS)) {
            // get the synsets for each lexeme
            for (String lexeme : lexemes) {
                Set<Synset> synsets = toSynset(dict, lexeme, isCaseSensitive);
                if (synsets != null) {
                    results.addAll(synsets);
                }
            }
        }
        else {
            // get the synsets for each lexeme
            for (String lexeme : lexemes) {
                Set<Synset> synsets = toSynset(dict, lexeme, pos, isCaseSensitive);
                if (synsets != null) {
                    results.addAll(synsets);
                }
            }
        }

        return results;
    }

    // a wordnet synset is unambiguously indicated by a pos and a sense number
    public static Entity getExactEntity(Dictionary dict, String lexeme, PoS pos, String sense, boolean isCaseSensitive) throws LexicalSemanticResourceException {
        return synsetToEntity(toSynset(dict, lexeme, pos, sense, isCaseSensitive));
    }
}
