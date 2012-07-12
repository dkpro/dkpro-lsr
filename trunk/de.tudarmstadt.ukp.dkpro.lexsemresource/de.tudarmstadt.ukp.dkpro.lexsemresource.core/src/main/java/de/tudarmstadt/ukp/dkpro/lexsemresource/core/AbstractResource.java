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
package de.tudarmstadt.ukp.dkpro.lexsemresource.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;

public abstract class AbstractResource implements LexicalSemanticResource {

	protected boolean isCaseSensitive = false;

    public Entity getEntity(Map<String, String> lexemes, PoS pos) throws LexicalSemanticResourceException {
        if (lexemes == null) {
            return null;
        }
        Entity e = new Entity(lexemes, pos);
        // querying each of the lexemes should give the correct answer set
        Set<Entity> possibleEntities = getEntity(lexemes.keySet().iterator().next(), pos);
        for (Entity possibleEntity : possibleEntities) {
            // return the entity if there is a real entity that matches the possible entity
            if (e.compareTo(possibleEntity) == 0) {
                return e;
            }
        }
        return null;
    }

    public Entity getEntityById(String id) throws LexicalSemanticResourceException {
        String splitter = "\\" + Entity.conceptSeparator + Entity.posSeparator;
        String[] conceptsPosParts = id.split(splitter);
        if (conceptsPosParts.length != 2) {
            throw new LexicalSemanticResourceException("Invalid id: " + id);
        }

        String concepts = conceptsPosParts[0];
        String pos = conceptsPosParts[1];

        String[] lexemeSensePairs = concepts.split("\\" + Entity.conceptSeparator);

        Map<String, String> lexemes = new HashMap<String,String>();
        for (String lexemeSensePair : lexemeSensePairs) {
            String[] lexemeSenseSplit = lexemeSensePair.split(Entity.senseSeparator);
            if (lexemeSenseSplit.length != 2) {
                throw new LexicalSemanticResourceException("Invalid id: " + id);
            }

            lexemes.put(lexemeSenseSplit[0], lexemeSenseSplit[1]);
        }

        return getEntity(lexemes, PoS.valueOf(pos));
    }


    public Set<Entity> getNeighbors(Entity entity) throws LexicalSemanticResourceException  {
        Set<Entity> neighbors = new HashSet<Entity>();
        neighbors.addAll(getParents(entity));
        neighbors.addAll(getChildren(entity));
        return neighbors;
    }

    public String getPseudoGloss(Entity entity, Set<LexicalRelation> lexicalRelations, Map<SemanticRelation, Integer> semanticRelations) throws LexicalSemanticResourceException {
        Set<String> pseudoGloss = new HashSet<String>();

        for (LexicalRelation lexicalRelation : lexicalRelations) {
            for (String lexeme : entity.getLexemes()) {
                pseudoGloss.add(lexeme);
                pseudoGloss.addAll(this.getRelatedLexemes(lexeme, entity.getPos(), entity.getSense(lexeme), lexicalRelation));
            }
        }

        for (SemanticRelation semanticRelation : semanticRelations.keySet()) {
            pseudoGloss.addAll( getSemanticRelationsRecursive(pseudoGloss, semanticRelation, entity, semanticRelations.get(semanticRelation)) );
        }

        return StringUtils.join(pseudoGloss, " ").trim();
    }

    public void setIsCaseSensitive(boolean isCaseSensitive){
        this.isCaseSensitive = isCaseSensitive;
    }

    public boolean getIsCaseSensitive(){
        return this.isCaseSensitive;
    }

    /**
     * Recursively selects all lexemes from related entities up to the given depth.
     * @param rel semantic relation
     * @param e entity
     * @param depth The recursive depth
     * @return The set of string in the pseudo gloss.
     * @throws UnsupportedOperationException
     * @throws LexicalSemanticResourceException
     */
    private Set<String> getSemanticRelationsRecursive(Set<String> pseudoGloss, SemanticRelation semanticRelation, Entity entity, int depth) throws LexicalSemanticResourceException {

        for (Entity e : this.getRelatedEntities(entity, semanticRelation)) {
            pseudoGloss.addAll(e.getLexemes());
            if (depth > 1) {
                pseudoGloss = getSemanticRelationsRecursive(pseudoGloss, semanticRelation, e, depth-1);
            }
        }

        return pseudoGloss;
    }


}