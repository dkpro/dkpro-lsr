package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import java.util.Iterator;

import net.didion.jwnl.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;

public class WordNetEntityIterable extends LexicalSemanticEntityIterable {

    Dictionary dict;
    
    public WordNetEntityIterable(Dictionary dict) {
        this.dict = dict;
    }
    
    @Override
    public Iterator<Entity> iterator() {
        return new WordNetEntityIterator(dict);
    }

}
