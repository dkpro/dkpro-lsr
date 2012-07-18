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
package de.tudarmstadt.ukp.dkpro.lexsemresource.openthesaurus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.AbstractResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.openthesaurus.util.OpenThesaurusEntityIterable;
import de.tudarmstadt.ukp.dkpro.lexsemresource.openthesaurus.util.OpenThesaurusUtils;
import de.tudarmstadt.ukp.openthesaurus.api.OTLanguage;
import de.tudarmstadt.ukp.openthesaurus.api.OpenThesaurus;
import de.tudarmstadt.ukp.openthesaurus.api.Synset;
import de.tudarmstadt.ukp.openthesaurus.api.SynsetLinkType;
import de.tudarmstadt.ukp.openthesaurus.api.Term;
import de.tudarmstadt.ukp.openthesaurus.api.TermLinkType;
import de.tudarmstadt.ukp.openthesaurus.db.DatabaseConfiguration;
import de.tudarmstadt.ukp.openthesaurus.exception.OpenThesaurusException;

/**
 *
 * @author chebotar
 *
 */
public class OpenThesaurusResource extends AbstractResource {

	DatabaseConfiguration dbConfig;
	OpenThesaurus openThesaurus;
	private final static String resourceName = "OpenThesaurus";
	private final static String resourceVersion = "1.0";

	/**
	 * Needs database configuration and language
	 * @param host
	 * @param database
	 * @param user
	 * @param password
	 * @param language
	 * @throws LexicalSemanticResourceException
	 */
	public OpenThesaurusResource(String host, String database, String user, String password, String language) throws LexicalSemanticResourceException {
	       this(new DatabaseConfiguration(host, database, user, password, resolveLanguage(language)));
	}

	public OpenThesaurusResource(DatabaseConfiguration dbConfig) throws LexicalSemanticResourceException {
        try {
        	this.dbConfig = dbConfig;
        	this.isCaseSensitive = false;
        	this.openThesaurus = new OpenThesaurus(dbConfig, isCaseSensitive);

        } catch (OpenThesaurusException e) {
	            throw new LexicalSemanticResourceException("OpenThesaurus could not be initialized.",e);
        }
	 }

	protected static int resolveLanguage(final String language) {
		String l = language.toLowerCase().trim();
		if ("de".equals(l)) {
			return OTLanguage.GERMAN;
		}
		if ("en".equals(l)) {
			return OTLanguage.ENGLISH;
		}
		return 0;
	}

	@Override
	public boolean containsEntity(Entity entity) throws LexicalSemanticResourceException  {
		try {

			Synset synset = OpenThesaurusUtils.entityToSynset(openThesaurus, entity);

			if(synset == null) {
				return false;
			}

			Entity newEntity = OpenThesaurusUtils.synsetToEntity(openThesaurus, synset);

			return entity.equals(newEntity);

		} catch (OpenThesaurusException e) {
			throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}
	}


	@Override
	public boolean containsLexeme(String lexeme) throws LexicalSemanticResourceException {
		try{
			return openThesaurus.getTermsByWord(lexeme).isEmpty();

		}catch(OpenThesaurusException e){
			throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}
	}


	@Override
	public Iterable<Entity> getEntities() throws LexicalSemanticResourceException {
		try {
			return new OpenThesaurusEntityIterable(openThesaurus);
		} catch (OpenThesaurusException e) {
			throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}
	}


	@Override
	public Set<Entity> getEntity(String lexeme)	throws LexicalSemanticResourceException {

		Set<Entity> result = new HashSet<Entity>();
		try {
			Set<Synset> synsets = openThesaurus.getSynsetsByWord(lexeme);

			for(Synset syn : synsets){
				result.add(OpenThesaurusUtils.synsetToEntity(openThesaurus, syn));
			}

		} catch (OpenThesaurusException e) {
			 throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}

		return result;
	}


	@Override
	public Set<Entity> getEntity(String lexeme, PoS pos) throws LexicalSemanticResourceException {
		return getEntity(lexeme);
	}


	@Override
	public Set<Entity> getEntity(String lexeme, PoS pos, String sense) throws LexicalSemanticResourceException {

		Set<Entity> result =  new HashSet<Entity>();
		try {
			Term term = OpenThesaurusUtils.getTermBySense(openThesaurus, lexeme,
													sense, isCaseSensitive);

			if(term == null) {
				return result;
			}

			Synset synset = term.getSynset();

			result.add(OpenThesaurusUtils.synsetToEntity(openThesaurus, synset));

		} catch (OpenThesaurusException e) {
			 throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}

		return result;
	}


	@Override
	public String getGloss(Entity entity) throws LexicalSemanticResourceException {
		throw new UnsupportedOperationException();
	}


	@Override
	public int getNumberOfEntities() throws LexicalSemanticResourceException {
		try {
			return openThesaurus.getNumberOfSynsets();
		} catch (OpenThesaurusException e) {
			throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}
	}


