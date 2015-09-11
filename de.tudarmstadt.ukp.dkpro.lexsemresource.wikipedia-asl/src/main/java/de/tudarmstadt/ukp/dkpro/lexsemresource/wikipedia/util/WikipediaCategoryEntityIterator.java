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
