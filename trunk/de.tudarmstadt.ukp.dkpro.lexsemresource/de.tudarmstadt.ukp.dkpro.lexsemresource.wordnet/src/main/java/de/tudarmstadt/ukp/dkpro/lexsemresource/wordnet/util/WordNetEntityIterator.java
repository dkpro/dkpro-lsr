package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import java.util.Iterator;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterator;

public class WordNetEntityIterator extends LexicalSemanticEntityIterator {

    Iterator adjIter;
    Iterator advIter;
    Iterator nounIter;
    Iterator verbIter;
    
    public WordNetEntityIterator(Dictionary dict) {
        try {
            this.adjIter = dict.getSynsetIterator(POS.ADJECTIVE);
            this.advIter  = dict.getSynsetIterator(POS.ADVERB);
            this.nounIter = dict.getSynsetIterator(POS.NOUN);
            this.verbIter = dict.getSynsetIterator(POS.VERB);
        } catch (JWNLException e) {
            e.printStackTrace();
        }
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
            return WordNetUtils.synsetToEntity((Synset) adjIter.next());
        }
        else if (advIter.hasNext()) {
            return WordNetUtils.synsetToEntity((Synset) advIter.next());
        }
        else if (nounIter.hasNext()) {
            return WordNetUtils.synsetToEntity((Synset) nounIter.next());
        }
        else if (verbIter.hasNext()) {
            return WordNetUtils.synsetToEntity((Synset) verbIter.next());
        }
        else {
            return null;
        }
    }
}
