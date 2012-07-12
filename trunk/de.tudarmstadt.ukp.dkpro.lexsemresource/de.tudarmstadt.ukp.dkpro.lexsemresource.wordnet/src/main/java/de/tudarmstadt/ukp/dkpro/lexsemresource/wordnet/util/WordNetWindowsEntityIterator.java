package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterator;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Synset;

public class WordNetWindowsEntityIterator extends LexicalSemanticEntityIterator {
	
    Iterator adjIter;
    Iterator advIter;
    Iterator nounIter;
    Iterator verbIter;

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
