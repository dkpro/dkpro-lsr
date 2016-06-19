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
package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;

public class WordNetUtils
{
    /**
     * @param synsets A set of WordNet synsets.
     * @return Creates a set of Entities from a set of synsets.
     */
	public static Set<Entity> synsetsToEntities(Set<Synset> synsets)
	{
		Set<Entity> entities = new LinkedHashSet<Entity>();
		for (Synset synset : synsets) {
			entities.add(synsetToEntity(synset));
		}
		return entities;
	}

    /**
     * @param synset A WordNet synset.
     * @return Creates an Entity from a synset.
     */
	public static Entity synsetToEntity(Synset synset)
	{
		if (synset == null) {
			return null;
		}

		return new Entity(getSynsetLexemes(synset), WordNetUtils.mapPos(synset.getPOS()));
	}

	public static Map<String, String> getSynsetLexemes(Synset synset)
	{
		Map<String, String> result = new HashMap<String, String>();

		long sense = synset.getOffset();

		List<Word> words = synset.getWords();
		for (Word word : words) {
			String lexeme = word.getLemma();
			// remove some suffixes that might be added to the synset representation by JWNL
			lexeme = cleanLexeme(lexeme);
			result.put(lexeme, new Long(sense).toString());
		}
		return result;
	}

	// returns lexsemresource PoS for a JWNL POS
	public static PoS mapPos(POS pos)
	{
		if (pos.equals(POS.NOUN)) {
			return PoS.n;
		}
		else if (pos.equals(POS.VERB)) {
			return PoS.v;
		}
		else if (pos.equals(POS.ADJECTIVE)) {
			return PoS.adj;
		}
		else if (pos.equals(POS.ADVERB)) {
			return PoS.adv;
		}
		else {
			return PoS.unk;
		}
	}

	// returns JWNL POS for a lexsemresource PoS
	public static POS getJwnlPos(PoS lsrPos)
	{

		if (lsrPos.equals(PoS.n)) {
			return POS.NOUN;
		}
		else if (lsrPos.equals(PoS.v)) {
			return POS.VERB;
		}
		else if (lsrPos.equals(PoS.adj)) {
			return POS.ADJECTIVE;
		}
		else if (lsrPos.equals(PoS.adv)) {
			return POS.ADVERB;
		}
		else {
			// defaults to noun
			return POS.NOUN;
		}
	}

	// stays just for backwards compatibility
	// this mapping always gives a single POS, so use getJwnlPos(PoS lsrPos)
	public static Set<POS> mapPos(PoS lsrPos)
	{
		Set<POS> pos = new LinkedHashSet<POS>();
		if (lsrPos.equals(PoS.n)) {
			pos.add(POS.NOUN);
		}
		else if (lsrPos.equals(PoS.v)) {
			pos.add(POS.VERB);
		}
		else if (lsrPos.equals(PoS.adj)) {
			pos.add(POS.ADJECTIVE);
		}
		else if (lsrPos.equals(PoS.adv)) {
			pos.add(POS.ADVERB);
		}
		else {
			// defaults to noun
			pos.add(POS.NOUN);
		}
		return pos;
	}

	public static Set<Synset> toSynset(Dictionary dict, String lexeme, boolean isCaseSensitive)
		throws LexicalSemanticResourceException
	{
		Set<Synset> resultsSynsets = new LinkedHashSet<Synset>();

		for (PoS pos : PoS.values()) {
			resultsSynsets.addAll(toSynset(dict, lexeme, pos, isCaseSensitive));
		}

		return resultsSynsets;
	}

