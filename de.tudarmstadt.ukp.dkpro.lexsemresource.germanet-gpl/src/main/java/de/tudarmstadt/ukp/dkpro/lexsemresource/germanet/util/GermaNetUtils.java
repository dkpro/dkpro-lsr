/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.LexUnit;
import de.tuebingen.uni.sfs.germanet.api.Synset;
import de.tuebingen.uni.sfs.germanet.api.WordCategory;

public class GermaNetUtils {

    /**
     * @param synsets A set of GermaNet synsets.
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
        return new Entity(getSynsetLexemes(synset), GermaNetUtils.mapPos(synset.getWordCategory()));
    }

    public static Set<Synset> entityToSynsets(GermaNet gn, Entity entity) {
        Set<Synset> synsets = new HashSet<Synset>();

        Set<String> lexemes = entity.getLexemes();
        PoS lsrPos = entity.getPos();

        if (lsrPos.equals(Entity.UNKNOWN_POS)) {
            // get the synsets for each lexeme
            for (String lexeme : lexemes) {
                synsets.addAll( gn.getSynsets(lexeme) );
            }
        }
        else {
            // get the synsets for each lexeme
            for (String lexeme : lexemes) {
                synsets.addAll( gn.getSynsets(lexeme, GermaNetUtils.mapPos(lsrPos)) );
            }
        }
        return synsets;
    }

    public static Map<String,String> getSynsetLexemes(Synset synset) {
        Map<String,String> result = new HashMap<String,String>();

        if (synset == null) {
            return null;
        }

        List<LexUnit> lexUnits= synset.getLexUnits();
        for (LexUnit lexUnit : lexUnits) {
            result.put(lexUnit.getOrthForm(), Integer.toString(lexUnit.getSense()));
        }
        return result;
    }

    public static PoS mapPos(WordCategory cat) {
        if (cat.equals(WordCategory.nomen)) {
            return PoS.n;
        }
        else if (cat.equals(WordCategory.verben)) {
            return PoS.v;
        }
        else if (cat.equals(WordCategory.adj)) {
            return PoS.adj;
        }
        else {
            return PoS.unk;
        }
    }

    public static WordCategory mapPos(PoS lsrPos) {
        if (lsrPos.equals(PoS.n)) {
            return WordCategory.nomen;
        }
        else if (lsrPos.equals(PoS.v)) {
            return WordCategory.verben;
        }
        else if (lsrPos.equals(PoS.adj) || lsrPos.equals(PoS.adv)) {
            return WordCategory.adj;
        }
        else {
            return WordCategory.nomen;
        }
    }

}