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
package de.tudarmstadt.ukp.dkpro.lexsemresource;

import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;

/**
 * An interface for lexical semantic resource.
 * Algorithms working on lexical semantic resources (e.g. semantic relatedness measures) should be programmed against the interface.
 * This makes them applicable to any lexical semantic resource that implements this interface.
 * @author zesch
 *
 */
public interface LexicalSemanticResource {

    /**
     * Supported lexical relation types.
     */
    public enum LexicalRelation {
        antonymy,
        synonymy
    }

    /**
     * Supported semantic relation types.
     */
    public enum SemanticRelation {
        holonymy,
        hypernymy,
        hyponymy,
        meronymy,
        cohyponymy,
        other           // TODO add this in all resources
    }

    /**
     * @return The name of the resource (e.g. WordNet, or Wiktionary).
     */
    public String getResourceName();
    /**
     * @return The version of the resource (e.g. 3.0, or 2008-01-04).
     */
    public String getResourceVersion();

    /**
     * @param lexeme A lexeme.
     * @return True if there is an entity that contains the lexeme, false otherwise.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public boolean containsLexeme(String lexeme) throws LexicalSemanticResourceException;

    /**
     * @param entity An entity.
     * @return True if the resource contains the entity, false otherwise.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public boolean containsEntity(Entity entity) throws LexicalSemanticResourceException;

    /**
     * @param lexeme A lexeme.
     * @return The set of entities which contain the lexeme.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Set<Entity> getEntity(String lexeme) throws LexicalSemanticResourceException;
    /**
     * @param lexeme A lexeme.
     * @param pos The part-of-speech of the lexeme.
     * @return The set of entities which contain a lexeme with the given pos.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Set<Entity> getEntity(String lexeme, PoS pos) throws LexicalSemanticResourceException;

    /**
     * This returns a set of entities, as a entity might not be unambiguously identified by a lexeme/pos/sense combiniation.
     * For example, in GermaNet, each lexeme has its own sense number.
     * Auto/n/1 may map to the entity "Auto#1 Fahrzeug#1 ---n" as well as to "Auto#1 Fahrzeug#2 ---n".
     * Thus, this method has to return a set of entities instead of a single entity.
     *
     * @param lexeme A lexeme.
     * @param pos The part-of-speech of the lexeme.
     * @param sense The sense of the lexeme.
     * @return The set of entities which contain a lexeme with the given pos and sense.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Set<Entity> getEntity(String lexeme, PoS pos, String sense) throws LexicalSemanticResourceException;

    /**
     * In contrast to the other getEntity() methods, this returns just a single entity instead of a set, as an entity is fully disambiguated by a mapping from lexemes to senses and a part-of-speech tag.
     *
     * @param lexemes A map with lexemes as keys and senses as values.
     * @param pos The part-of-speech of that entity.
     * @return An entity with the given lexemes, senses, and pos if there is such an entity in the resource. Null otherwise.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Entity getEntity(Map<String,String> lexemes, PoS pos) throws LexicalSemanticResourceException;

    /**
     * Return the entity which represents the most frequent sense of that lexeme. E.g. for WordNet this will be the first in the list, as no real frequency information is encoded.
     *
     * @param lexeme A lexeme.
     * @return The entity which represents the most frequent sense of that lexeme.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Entity getMostFrequentEntity(String lexeme) throws LexicalSemanticResourceException;

    /**
     * Return the entity which represents the most frequent sense of that lexeme. E.g. for WordNet this will be the first in the list, as no real frequency information is encoded.
     *
     * @param lexeme A lexeme.
     * @param pos The part-of-speech of that entity.
     * @return The entity which represents the most frequent sense of that lexeme.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Entity getMostFrequentEntity(String lexeme, PoS pos) throws LexicalSemanticResourceException;

    /**
     * In contrast to the other getEntity() methods, this returns just a single entity instead of a set, as an entity is fully disambiguated by its entity id.
     *
     * @param id The id of the entity as returned by a Entity object.
     * @return An entity with the given id. Null otherwise.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Entity getEntityById(String id) throws LexicalSemanticResourceException;

    /**
     * @param entity An entity.
     * @return The gloss of the entity.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public String getGloss(Entity entity) throws LexicalSemanticResourceException;
    /**
     * A pseudo gloss of an entity is build from concepts that are in a certain lexical or semantic relation (as determined by the corresponding parameter) to the given entity.
     * Pseudo glosses are based on the observation that an entity's gloss typically consists of terms from entities that are in close relation to the original entity.
     * Pseudo glosses can be used as a fallback for resources that do not have glosses and thus do not implement the getGloss() method.
     *
     * @param entity An entity.
     * @param lexicalRelations A set with the lexical relations. We do not need a map as with semantic relations, as lexical relations are not transitively defined (?"the synonym of the synonym").
     * @param semanticRelations A map with a semantic relation as key and the depth to which entities of that relation should be used.
     * @return The pseudo gloss of the entity.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public String getPseudoGloss(Entity entity, Set<LexicalRelation> lexicalRelations, Map<SemanticRelation,Integer> semanticRelations) throws LexicalSemanticResourceException;

    /**
     * @return The number of entities in the resource.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public int getNumberOfEntities() throws LexicalSemanticResourceException;

    /**
     * @return An iterable over all entities in the resource.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Iterable<Entity> getEntities() throws LexicalSemanticResourceException;

    /**
     * @param entity An entity.
     * @return A set containing all neighbors (i.e., parents and children) of the entity.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Set<Entity> getNeighbors(Entity entity) throws LexicalSemanticResourceException;

    /**
     * Returns the parents of an entity.  The meaning of "parents" depends on the
     * underlying lexical semantic resource:
     * <ul>
     * <li>For WordNet, GermaNet, Wiktionary, and OpenThesaurus, the parents are the hypernyms.</li>
     * <li>For Wikipedia articles, the parents are the articles which link to the entity.</li>
     * <li>For Wikipedia categories, the parents are the parent categories.</li>
     * </ul>
     *
     * @param entity An entity.
     * @return A set containing all parents of the entity.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Set<Entity> getParents(Entity entity) throws LexicalSemanticResourceException;

    /**
     * Returns the children of an entity.  The meaning of "children" depends on the
     * underlying lexical semantic resource:
     * <ul>
     * <li>For WordNet, GermaNet, Wiktionary, and OpenThesaurus, the children are the hyponyms.</li>
     * <li>For Wikipedia articles, the children are the articles which this entity links to.</li>
     * <li>For Wikipedia categories, the children are the child categories.</li>
     * </ul>
     *
     * @param entity An entity.
     * @return A set containing all children of the entity.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Set<Entity> getChildren(Entity entity) throws LexicalSemanticResourceException;

    /**
     * @return The root entity of the resource or null, if the resource contains no explicit taxonomy with a root.
     * @throws LexicalSemanticResourceException
     */
    public Entity getRoot() throws LexicalSemanticResourceException;

