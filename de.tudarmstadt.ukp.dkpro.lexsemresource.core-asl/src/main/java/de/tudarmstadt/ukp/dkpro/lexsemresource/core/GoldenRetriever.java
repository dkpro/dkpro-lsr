/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.lexsemresource.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;

/**
 * LSR wrapper. Handles cases where an entity is looked up by its lexeme but the lexeme cannot
 * directly be found in the semantic resource. In this case the lexeme may be split up if it
 * contains dashes, spaces or underscores and each segment will be looked up independently. The
 * returned entity set is then the union of the entity sets of each segment.
 *
 * @author Richard Eckart de Castilho
 */
public class GoldenRetriever
	implements LexicalSemanticResource
{
	private final LexicalSemanticResource lsr;
	private final Map<String, Set<Entity>> cache = new HashMap<String, Set<Entity>>();

	public GoldenRetriever(LexicalSemanticResource aLsr)
	{
		super();
		lsr = aLsr;
	}

	@Override
	public boolean containsEntity(Entity aEntity)
		throws LexicalSemanticResourceException
	{
		return lsr.containsEntity(aEntity);
	}

	@Override
	public Set<Entity> getEntity(String aLexeme)
		throws LexicalSemanticResourceException
	{
		String usLexeme = aLexeme;
		usLexeme = usLexeme.replace('-', ' ');
		usLexeme = usLexeme.replace('_', ' ');
		usLexeme = StringUtils.join(usLexeme.split("[ ]+"), "_");

		Set<Entity> result = cache.get(usLexeme+"-ALL");
		if (result != null) {
			return result;
		}

		result = lsr.getEntity(usLexeme);
		if (result.size() > 0) {
			return result;
		}

		result = new HashSet<Entity>();
		for (String fragment : usLexeme.split("[_]+")) {
			result.addAll(lsr.getEntity(fragment));
		}

		if (cache != null) {
			cache.put(usLexeme+"-ALL", result);
		}

		return result;
	}

	@Override
	public Set<Entity> getEntity(String aLexeme, PoS aPos)
		throws LexicalSemanticResourceException
	{
		String usLexeme = aLexeme;
		usLexeme = usLexeme.replace('-', ' ');
		usLexeme = usLexeme.replace('_', ' ');
		usLexeme = StringUtils.join(usLexeme.split("[ ]+"), "_");

		Set<Entity> result = cache.get(usLexeme+"-"+aPos);
		if (result != null) {
			return result;
		}

		result = lsr.getEntity(usLexeme, aPos);
		if (result.size() > 0) {
			return result;
		}

		result = new HashSet<Entity>();
		for (String fragment : usLexeme.split("[_]+")) {
			result.addAll(lsr.getEntity(fragment, aPos));
		}

		if (cache != null) {
			cache.put(usLexeme+"-"+aPos, result);
		}

		return result;
	}

	@Override
	public Set<Entity> getEntity(String aLexeme, PoS aPos, String aSense)
		throws LexicalSemanticResourceException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity getEntity(Map<String, String> aLexemes, PoS aPos)
		throws LexicalSemanticResourceException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity getEntityById(String aId)
		throws LexicalSemanticResourceException
	{
		return lsr.getEntityById(aId);
	}

	@Override
	public boolean containsLexeme(String aLexeme)
		throws LexicalSemanticResourceException
	{
		return getEntity(aLexeme).size() > 0;
	}

	@Override
	public Set<Entity> getChildren(Entity aEntity)
		throws LexicalSemanticResourceException
	{
		return lsr.getChildren(aEntity);
	}

	@Override
	public Iterable<Entity> getEntities()
		throws LexicalSemanticResourceException
	{
		return lsr.getEntities();
	}

	@Override
	public String getGloss(Entity aEntity)
		throws LexicalSemanticResourceException
	{
		return lsr.getGloss(aEntity);
	}

	@Override
	public boolean getIsCaseSensitive()
	{
		return lsr.getIsCaseSensitive();
	}

	@Override
	public Set<Entity> getNeighbors(Entity aEntity)
		throws LexicalSemanticResourceException
	{
		return lsr.getNeighbors(aEntity);
	}

	@Override
	public int getNumberOfEntities()
		throws LexicalSemanticResourceException
	{
		return lsr.getNumberOfEntities();
	}

	@Override
	public Set<Entity> getParents(Entity aEntity)
		throws LexicalSemanticResourceException
	{
		return lsr.getParents(aEntity);
	}

	@Override
	public String getPseudoGloss(Entity aEntity, Set<LexicalRelation> aLexicalRelations,
			Map<SemanticRelation, Integer> aSemanticRelations)
		throws LexicalSemanticResourceException
	{
		return lsr.getPseudoGloss(aEntity, aLexicalRelations, aSemanticRelations);
	}

	@Override
	public Set<Entity> getRelatedEntities(Entity aEntity, SemanticRelation aSemanticRelation)
		throws LexicalSemanticResourceException
	{
		return lsr.getRelatedEntities(aEntity, aSemanticRelation);
	}

	@Override
	public Set<String> getRelatedLexemes(String aLexeme, PoS aPos, String aSense,
			LexicalRelation aLexicalRelation)
		throws LexicalSemanticResourceException
	{
		return lsr.getRelatedLexemes(aLexeme, aPos, aSense, aLexicalRelation);
	}

	@Override
	public String getResourceName()
	{
		return lsr.getResourceName();
	}

	@Override
	public String getResourceVersion()
	{
		return lsr.getResourceVersion()+"-"+getClass().getSimpleName();
	}

	@Override
	public Entity getRoot()
		throws LexicalSemanticResourceException
	{
		return lsr.getRoot();
	}

	@Override
	public Entity getRoot(PoS aPos)
		throws LexicalSemanticResourceException
	{
		return lsr.getRoot(aPos);
	}

	@Override
	public int getShortestPathLength(Entity aE1, Entity aE2)
		throws LexicalSemanticResourceException
	{
		return lsr.getShortestPathLength(aE1, aE2);
	}

	@Override
	public void setIsCaseSensitive(boolean aIsCaseSensitive)
	{
		lsr.setIsCaseSensitive(aIsCaseSensitive);
	}

    @Override
    public Entity getMostFrequentEntity(String lexeme)
        throws LexicalSemanticResourceException
    {
        return lsr.getMostFrequentEntity(lexeme);
    }

    @Override
    public Entity getMostFrequentEntity(String lexeme, PoS pos)
        throws LexicalSemanticResourceException
    {
        return lsr.getMostFrequentEntity(lexeme, pos);
    }
}
