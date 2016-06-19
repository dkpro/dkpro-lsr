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
package de.tudarmstadt.ukp.dkpro.lexsemresource.openthesaurus.util;

import java.util.Iterator;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;
import de.tudarmstadt.ukp.openthesaurus.api.OpenThesaurus;
import de.tudarmstadt.ukp.openthesaurus.api.Synset;
import de.tudarmstadt.ukp.openthesaurus.exception.OpenThesaurusException;

/**
 * 
 * @author chebotar
 *
 */
public class OpenThesaurusEntityIterable extends LexicalSemanticEntityIterable {
	
	private Set<Synset> synsets;
	
    public OpenThesaurusEntityIterable(OpenThesaurus openThesaurus) throws OpenThesaurusException{
		this.synsets = openThesaurus.getAllSynsets();		
	}
    
	@Override
	public Iterator<Entity> iterator() {
		return new OpenThesaurusEntityIterator(synsets.iterator());		
	}

}
