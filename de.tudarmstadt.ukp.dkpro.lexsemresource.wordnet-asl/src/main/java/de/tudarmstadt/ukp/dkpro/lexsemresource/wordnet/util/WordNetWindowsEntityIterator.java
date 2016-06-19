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

import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterator;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Synset;

public class WordNetWindowsEntityIterator extends LexicalSemanticEntityIterator {
	
    Iterator<ISynset> adjIter;
    Iterator<ISynset> advIter;
    Iterator<ISynset> nounIter;
    Iterator<ISynset> verbIter;

	public WordNetWindowsEntityIterator(Dictionary dict) {
		
		this.adjIter = dict.getSynsetIterator(POS.ADJECTIVE);
		this.advIter = dict.getSynsetIterator(POS.ADVERB);
		this.nounIter = dict.getSynsetIterator(POS.NOUN);
		this.verbIter = dict.getSynsetIterator(POS.VERB);
	}
	
	@Override
	public boolean hasNext() {        
		if (adjIter.hasNext() || advIter.hasNext() || nounIter.hasNext() || verbIter.hasNext()) {
			return true;
		}
		else {
			return false;
		}
	}

    @Override
    public Entity next() {
        if (adjIter.hasNext()) {
            return WordNetWindowsUtils.synsetToEntity((Synset) adjIter.next());
        }
        else if (advIter.hasNext()) {
            return WordNetWindowsUtils.synsetToEntity((Synset) advIter.next());
        }
        else if (nounIter.hasNext()) {
            return WordNetWindowsUtils.synsetToEntity((Synset) nounIter.next());
        }
        else if (verbIter.hasNext()) {
            return WordNetWindowsUtils.synsetToEntity((Synset) verbIter.next());
        }
        else {
            return null;
        }
    }

}
