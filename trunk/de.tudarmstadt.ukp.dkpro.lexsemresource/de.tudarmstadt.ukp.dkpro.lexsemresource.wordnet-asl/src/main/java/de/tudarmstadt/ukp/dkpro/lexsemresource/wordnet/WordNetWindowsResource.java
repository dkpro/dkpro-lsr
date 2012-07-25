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
package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.AbstractResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util.WordNetWindowsEntityIterable;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util.WordNetWindowsUtils;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.item.Synset;

/**
 * A WordNet resource that uses the MIT Java Wordnet Interface (JWI) rather than JWNL and thus also
 * runs on Windows. Works with WordNet 3.0.
 *
 * @author garoufi
 *
 */
public class WordNetWindowsResource extends AbstractResource {
    private final Log logger = LogFactory.getLog(getClass());

	private static final String RESOURCE_NAME = "wordnet_windows";
	static Dictionary dict;

	private int numberOfEntities = -1;


	public WordNetWindowsResource(String wordnetDictPath) throws LexicalSemanticResourceException {

		// construct the URL to the Wordnet dictionary directory
		URL url;
		try {
			url = new URL("file", null, wordnetDictPath);
		} catch (MalformedURLException e) {
			logger.info("URL not valid");
			throw new LexicalSemanticResourceException(e);
		}

		// construct the dictionary object and open it
		dict = new Dictionary(url);
		dict.open();
		setIsCaseSensitive(isCaseSensitive);

	}


	public boolean containsEntity(Entity entity) throws LexicalSemanticResourceException {
		Set<Synset> synsets = WordNetWindowsUtils.entityToSynsets(dict, entity, isCaseSensitive);
		if (synsets.size() == 0) {
		    return false;
		}
		else {
		    return true;
		}
	}


	public boolean containsLexeme(String lexeme) throws LexicalSemanticResourceException {
		// iterate over all POS to check if there is an index word with given lexeme
		for (POS pos : POS.values()) {
			IIndexWord indexWord = dict.getIndexWord(lexeme, pos);
			if (indexWord != null) {
				if (isCaseSensitive) {
					if (indexWord.getLemma().equals(lexeme)) {
                        return true;
					}
				} else {
					return true;
				}
			}

			/*if (idxWord != null) {
				String lemma = idxWord.getLemma();
				if (lemma.equals(lexeme)) {
					return true;
				}
			}*/
		}
		return false;
	}

	public Iterable<Entity> getEntities() {
		return new WordNetWindowsEntityIterable(dict);
	}




	public Set<Entity> getChildren(Entity entity) throws LexicalSemanticResourceException  {
		// deliberately used a set to collect results to allow other relation types to be added
		Set<Entity> children = new HashSet<Entity>();
		children.addAll(getRelatedEntities(entity, SemanticRelation.hyponymy));
		return children;
	}


	public Set<Entity> getEntity(String lexeme) throws LexicalSemanticResourceException {
		Set<Synset> synsets = WordNetWindowsUtils.toSynset(dict, lexeme, isCaseSensitive);
		return WordNetWindowsUtils.synsetsToEntities(synsets);
	}

	public Set<Entity> getEntity(String lexeme, PoS pos) throws LexicalSemanticResourceException {
		Set<Synset> synsets = WordNetWindowsUtils.toSynset(dict, lexeme, pos, isCaseSensitive);
		return WordNetWindowsUtils.synsetsToEntities(synsets);
	}

	public Set<Entity> getEntity(String lexeme, PoS pos, String sense) throws LexicalSemanticResourceException {
		Set<Entity> entities = new HashSet<Entity>();
		Entity e = WordNetWindowsUtils.getExactEntity(dict, lexeme, pos, sense, isCaseSensitive);
		if (e != null) {
			entities.add(e);
		}
		return entities;
	}

	public String getGloss(Entity entity) throws LexicalSemanticResourceException  {
		StringBuilder sb = new StringBuilder();
		Set<Synset> synsets = WordNetWindowsUtils.entityToSynsets(dict, entity, isCaseSensitive);
		for (Synset synset : synsets) {
			sb.append(synset.getGloss());
			sb.append(" ");
		}
		return sb.toString().trim();
	}

