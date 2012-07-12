package de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util;

import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

public class WikipediaArticleEntityIterable extends LexicalSemanticEntityIterable {

    Iterable<Page> wikiPageIterable;
    Wikipedia wiki;
    private boolean isCaseSensitive;
    
    public WikipediaArticleEntityIterable(Wikipedia wiki, boolean isCaseSensitive) {
        this.wikiPageIterable = wiki.getArticles();
        this.wiki = wiki;
        this.isCaseSensitive = isCaseSensitive;
    }
    
    @Override
    public Iterator<Entity> iterator() {
        return new WikipediaArticleEntityIterator(wiki, wikiPageIterable, isCaseSensitive);
    }

}
