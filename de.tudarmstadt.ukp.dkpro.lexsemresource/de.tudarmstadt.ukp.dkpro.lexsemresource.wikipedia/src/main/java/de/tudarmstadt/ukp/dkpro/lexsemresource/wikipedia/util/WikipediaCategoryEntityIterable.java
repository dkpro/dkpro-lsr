package de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util;

import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

public class WikipediaCategoryEntityIterable extends LexicalSemanticEntityIterable {

    Iterable<Category> wikiCatIterable;
    
    public WikipediaCategoryEntityIterable(Wikipedia wiki) {
        wikiCatIterable = wiki.getCategories();
    }
    
    @Override
    public Iterator<Entity> iterator() {
        return new WikipediaCategoryEntityIterator(wikiCatIterable);
    }

}
