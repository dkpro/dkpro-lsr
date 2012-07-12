package de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util;

import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterator;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

public class WikipediaArticleEntityIterator extends LexicalSemanticEntityIterator {

    private Iterator<Page> wikiPageIterator;
    private Wikipedia wiki;
    private Page nextPage;
    private boolean nextIsValid = false;
    private boolean isCaseSensitive;
    
    public WikipediaArticleEntityIterator(Wikipedia wiki, Iterable<Page> wikiPageIterable, boolean isCaseSensitive) {
        this.wikiPageIterator = wikiPageIterable.iterator();
        this.wiki = wiki;
        this.isCaseSensitive = isCaseSensitive;
    }
    
    @Override
    public boolean hasNext() {
        // if we have already called hasNext and updated nextPage, hasNext() should still return true but not update nextPage again 
        if (nextIsValid) {
            return true;
        }
        
        while (wikiPageIterator.hasNext()) {
            Page item = wikiPageIterator.next();
            if (item.isRedirect()) {
                continue;
            }
            else {
                nextPage = item;
                nextIsValid = true;
                return true;
            }
        }
        
        // if while loop end, then the iterator has no more elements => return false
        return false;
    }

    @Override
    public Entity next() {
        nextIsValid = false;
        try {
            return WikipediaArticleUtils.pageToEntity(wiki, nextPage, isCaseSensitive);
        } catch (LexicalSemanticResourceException e1) {
            e1.printStackTrace();
            return null;
        } 
    }
}
