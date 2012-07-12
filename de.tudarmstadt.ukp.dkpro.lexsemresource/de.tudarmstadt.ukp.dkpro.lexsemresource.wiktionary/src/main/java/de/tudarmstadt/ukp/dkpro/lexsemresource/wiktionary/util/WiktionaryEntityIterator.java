package de.tudarmstadt.ukp.dkpro.lexsemresource.wiktionary.util;

import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterator;
import de.tudarmstadt.ukp.wiktionary.api.WordEntry;

public class WiktionaryEntityIterator extends LexicalSemanticEntityIterator {

    private Iterator<WordEntry> keyIter; 
    
    public WiktionaryEntityIterator(Iterator<WordEntry> wordEntryIterator) {
        keyIter = wordEntryIterator;
    }
    
    @Override
    public boolean hasNext() {
        return keyIter.hasNext();
    }

    @Override
    public Entity next() {
        WordEntry nextWord = keyIter.next();
        return new Entity(nextWord.getWord(), WiktionaryUtils.mapPos(nextWord.getPartOfSpeech()));
    }
}
