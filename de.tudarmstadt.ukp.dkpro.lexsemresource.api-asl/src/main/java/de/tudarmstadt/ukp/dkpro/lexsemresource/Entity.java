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
package de.tudarmstadt.ukp.dkpro.lexsemresource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Represents a concept in a lexical semantic resource,
 * e.g. WordNet synsets, Wikipedia categories, or Wiktionary entries (not necessarily articles).
 *
 * An entity consists of a set of lexemes where each lexeme has a certain (possibly undefined) sense.
 * All lexemes of an entity share a (possible undefined) part-of-speech.
 *
 * An entity obviously resembles a synset, as it is the most complex representation of lexical semantic entities
 * observed among all currently implemented resources.
 *
 * @author zesch
 *
 */
public class Entity implements Comparable<Entity>, Serializable {

    /**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	public enum PoS {
		// TODO consider adding more parts of speech
        n,v,adj,adv,unk
    }

    public static final PoS UNKNOWN_POS = PoS.unk;
    public static final String UNKNOWN_SENSE = "-";

    // separates the senses from the lexeme in the output
    public static final String senseSeparator = "#";
    // separates multiple concepts (lexemes + senses) in the output
    // Attention: this is a special character in regular expression, thus it needs to be escaped when splitting the id
    public static final String conceptSeparator = "|";
    // separates POSs in the output
    public static final String posSeparator = "---";


    private Map<String,String> lexemes;
    private PoS pos;

    public Entity(String lexeme) {
        Map<String,String> lexemes = new TreeMap<String,String>();
        lexemes.put(lexeme, UNKNOWN_SENSE);
        initializeEntity(lexemes, UNKNOWN_POS);
    }

    public Entity(String lexeme, PoS pos) {
        Map<String,String> lexemes = new TreeMap<String,String>();
        lexemes.put(lexeme, UNKNOWN_SENSE);
        initializeEntity(lexemes, pos);
    }

    public Entity(String lexeme, PoS pos, String sense) {
        Map<String,String> lexemes = new TreeMap<String,String>();
        lexemes.put(lexeme, sense);
        initializeEntity(lexemes, pos);
    }

    public Entity(Map<String,String> lexemes) {
        initializeEntity(lexemes, UNKNOWN_POS);
    }

    public Entity(Map<String,String> lexemes, PoS pos) {
        initializeEntity(lexemes, pos);
    }

    private void initializeEntity(Map<String,String> lexemes, PoS pos) {
        this.lexemes = new TreeMap<String,String>();
        this.lexemes.putAll(lexemes);
        this.pos = pos;
    }

    /**
     * @return An id of this entity created as a concatenation of members.
     */
    public String getId() {
        StringBuilder id = new StringBuilder();
        for (String lexeme : lexemes.keySet()) {
            id.append(lexeme);
            id.append(senseSeparator);
            id.append(lexemes.get(lexeme));
            id.append(conceptSeparator);
        }
        id.append(posSeparator);
        id.append(pos);
        return id.toString();
    }

    /**
     * This method mainly functions as a shortcut for LSRs which only store one lexeme per Entity.
     * @return The first lexeme in the ordered set.
     */
    public String getFirstLexeme() {
        if (lexemes.size() > 0) {
            return lexemes.keySet().iterator().next();
        }
        else {
            return null;
        }
    }

    public Set<String> getLexemes() {
        return lexemes.keySet();
    }

    public PoS getPos() {
        return pos;
    }

    public String getSense(String lexeme) {
        return lexemes.get(lexeme);
    }

    @Override
    public String toString() {
        return this.getId();
    }

    @Override
    public int compareTo(Entity arg0) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this == arg0) {
            return EQUAL;
        }
        if (this.lexemes.equals(arg0.lexemes) && this.pos == arg0.pos) {
            return EQUAL;
        }

        if (this.lexemes == null && arg0.lexemes == null) {
            return EQUAL;
        }
        else if (this.lexemes != null && arg0.lexemes == null) {
            return AFTER;
        }
        else if (this.lexemes == null && arg0.lexemes != null) {
            return BEFORE;
        }
        else if (this.lexemes.size() > arg0.lexemes.size()) {
            return AFTER;
        }
        else if (this.lexemes.size() < arg0.lexemes.size()) {
            return BEFORE;
        }

        return BEFORE;
    }

    public int compareToCaseInsensitive(Entity arg0) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this == arg0) {
            return EQUAL;
        }

        Map<String,String> thisLex = lowerCaseMap(this.lexemes);
        Map<String,String> argLex = lowerCaseMap(arg0.lexemes);

        if (thisLex == null && argLex == null) {
            return EQUAL;
        }
        else if (thisLex != null && argLex == null) {
            return AFTER;
        }
        else if (thisLex == null && argLex != null) {
            return BEFORE;
        }
        else if (thisLex.equals(argLex) && this.pos == arg0.pos) {
            return EQUAL;
        }
        else if (thisLex.size() > argLex.size()) {
            return AFTER;
        }
        else if (thisLex.size() < argLex.size()) {
            return BEFORE;
        }

        return BEFORE;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((lexemes == null) ? 0 : lexemes.hashCode());
        result = PRIME * result + ((pos == null) ? 0 : pos.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Entity other = (Entity) obj;
        if (lexemes == null) {
            if (other.lexemes != null) {
                return false;
            }
        } else if (!lexemes.equals(other.lexemes)) {
            return false;
        }
        if (pos == null) {
            if (other.pos != null) {
                return false;
            }
        } else if (!pos.equals(other.pos)) {
            return false;
        }
        return true;
    }

    private Map<String,String> lowerCaseMap(Map<String,String> map) {
        Map<String,String> lowerCaseMap = new HashMap<String,String>();
        for (String key : map.keySet()) {
            lowerCaseMap.put(key.toLowerCase(), map.get(key));
        }
        return lowerCaseMap;
    }
}