	public static Set<Synset> toSynset(Dictionary dict, String lexeme, PoS pos,
			boolean isCaseSensitive)
		throws LexicalSemanticResourceException
	{
		try {
			Set<Synset> resultsSynsets = new LinkedHashSet<Synset>();

			// removed loop iterating over mapped POSs
			POS gnPos = WordNetUtils.getJwnlPos(pos);
			IndexWord indexWord = dict.getIndexWord(gnPos, lexeme);

			// IndexWord indexWord = dict.lookupIndexWord(gnPos, lexeme);
			// lookupIndexWord is case independent and may try a stemmed form of lexeme

			// TODO How to do if lookupIndexWord function returns a stemmed form?
			// cannot only test with equal() as this fails if the lexeme contains a underscore _,
			// as e.g. in "automotive_vehicle" as the lemma is "automotive vehicle"
			if (
			// No such lexeme in WN
			indexWord == null ||
			// there is a case mismatch
					(isCaseSensitive && !areEqualLexemes(indexWord.getLemma(), lexeme))) {
				return resultsSynsets;
			}

			List<Synset> synsets = indexWord.getSenses();
			for (Synset synset : synsets) {
				resultsSynsets.add(synset);
			}

			return resultsSynsets;
		}
		catch (JWNLException e) {
			throw new LexicalSemanticResourceException(e);
		}
	}

	public static Synset toSynset(Dictionary dict, String lexeme, PoS pos, int sense,
			boolean isCaseSensitive)
		throws LexicalSemanticResourceException
	{
		try {
			POS gnPos = WordNetUtils.getJwnlPos(pos);
			IndexWord indexWord = dict.getIndexWord(gnPos, lexeme);
			// IndexWord indexWord = dict.lookupIndexWord(gnPos, lexeme);
			// lookupIndexWord is case independent and may try a stemmed form of lexeme

			// TODO How to do if lookupIndexWord function returns a stemmed form?
			// cannot only test with equal() as this fails if the lexeme contains a underscore _,
			// as e.g. in "automotive_vehicle" as the lemma is "automotive vehicle"
			if (
			// No such lexeme in WN
			indexWord == null ||
			// there is a case mismatch
					(isCaseSensitive && !areEqualLexemes(indexWord.getLemma(), lexeme))) {
				return null;
			}

			List<Synset> synsets = indexWord.getSenses();

			// the given lexeme doesn't have this many senses
			if (sense >= synsets.size()) {
				return null;
			}

			// sense IDs are normally numbered from 1 ... ǹum_senses
			Synset synset = synsets.get(sense - 1);
			return synset;

		}
		catch (JWNLException e) {
			throw new LexicalSemanticResourceException(e);
		}
	}

    public static Synset toSynset(Dictionary dict, POS pos, int synsetOffset)
        throws LexicalSemanticResourceException
    {
        try {
            return dict.getSynsetAt(pos, 1025455);
        }
        catch (JWNLException e) {
            throw new LexicalSemanticResourceException(e);
        }
    }

    // This is left to work with offsets, to enable backwards compatibility. GS
	public static Synset toSynset(Dictionary dict, String lexeme, PoS pos, String sense,
			boolean isCaseSensitive)
		throws LexicalSemanticResourceException
	{
		Set<Synset> possibleSynsets = toSynset(dict, lexeme, pos, isCaseSensitive);
		for (Synset synset : possibleSynsets) {
			Long senseNumber = synset.getOffset();

			// if the sense number matches, then we have found the correct synset
			if (senseNumber.toString().equals(sense)) {
				return synset;
			}
		}

		// if we get here, there is no matching synset
		return null;
	}

