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
package de.tudarmstadt.ukp.dkpro.lexsemresource.openthesaurus.util;

import java.util.Map;
import java.util.TreeMap;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.openthesaurus.api.OpenThesaurus;
import de.tudarmstadt.ukp.openthesaurus.api.Synset;
import de.tudarmstadt.ukp.openthesaurus.api.Term;
import de.tudarmstadt.ukp.openthesaurus.exception.OpenThesaurusException;

/**
 * 
 * @author chebotar
 *
 */
public class OpenThesaurusUtils {
	
	/**
	 * Converts Entity to OpenThesaurus Synset
	 * @param openThesaurus OpenThesaurus object
	 * @param entity Entity
	 * @return Synset
	 * @throws OpenThesaurusException
	 */
	public static Synset entityToSynset(OpenThesaurus openThesaurus, Entity entity)
				throws OpenThesaurusException{
		
		return openThesaurus.getSynsetByTermId(Integer.parseInt(
				entity.getSense(entity.getFirstLexeme())));	
	}
	
	/**
	 * Converts OpenThesaurus Synset to Entity
	 * @param openThesaurus OpenThesaurus objcet
	 * @param synset Synset
	 * @return Entity
	 * @throws OpenThesaurusException
	 */
	public static Entity synsetToEntity(OpenThesaurus openThesaurus, Synset synset) 
						throws OpenThesaurusException{
		
		Map<String, String> terms = new TreeMap<String, String>();
		for(Term t : synset.getTerms()){
			terms.put(t.getWord(), String.valueOf(t.getTermId()));
		}
		
		return new Entity(terms);
	}
	
	/**
	 * Finds OpenThesaurus Term with lexeme as word, sense as termId
	 * @param openThesaurus OpenThesaurus object
	 * @param lexeme Lexeme
	 * @param sense Sense
	 * @param isCaseSensitive Case Sensitivity Setting
	 * @return Term, if not found: null
	 * @throws OpenThesaurusException
	 */
	public static Term getTermBySense(OpenThesaurus openThesaurus, String lexeme, 
			String sense, boolean isCaseSensitive) throws OpenThesaurusException{
		
		Term term = openThesaurus.getTermById(Integer.parseInt(sense));
		
		if(term == null || (!isCaseSensitive && !term.getWord().equalsIgnoreCase(lexeme))
				|| (isCaseSensitive && !term.getWord().equals(lexeme)))				
			return null;
		else return term;
	}
}
