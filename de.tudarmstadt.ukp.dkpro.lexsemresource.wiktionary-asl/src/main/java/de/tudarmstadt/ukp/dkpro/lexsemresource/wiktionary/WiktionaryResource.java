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
package de.tudarmstadt.ukp.dkpro.lexsemresource.wiktionary;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sleepycat.je.DatabaseException;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.AbstractResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wiktionary.util.WiktionaryEntityIterable;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wiktionary.util.WiktionaryUtils;
import de.tudarmstadt.ukp.wiktionary.api.Language;
import de.tudarmstadt.ukp.wiktionary.api.PartOfSpeech;
import de.tudarmstadt.ukp.wiktionary.api.RelationType;
import de.tudarmstadt.ukp.wiktionary.api.Wiktionary;
import de.tudarmstadt.ukp.wiktionary.api.WordEntry;

public class WiktionaryResource extends AbstractResource {

    private final Log log = LogFactory.getLog(getClass());

    private static final String resourceName = "Wiktionary";

    private Wiktionary wkt;
    private Iterable<Entity> wktIterable;
    private String version;

    public
    WiktionaryResource(
    		Language language,
    		String wiktionaryDirectory)
    throws LexicalSemanticResourceException
    {
        try {
        	// Check if we got an URL (file URL)
        	String dir = null;
        	try {
        		URL url = new URL(wiktionaryDirectory);
        		if ("file".equals(url.getProtocol())) {
        			dir = new File(url.getPath()).getAbsolutePath();
        		}
        		else {
        			throw new LexicalSemanticResourceException(
        					"Wiktionary resources have to reside on the file "+
        					"system, but are at ["+url+"]");
        		}
        	}
        	catch (IOException e) {
        		// Ignore
        	}

        	if (dir == null) {
        		dir = wiktionaryDirectory;
        	}

            wkt = new Wiktionary(dir);

            // use a certain Wiktionary DB
            wkt.setAllowedEntryLanguage(language);
            log.info("Setting PreferedEntryLanguage to " + language.toString());

            // use only words from a certain language
            wkt.setAllowedWordLanguage(language);
            log.info("Setting PreferedWordLanguage to " + language.toString());

            wkt.setIsCaseSensitive(isCaseSensitive);

            wktIterable = new WiktionaryEntityIterable(wkt);

            version = language.toString();

        } catch (DatabaseException e) {
            throw new LexicalSemanticResourceException(e);
        }
    }

    @Override
	public void setIsCaseSensitive(boolean isCaseSensitive){
        this.isCaseSensitive = isCaseSensitive;
        wkt.setIsCaseSensitive(isCaseSensitive);
    }

    @Override
	public boolean getIsCaseSensitive(){
        assert(wkt.getIsCaseSensitive() == this.isCaseSensitive);
        return isCaseSensitive;
    }

    public boolean containsLexeme(String lexeme) {
        boolean contained = false;
        List<WordEntry> words = wkt.getWordEntries(lexeme);
        if (words.size() != 0) {
            contained = true;
        }
        return contained;
    }

    public boolean containsEntity(Entity entity) {
        Set<WordEntry> entries = WiktionaryUtils.entityToWords(wkt, entity);
        if (entries.size() == 0) {
            return false;
        }
        else {
            return true;
        }
    }

    public Set<Entity> getEntity(String lexeme) {
        Set<Entity> resultEntities = new HashSet<Entity>();
        List<WordEntry> words = wkt.getWordEntries(lexeme);
        for (WordEntry word : words) {
            resultEntities.add( new Entity(lexeme, WiktionaryUtils.mapPos(word.getPartOfSpeech())));
        }
        return resultEntities;
    }

    public Set<Entity> getEntity(String lexeme, PoS pos) {
        Set<Entity> resultEntities = new HashSet<Entity>();
        List<WordEntry> words = new ArrayList<WordEntry>();
        for (PartOfSpeech wktPos : WiktionaryUtils.mapPos(pos)) {
            words.addAll(wkt.getWordEntries(lexeme, wktPos));
        }

        for (WordEntry word : words) {
            resultEntities.add( new Entity(lexeme, WiktionaryUtils.mapPos(word.getPartOfSpeech())));
        }
        return resultEntities;
    }

    public Set<Entity> getEntity(String lexeme, PoS pos, String sense) throws LexicalSemanticResourceException {
        Set<Entity> resultEntities = getEntity(lexeme, pos);
        Iterator<Entity> entityIter = resultEntities.iterator();
        while (entityIter.hasNext()) {
            Entity e = entityIter.next();

            // one of the lexemes must be the given lexeme with the given pos
            boolean found = false;
            for (String currentLexeme : e.getLexemes()) {
                if (currentLexeme.equals(lexeme) && e.getSense(currentLexeme).equals(sense)) {
                    found = true;
                }
            }
            // remove entity from result set, if not found
            if (!found) {
                entityIter.remove();
            }
        }
        return resultEntities;
    }

    public Set<Entity> getParents(Entity entity) {
        List<String> children = new ArrayList<String>();
		for (WordEntry word : WiktionaryUtils.entityToWords(wkt, entity)) {
	        for (String relation : word.getAllRelatedWords(RelationType.HYPERNYM)) {
	            	children.add(relation);
	        }
		}
		Set<Entity> results = new HashSet<Entity>();
		for (String child : children) {
		    results.addAll(this.getEntity(child));
		}
		return results;
    }