	public static Set<Synset> entityToSynsets(Dictionary dict, Entity entity,
			boolean isCaseSensitive)
		throws LexicalSemanticResourceException
	{
		Set<Synset> results = new LinkedHashSet<Synset>();

		Set<String> lexemes = entity.getLexemes();
		PoS pos = entity.getPos();
		String sense = entity.getSense(entity.getFirstLexeme());
		Long senseNumber = Long.MIN_VALUE;
		if (sense != Entity.UNKNOWN_SENSE) {
			try {
				senseNumber = new Long(sense);
			}
			catch (NumberFormatException e) {
				throw new LexicalSemanticResourceException(e);
			}
		}

		// we can directly get the synset - a WordNet is synset is disambiguated by a POS and a
		// sense number
		if (senseNumber != Long.MIN_VALUE && pos != Entity.UNKNOWN_POS) {
			// it does not matter which lexeme, so simply take the first
			Synset synset = WordNetUtils.toSynset(dict, lexemes.iterator().next(), pos, sense,
					isCaseSensitive);
			if (synset != null) {
				results.add(synset);
			}
		}
		else if (pos.equals(Entity.UNKNOWN_POS)) {
			// get the synsets for each lexeme
			for (String lexeme : lexemes) {
				Set<Synset> synsets = WordNetUtils.toSynset(dict, lexeme, isCaseSensitive);
				if (synsets != null) {
					results.addAll(synsets);
				}
			}
		}
		else {
			// get the synsets for each lexeme
			for (String lexeme : lexemes) {
				Set<Synset> synsets = WordNetUtils.toSynset(dict, lexeme, pos, isCaseSensitive);
				if (synsets != null) {
					results.addAll(synsets);
				}
			}
		}
		return results;
	}

	// a wordnet synset is unambigously indicated by a pos and a sense number
	public static Entity getExactEntity(Dictionary dict, String lexeme, PoS pos, int sense,
			boolean isCaseSensitive)
		throws LexicalSemanticResourceException
	{
		return synsetToEntity(toSynset(dict, lexeme, pos, sense, isCaseSensitive));
	}

    public static Entity getExactEntity(Dictionary dict, POS pos, int synsetOffset)
        throws LexicalSemanticResourceException
    {
        return synsetToEntity(toSynset(dict, pos, synsetOffset));
    }

    // a wordnet synset is unambigously indicated by a pos and an offset
	// kept to preserve backwards compatibility
	public static Entity getExactEntity(Dictionary dict, String lexeme, PoS pos, String sense,
			boolean isCaseSensitive)
		throws LexicalSemanticResourceException
	{
		return synsetToEntity(toSynset(dict, lexeme, pos, sense, isCaseSensitive));
	}

	private static String cleanLexeme(String lexeme)
	{
		if (lexeme.endsWith("(n)") || lexeme.endsWith("(a)") || lexeme.endsWith("(v)")
				|| lexeme.endsWith("(p)")) {
			return lexeme.substring(0, lexeme.length() - 3);
		}
		else if (lexeme.endsWith("(ip)")) {
			return lexeme.substring(0, lexeme.length() - 4);
		}
		return lexeme;
	}

	/**
	 * Sometimes different API methods deliver different results, e.g. we can find a synset for a lexeme with underscores with one method, but not with the other.
	 * Thus, equal lexemes should be tested a little bit more thoroughly.
	 *
	 * @param l1
	 * @param l2
	 * @return
	 */
	private static boolean areEqualLexemes(String l1, String l2) {
	    if (l1.equals(l2)) {
	        return true;
	    }

	    // replace spaces with underscores.
	    String l1_relaxed = l1.replaceAll(" ", "_");
	    String l2_relaxed = l2.replaceAll(" ", "_");

	    if (l1_relaxed.equals(l2_relaxed)) {
	        return true;
	    }

	    return false;
	}

	public static Entity getMostFrequentEntity(
	        Dictionary dict,
	        String lexeme,
	        PoS pos,
	        boolean isCaseSensitive)
	    throws LexicalSemanticResourceException
	{

        POS gnPos = WordNetUtils.getJwnlPos(pos);
        IndexWord indexWord;
        try {
            indexWord = dict.getIndexWord(gnPos, lexeme);
        }
        catch (JWNLException e) {
            throw new LexicalSemanticResourceException(e);
        }

        // cannot only test with equal() as this fails if the lexeme contains a underscore _,
        // as e.g. in "automotive_vehicle" as the lemma is "automotive vehicle"
        // No such lexeme in WN || there is a case mismatch
        if (indexWord == null ||
           (isCaseSensitive && !areEqualLexemes(indexWord.getLemma(), lexeme)))
        {
            return null;
        }

        List<Synset> synsets;
        synsets = indexWord.getSenses();

        if (synsets.size() > 0) {
            return synsetToEntity(synsets.get(0));
        }

	    return null;
	}
}