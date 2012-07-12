package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;
import edu.mit.jwi.Dictionary;

public class WordNetWindowsEntityIterable extends LexicalSemanticEntityIterable {

    Dictionary dict;
    
    public WordNetWindowsEntityIterable(Dictionary dict) {
        this.dict = dict;
    }
    
    @Override
    public Iterator<Entity> iterator() {
        return new WordNetWindowsEntityIterator(dict);
    }
	
}
