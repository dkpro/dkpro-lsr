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
package de.tudarmstadt.ukp.dkpro.lexsemresource.openthesaurus.util;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterator;
import de.tudarmstadt.ukp.openthesaurus.api.Synset;
import de.tudarmstadt.ukp.openthesaurus.api.Term;
import de.tudarmstadt.ukp.openthesaurus.exception.OpenThesaurusException;
/**
 * 
 * @author chebotar
 *
 */
public class OpenThesaurusEntityIterator extends LexicalSemanticEntityIterator {
	private Iterator<Synset> synsets;
	
	public OpenThesaurusEntityIterator(Iterator<Synset> synsets){
		this.synsets = synsets;
	}
	
	@Override
	public boolean hasNext() {		
		return synsets.hasNext();
	}

	@Override
	public Entity next() {		
		Synset syn = synsets.next();
		Map<String, String> terms = new TreeMap<String, String>();
		try{
			for(Term t : syn.getTerms()){
				terms.put(t.getWord(), String.valueOf(t.getTermId()));
			}
			
		}catch (OpenThesaurusException ex){
			ex.printStackTrace();
		}
		
		return new Entity(terms);
		
	}

}