	@Override
	public Set<Entity> getChildren(Entity entity)
			throws LexicalSemanticResourceException {

		Set<Entity> children = new HashSet<Entity>();
		try {

			Synset synset = OpenThesaurusUtils.entityToSynset(openThesaurus, entity);

			for(Synset syn : synset.getSynsetLinksBackwards(SynsetLinkType.HYPERNYMY)){

				children.add(OpenThesaurusUtils.synsetToEntity(openThesaurus, syn));
			}

		} catch (OpenThesaurusException e) {
			throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}
		return children;
	}


	@Override
	public Set<Entity> getParents(Entity entity) throws LexicalSemanticResourceException {

		Set<Entity> parents = new HashSet<Entity>();
		try {

			Synset synset = OpenThesaurusUtils.entityToSynset(openThesaurus, entity);

			for(Synset syn : synset.getSynsetLinks(SynsetLinkType.HYPERNYMY)){

				parents.add(OpenThesaurusUtils.synsetToEntity(openThesaurus, syn));
			}

		} catch (OpenThesaurusException e) {
			throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}
		return parents;
	}


	@Override
	public Set<Entity> getRelatedEntities(Entity entity, SemanticRelation semanticRelation)
			throws LexicalSemanticResourceException {

		if(semanticRelation.equals(SemanticRelation.hypernymy)){
			return this.getParents(entity);
		}else if(semanticRelation.equals(SemanticRelation.hyponymy)){
			return this.getChildren(entity);
		}
		else {
			return Collections.emptySet();
		}
	}


	@Override
	public Set<String> getRelatedLexemes(String lexeme, PoS pos, String sense,
			LexicalRelation lexicalRelation) throws LexicalSemanticResourceException {

		Set<String> relatedLexemes = new HashSet<String>();
		try {
			Term term = OpenThesaurusUtils.getTermBySense(openThesaurus, lexeme,
					sense, isCaseSensitive);

			if(term == null) {
				return relatedLexemes;
			}

			if(lexicalRelation.equals(LexicalRelation.antonymy)){
				for(Term t : term.getTermLinks(TermLinkType.ANTONYMY)){
					relatedLexemes.add(t.getWord());
				}
			}
		} catch (OpenThesaurusException e) {
			throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}

		return relatedLexemes;
	}

	@Override
	/*--Shortest Path. Moore-Algorithm--*/

	//Generic with Entities, slow
	/*
	public int getShortestPathLength(Entity e1, Entity e2) throws LexicalSemanticResourceException,
			UnsupportedOperationException {

		if(e1.equals(e2))
			return 0;

		HashMap<Entity, Integer> distances = new HashMap<Entity, Integer>();
		distances.put(e1, 0);

		LinkedList<Entity> queue = new LinkedList<Entity>();
		queue.add(e1);

		while(!queue.isEmpty()){
			Entity entity = queue.pop();

			for(Entity neighbor : getNeighbors(entity)){
				if(neighbor.equals(e2))
					return distances.get(entity)+1;

				if(!distances.containsKey(neighbor)){
					queue.add(neighbor);
					distances.put(neighbor, distances.get(entity)+1);
				}
			}
		}

		return -1;
	}
	*/

	//Faster, with Synsets
	public int getShortestPathLength(Entity e1, Entity e2) throws LexicalSemanticResourceException {

		if(e1.equals(e2)) {
			return 0;
		}

		try{

			Synset s1 = OpenThesaurusUtils.entityToSynset(openThesaurus, e1);
			Synset s2 = OpenThesaurusUtils.entityToSynset(openThesaurus, e2);

			HashMap<Synset, Integer> distances = new HashMap<Synset, Integer>();
			distances.put(s1, 0);

			LinkedList<Synset> queue = new LinkedList<Synset>();
			queue.add(s1);

			while(!queue.isEmpty()){
				Synset synset = queue.pop();

				Set<Synset> neighbors = synset.getSynsetLinks(SynsetLinkType.HYPERNYMY);
				neighbors.addAll(synset.getSynsetLinksBackwards(SynsetLinkType.HYPERNYMY));

				for(Synset neighbor : neighbors){
					if(neighbor.equals(s2)) {
						return distances.get(synset)+1;
					}

					if(!distances.containsKey(neighbor)){
						queue.add(neighbor);
						distances.put(neighbor, distances.get(synset)+1);
					}
				}
			}
		}catch (OpenThesaurusException e){
			throw new LexicalSemanticResourceException("OpenThesaurus Exception ",e);
		}
		return -1;
	}


   @Override
	public void setIsCaseSensitive(boolean isCaseSensitive){
        this.isCaseSensitive = isCaseSensitive;
        try {
			openThesaurus.setIsCaseSensitive(isCaseSensitive);
		} catch (OpenThesaurusException e) {
			e.printStackTrace();
		}

    }

    @Override
	public boolean getIsCaseSensitive(){
        assert(openThesaurus.getIsCaseSensitive() == this.isCaseSensitive);
        return isCaseSensitive;
    }

	@Override
	public String getResourceName() {
		return resourceName;
	}

	@Override
	public String getResourceVersion() {
		return resourceVersion;
	}

	@Override
	public Entity getRoot() throws LexicalSemanticResourceException {
		return null;
	}

	@Override
	public Entity getRoot(PoS pos) throws LexicalSemanticResourceException {
		return null;
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
