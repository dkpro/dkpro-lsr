package de.tudarmstadt.ukp.dkpro.lexsemresource.wiktionary.util;

import java.util.Iterator;

import com.sleepycat.je.DatabaseException;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;
import de.tudarmstadt.ukp.wiktionary.api.Wiktionary;
import de.tudarmstadt.ukp.wiktionary.api.WordEntry;

public class WiktionaryEntityIterable extends LexicalSemanticEntityIterable {

    private Iterator<WordEntry> wordEntryIterator;
    
    public WiktionaryEntityIterable(Wiktionary wkt) throws DatabaseException {
        wordEntryIterator = wkt.wordEntryIterator();
    }
    
    @Override
    public Iterator<Entity> iterator() {
        return new WiktionaryEntityIterator(wordEntryIterator);
    }

}