    public Set<Entity> getChildren(Entity entity) throws LexicalSemanticResourceException {
        List<String> children = new ArrayList<String>();
		for (WordEntry word : WiktionaryUtils.entityToWords(wkt, entity)) {
	        for (String relation : word.getAllRelatedWords(RelationType.HYPONYM)) {
            	children.add(relation);
        }
		}
		Set<Entity> results = new HashSet<Entity>();
		for (String child : children) {
		    results.addAll(this.getEntity(child));
		}
		return results;
    }

    // TODO we need a method in the API that returns the real value
    public int getNumberOfEntities() throws LexicalSemanticResourceException {
        int i=0;
        Iterator<WordEntry> wordEntryIter = wkt.wordEntryIterator();
        while (wordEntryIter.hasNext()) {
            wordEntryIter.next();
            i++;
        }
        return i;
    }

    public Iterable<Entity> getEntities() throws LexicalSemanticResourceException {
    	try{
    		wktIterable = new WiktionaryEntityIterable(wkt);
        } catch (DatabaseException e) {
            throw new LexicalSemanticResourceException(e);
        }
        return wktIterable;
    }

    @Override
	public Set<Entity> getNeighbors(Entity entity) throws LexicalSemanticResourceException {
    	//TODO when getParents is implemented, this must change
    	return getChildren(entity);
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceVersion() {
        return version;
    }

    public int getShortestPathLength(Entity firstEntity, Entity secondEntity) {
        throw new UnsupportedOperationException();
    }

    public String getGloss(Entity entity) throws LexicalSemanticResourceException  {
        return WiktionaryUtils.getGlossFromEntity(wkt, entity);
    }

//    private List<String> getGloss(Set<Word> words) throws LexicalSemanticResourceException {
//    	List<String> glosses = new ArrayList<String>();
//    	for (Word word : words) {
//    		for (String gloss : word.gloss()) {
//    			glosses.add(gloss);
//
//    			// add gloss separator as temporary workaround until entities are modeled properly
//    			glosses.add("#");
//    		}
//    	}
//    	return glosses;
//    }

    public Set<Entity> getRelatedEntities(Entity entity, SemanticRelation semanticRelation) throws LexicalSemanticResourceException {
        Set<String> relatedWords = new HashSet<String>();
        Set<WordEntry> words = WiktionaryUtils.entityToWords(wkt, entity);
        for (WordEntry word : words) {
            if (semanticRelation.equals(SemanticRelation.holonymy)) {
                relatedWords.addAll(word.getAllRelatedWords(RelationType.HOLONYM));
            }
            else if (semanticRelation.equals(SemanticRelation.hypernymy)) {
                relatedWords.addAll(word.getAllRelatedWords(RelationType.HYPERNYM));
            }
            else if (semanticRelation.equals(SemanticRelation.hyponymy)) {
                relatedWords.addAll(word.getAllRelatedWords(RelationType.HYPONYM));
            }
            else if (semanticRelation.equals(SemanticRelation.meronymy)) {
                relatedWords.addAll(word.getAllRelatedWords(RelationType.MERONYM));
            }
            else if (semanticRelation.equals(SemanticRelation.cohyponymy)) {
                relatedWords.addAll(word.getAllRelatedWords(RelationType.COORDINATE_TERM));
            }
            else if (semanticRelation.equals(SemanticRelation.other)) {
                relatedWords.addAll(word.getAllRelatedWords(RelationType.SEE_ALSO));
            }
        }
        Set<Entity> results = new HashSet<Entity>();
        for (String relatedWord : relatedWords) {
            results.addAll(this.getEntity(relatedWord));
        }
        return results;
    }

    public Set<String> getRelatedLexemes(String lexeme, PoS pos, String sense, LexicalRelation lexicalRelation) throws LexicalSemanticResourceException {
        Map<String,String> lexemeMap = new HashMap<String,String>();
        lexemeMap.put(lexeme, sense);
        Entity entity = this.getEntity(lexemeMap, pos);

        if (entity == null) {
            return Collections.emptySet();
        }

        Set<String> relatedWords = new HashSet<String>();
        Set<WordEntry> words = WiktionaryUtils.entityToWords(wkt, entity);
        for (WordEntry word : words) {
            if (lexicalRelation.equals(LexicalRelation.antonymy)) {
                relatedWords.addAll(word.getAllRelatedWords(RelationType.ANTONYM));
            }
            else if (lexicalRelation.equals(LexicalRelation.synonymy)) {
                relatedWords.addAll(word.getAllRelatedWords(RelationType.SYNONYM));
            }
        }
        return relatedWords;

    }

    @Override
    public Entity getRoot() throws LexicalSemanticResourceException {
        return null;
    }

    @Override
    public Entity getRoot(PoS pos) throws LexicalSemanticResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity getMostFrequentEntity(String lexeme)
        throws LexicalSemanticResourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity getMostFrequentEntity(String lexeme, PoS pos)
        throws LexicalSemanticResourceException
    {
        throw new UnsupportedOperationException();
    }
}