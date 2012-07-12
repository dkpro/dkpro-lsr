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
package de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tud.sir.gn.GermaNetObject;
import org.tud.sir.gn.Synset;
import org.tud.sir.gn.WordSense;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;

public class GermaNetUtils {

    /**
     * @param synset A set of GermaNet synsets.
     * @return Creates a set of Entities from a set of synsets.
     */
    public static Set<Entity> synsetsToEntities(Set<Synset> synsets) {
        Set<Entity> entities = new HashSet<Entity>();
        for (Synset synset : synsets) {
            entities.add(synsetToEntity(synset));
        }
        return entities;
    }

    /**
     * @param synset A GermaNet synset.
     * @return Creates an Entity from a synset.
     */
    public static Entity synsetToEntity(Synset synset) {
        return new Entity(getSynsetLexemes(synset), GermaNetUtils.mapPos(synset.getPartOfSpeech()));
    }

    public static Set<Synset> entityToSynsets(GermaNetObject gno, Entity entity, boolean isCaseSensitive) {
        Set<Synset> synsets = new HashSet<Synset>();
        
        Set<String> lexemes = entity.getLexemes();
        PoS lsrPos = entity.getPos();
        
        if (lsrPos.equals(Entity.UNKNOWN_POS)) {
            // get the synsets for each lexeme
            for (String lexeme : lexemes) { 
            	if(isCaseSensitive){
                	List sensesList = gno.getWordSenses(lexeme);
                	if(sensesList == null) continue;
            	    for(int i = 0; i < sensesList.size(); ++i){
            	    	WordSense ws = (WordSense)sensesList.get(i);
            	    	if(lexeme.equals(ws.getGrapheme()))
            	    		synsets.add(ws.getSynset());
            	    	}            	
            	}else{
                    if (gno.getSynsets(lexeme) != null) 
                        synsets.addAll( gno.getSynsets(lexeme) );                              	
            	}
            }
        } else {
            // get the synsets for each lexeme
            for (String lexeme : lexemes) {            	
            	if(isCaseSensitive){
                	List sensesList = gno.getWordSenses(lexeme, GermaNetUtils.mapPos(lsrPos));
                	if(sensesList == null) continue;
            	    for(int i = 0; i < sensesList.size(); ++i){
            	    	WordSense ws = (WordSense)sensesList.get(i);
            	    	if(lexeme.equals(ws.getGrapheme()))
            	    		synsets.add(ws.getSynset());
            	    	}
            	    
            	}else{
                    if (gno.getSynsets(lexeme, GermaNetUtils.mapPos(lsrPos)) != null) {
                        synsets.addAll( gno.getSynsets(lexeme, GermaNetUtils.mapPos(lsrPos)) );
                    }                            	
            	}             
            }
        }        
        return synsets;
    }
    
    public static Map<String,String> getSynsetLexemes(Synset synset) {
        Map<String,String> result = new HashMap<String,String>();    
        
        if (synset == null) {
            return null;
        }
        
        List senses = synset.getWordSenses();
        for (int i = 0; i < senses.size(); i++) {
            WordSense wordSense = (WordSense) senses.get(i);
            String sense = new Integer(wordSense.getSense()).toString();
            String lexeme = wordSense.getGrapheme();
            result.put(lexeme, sense);
        }
        return result;
    }

    public static PoS mapPos(char pos) {
        if (pos ==  'n') {
            return PoS.n;
        }
        else if (pos == 'v') {
            return PoS.v;
        }
        else if (pos == 'a') {
            return PoS.adj;
        }
        else {
            return PoS.unk;
        }
    }

    public static char mapPos(PoS lsrPos) {
        if (lsrPos.equals(PoS.n)) {
            return 'n';
        }
        else if (lsrPos.equals(PoS.v)) {
            return 'v';
        }
        
        // adj and adv both map to 'a'
        else if (lsrPos.equals(PoS.adj) || lsrPos.equals(PoS.adv)) {
            return 'a';
        }
        
        else {
            return 'u';
        }
    }
}