	// TODO is there a more efficient way?
	public int getNumberOfEntities() throws LexicalSemanticResourceException  {
		if (this.numberOfEntities < 0) {
			int i=0;
			Iterator adjIter = dict.getSynsetIterator(POS.ADJECTIVE);
			Iterator advIter  = dict.getSynsetIterator(POS.ADVERB);
			Iterator nounIter = dict.getSynsetIterator(POS.NOUN);
			Iterator verbIter = dict.getSynsetIterator(POS.VERB);

			while(adjIter.hasNext()) {
				i++;
				adjIter.next();
			}
			while(advIter.hasNext()) {
				i++;
				advIter.next();
			}
			while(nounIter.hasNext()) {
				i++;
				nounIter.next();
			}
			while(verbIter.hasNext()) {
				i++;
				verbIter.next();
			}
			numberOfEntities = i;
		}
		return numberOfEntities;
	}

	public Set<Entity> getParents(Entity entity) throws LexicalSemanticResourceException {
		// deliberately used a set to collect results to allow other relation types to be added
		Set<Entity> parents = new HashSet<Entity>();
		parents.addAll(getRelatedEntities(entity, SemanticRelation.hypernymy));
		return parents;
	}

	public Set<Entity> getRelatedEntities(Entity entity, SemanticRelation semanticRelation) throws LexicalSemanticResourceException  {
		Set<Entity> relatedEntities = new HashSet<Entity>();
		Set<Synset> synsets = WordNetWindowsUtils.entityToSynsets(dict, entity, isCaseSensitive);

		for (Synset synset : synsets) {
			List<ISynsetID> synIDList = getNodeListByRelation(synset, semanticRelation);
			if (synIDList != null) {
				for (ISynsetID synID : synIDList) {
					Synset nodeSynset = (Synset) dict.getSynset(synID);
					relatedEntities.add(WordNetWindowsUtils.synsetToEntity(nodeSynset));
				}
			}
		}
		return relatedEntities;
	}

	// one relation of the interface can be mapped to several relations in WordNet 3.0
	private List<ISynsetID> getNodeListByRelation(Synset synset, SemanticRelation relationType) throws LexicalSemanticResourceException {
		List<ISynsetID> nodeList = new ArrayList<ISynsetID>();

		if (relationType.equals(SemanticRelation.holonymy)) {
			nodeList.addAll(synset.getRelatedSynsets(Pointer.HOLONYM_MEMBER));
			nodeList.addAll(synset.getRelatedSynsets(Pointer.HOLONYM_PART));
			nodeList.addAll(synset.getRelatedSynsets(Pointer.HOLONYM_SUBSTANCE));
		}
		else if (relationType.equals(SemanticRelation.hypernymy)) {
			nodeList.addAll(synset.getRelatedSynsets(Pointer.HYPERNYM));
			nodeList.addAll(synset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE));
		}
		else if (relationType.equals(SemanticRelation.hyponymy)) {
			nodeList.addAll(synset.getRelatedSynsets(Pointer.HYPONYM));
			nodeList.addAll(synset.getRelatedSynsets(Pointer.HYPONYM_INSTANCE));
		}
		else if (relationType.equals(SemanticRelation.meronymy)) {
			nodeList.addAll(synset.getRelatedSynsets(Pointer.MERONYM_MEMBER));
			nodeList.addAll(synset.getRelatedSynsets(Pointer.MERONYM_PART));
			nodeList.addAll(synset.getRelatedSynsets(Pointer.MERONYM_SUBSTANCE));
		}
		return nodeList;
	}

	public String getResourceName() {
		return RESOURCE_NAME;
	}

	public String getResourceVersion() {
		return dict.getVersion().toString();
	}

    @Override
    public Entity getRoot() throws LexicalSemanticResourceException {
        Map<String,String> rootLexemes = new HashMap<String,String>();
        rootLexemes.put("entity", "1740");

        try {
            return this.getEntity(rootLexemes, PoS.n);
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }


    @Override
    public Entity getRoot(PoS pos) throws LexicalSemanticResourceException {
        if (pos.equals(PoS.n)) {
            return getRoot();
        }
        else {
            return null;
        }
    }

	public Set<String> getRelatedLexemes(String lexeme, PoS pos, String sense,
			LexicalRelation lexicalRelation) {
		throw new UnsupportedOperationException();
	}

	public int getShortestPathLength(Entity firstEntity, Entity secondEntity) {
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