/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