    /**
     * Some resources have distinct roots for e.g. the taxonomy of nouns or the taxonomy of adjectives.
     *
     * @param pos The PoS for which the root should be returned.
     * @return The root entity of the resource or null, if the resource contains no explicit taxonomy with a root.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Entity getRoot(PoS pos) throws LexicalSemanticResourceException;

    /**
     * @param lexeme A lexeme.
     * @param pos The part-of-speech of the given lexeme.
     * @param sense The sense of the given lexeme.
     * @param lexicalRelation A lexical relation.
     * @return All lexemes that are directly other to the given lexeme via the specified relation.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Set<String> getRelatedLexemes(String lexeme, PoS pos, String sense, LexicalRelation lexicalRelation) throws LexicalSemanticResourceException;
    /**
     * @param entity An entity.
     * @param semanticRelation A semantic relation.
     * @return All entities that are directly other to the given entity via the specified relation.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public Set<Entity> getRelatedEntities(Entity entity, SemanticRelation semanticRelation) throws LexicalSemanticResourceException;

    /**
     * Only lexical semantic resources that implement sophisticated mechanisms for computing the shortest path between two concepts (e.g. JWPL for Wikipedia) should implement this directly.
     * Others should use the means provided by EntityGraph.
     * @param e1 The name of the first concept.
     * @param e2 The name of the second concept.
     * @return The shortest distance between the concepts in the lexical semantic resource.
     * @throws LexicalSemanticResourceException
     */
// TODO what does it return if no path is found?
    public int getShortestPathLength(Entity e1, Entity e2) throws LexicalSemanticResourceException;


    /**
     * Sets whether the resource should be case sensitive, or not.
     * @param isCaseSensitive True, if the resource should be case sensitive, false otherwise.
     * @throws UnsupportedOperationException
     */
    public void setIsCaseSensitive(boolean isCaseSensitive);

    /**
     * @return Returns whether the resource is case sensitive.
     * @throws UnsupportedOperationException
     */
    public boolean getIsCaseSensitive();

}