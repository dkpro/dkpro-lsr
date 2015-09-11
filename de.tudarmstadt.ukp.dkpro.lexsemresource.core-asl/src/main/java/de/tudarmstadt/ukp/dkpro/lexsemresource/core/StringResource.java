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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;

/**
 * For reasons of uniform treatment of all relatedness comparators, we need a StringResource that backs up all String based comparators.
 * @author zesch
 *
 */
public class StringResource extends AbstractResource {

    protected Log logger = LogFactory.getLog(getClass());

    private static final String resourceName = "StringResource";
    private static final String resourceVersion = "1.0";

    public boolean containsEntity(Entity entity) throws LexicalSemanticResourceException {
        return true;
    }

    public boolean containsLexeme(String lexeme) throws LexicalSemanticResourceException {
        return true;
    }

    public Set<Entity> getEntity(String lexeme) throws LexicalSemanticResourceException {
        Entity e = new Entity(lexeme);
        Set<Entity> eSet = new HashSet<Entity>();
        eSet.add(e);
        return eSet;
    }

    public Set<Entity> getEntity(String lexeme, PoS pos) throws LexicalSemanticResourceException {
        return getEntity(lexeme);
    }

    public Set<Entity> getEntity(String lexeme, PoS pos, String sense) throws LexicalSemanticResourceException {
        return getEntity(lexeme);
    }

    @Override
	public Entity getEntity(Map<String, String> lexemes, PoS pos) throws LexicalSemanticResourceException {
        return new Entity(lexemes.keySet().iterator().next());
    }

    public Set<Entity> getParents(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
	public Set<Entity> getNeighbors(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public int getNumberOfEntities() {
        return Integer.MAX_VALUE;
    }

    public Iterable<Entity> getEntities() {
        throw new UnsupportedOperationException();
    }

    public Set<Entity> getChildren(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public Set<String> getRelatedLexemes(String lexeme, PoS pos, String sense, LexicalRelation lexicalRelation) {
        throw new UnsupportedOperationException();
    }

    public int getShortestPathLength(Entity e1, Entity e2) {
        throw new UnsupportedOperationException();
    }

    public String getGloss(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public Set<Entity> getRelatedEntities(Entity entity, SemanticRelation semanticRelation) {
        throw new UnsupportedOperationException();
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
