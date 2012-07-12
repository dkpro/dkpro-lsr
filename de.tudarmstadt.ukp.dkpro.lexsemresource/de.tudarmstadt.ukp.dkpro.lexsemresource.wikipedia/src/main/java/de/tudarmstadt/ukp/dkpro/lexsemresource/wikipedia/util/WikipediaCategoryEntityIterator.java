package de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util;

import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterator;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class WikipediaCategoryEntityIterator extends LexicalSemanticEntityIterator {

    private Iterator<Category> wikiCatIterator; 
    
    public WikipediaCategoryEntityIterator(Iterable<Category> wikiCatIterable) {
        wikiCatIterator = wikiCatIterable.iterator();
    }
    
    @Override
    public boolean hasNext() {
        return wikiCatIterator.hasNext();
    }

    @Override
    public Entity next() {
        Category cat = wikiCatIterator.next();
        String title = "";
        try {
            title = cat.getTitle().getWikiStyleTitle();
        } catch (WikiTitleParsingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return new Entity(title);
    }
}
