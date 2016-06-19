/*******************************************************************************
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.AbstractResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util.WikipediaArticleEntityIterable;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util.WikipediaArticleUtils;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.MetaData;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.parser.Paragraph;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;

public class WikipediaArticleResource extends AbstractResource {

	private final Log logger = LogFactory.getLog(getClass());

    private static final String resourceName = "WikipediaAG";

    private Wikipedia wiki;
    private DatabaseConfiguration databaseConfiguration;
    private LexicalSemanticResource wikiCategoryResource;

    public WikipediaArticleResource(String host, String database, String user, String password, Language language) throws LexicalSemanticResourceException {
        this(new DatabaseConfiguration(host, database, user, password, language));
    }

    public WikipediaArticleResource(DatabaseConfiguration dbConfig) throws LexicalSemanticResourceException {
        try {
            wiki = new Wikipedia(dbConfig);
        } catch (WikiInitializationException e) {
            throw new LexicalSemanticResourceException("Wikipedia could not be initialized.",e);
        }

        initialSetup(dbConfig);
    }

    public WikipediaArticleResource(Wikipedia wiki) {
        this.wiki = wiki;

        initialSetup(wiki.getDatabaseConfiguration());
    }

    private void initialSetup(DatabaseConfiguration dbConfig) {
        setIsCaseSensitive(isCaseSensitive);    //set isCaseSensitive to the default value
        this.wikiCategoryResource = null;
        this.databaseConfiguration = dbConfig;
    }

    public boolean containsEntity(Entity entity) throws LexicalSemanticResourceException {
        return containsLexeme(entity.getFirstLexeme());
    }

    public boolean containsLexeme(String lexeme) throws LexicalSemanticResourceException {
      	lexeme = WikipediaArticleUtils.getCaseSensitiveLexeme(lexeme, isCaseSensitive);
       	try {
        	wiki.getPage(lexeme);
        } catch (WikiApiException e) {
            return false;
        }
        return true;
    }


    public Set<Entity> getEntity(String lexeme) throws LexicalSemanticResourceException {
        Set<Entity> entitySet = new HashSet<Entity>();
        if (!containsLexeme(lexeme)) {
            return entitySet;
        }
        Entity e = WikipediaArticleUtils.lexemeToEntity(wiki, lexeme, isCaseSensitive);
        if (e != null) {
            entitySet.add(e);
        }
        return entitySet;
    }

    public Set<Entity> getEntity(String lexeme, PoS pos) throws LexicalSemanticResourceException {
        return getEntity(lexeme);
    }

    public Set<Entity> getEntity(String lexeme, PoS pos, String sense) throws LexicalSemanticResourceException {
        return getEntity(lexeme);
    }

    public Set<Entity> getParents(Entity entity) throws LexicalSemanticResourceException {
        if (!this.containsEntity(entity)) {
            return Collections.emptySet();
        }
        Page p = WikipediaArticleUtils.entityToPage(wiki, entity, isCaseSensitive);
        return WikipediaArticleUtils.pagesToEntities(wiki, p.getInlinks(), isCaseSensitive );
    }



    public int getNumberOfEntities() {
        long numberOfEntities = wiki.getMetaData().getNumberOfPages() - wiki.getMetaData().getNumberOfRedirectPages();
        return new Long(numberOfEntities).intValue();
    }

    public Iterable<Entity> getEntities() throws LexicalSemanticResourceException {
        return new WikipediaArticleEntityIterable(wiki, isCaseSensitive);
    }

    public Set<Entity> getChildren(Entity entity) throws LexicalSemanticResourceException {
        if (!this.containsEntity(entity)) {
            return Collections.emptySet();
        }
        Page p = WikipediaArticleUtils.entityToPage(wiki, entity, isCaseSensitive);
        return WikipediaArticleUtils.pagesToEntities( wiki, p.getOutlinks(), isCaseSensitive);
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceVersion() {
        StringBuilder sb = new StringBuilder();
        String version = "";
        String language = "";
        try {
            MetaData metaData = wiki.getMetaData();
            language = metaData.getLanguage().toString();
            version = metaData.getVersion();

            if (language == null) {
                language = "unknown-language";
            }
            if (version == null) {
                version = "unknown-version";
            }


        } catch (WikiApiException e) {
            language = "unknown-language";
            version = "unknown-version";
        }

        sb.append(language);
        sb.append("_");
        sb.append(version);

        return sb.toString();
    }

    public Set<String> getRelatedLexemes(String lexeme, PoS pos, String sense, LexicalRelation lexicalRelation) throws LexicalSemanticResourceException {
        Entity entity = WikipediaArticleUtils.lexemeToEntity(wiki, lexeme, isCaseSensitive);
        Set<String> relatedLexemes = new HashSet<String>();
        if (entity == null) {
            return relatedLexemes;
        }

        if (lexicalRelation.equals(LexicalRelation.antonymy)) {
            logger.warn("Wikipedia contains no antonymy information. Returning empty set.");
        }
        else if (lexicalRelation.equals(LexicalRelation.synonymy)) {
            for (String synonym : entity.getLexemes()) {
                String plainSynonym = WikipediaArticleUtils.plainString(synonym);
                if (!plainSynonym.equals(lexeme)) {
                    relatedLexemes.add(plainSynonym);
                }
            }
        }
        return relatedLexemes;
    }

// this method is hijacked a bit for computing the shortest path length between two articles as the shortest path length between category pairs
// TODO find a better way to implement this in the LSR framework
    public int getShortestPathLength(Entity e1, Entity e2) throws LexicalSemanticResourceException {

        // initialize WikipediaCategoryResource only if necessary
        if (this.wikiCategoryResource == null) {
            initializeCategoryResource();
        }

        Set<Category> categories1 = WikipediaArticleUtils.entityToPage(wiki, e1, isCaseSensitive).getCategories();
        Set<Category> categories2 = WikipediaArticleUtils.entityToPage(wiki, e2, isCaseSensitive).getCategories();

        int shortestPathLength = Integer.MAX_VALUE;
        for (Category c1 : categories1) {
            for (Category c2 : categories2) {
                try {
                    Entity catEntity1 = new Entity(c1.getTitle().getWikiStyleTitle());
                    Entity catEntity2 = new Entity(c2.getTitle().getWikiStyleTitle());
                    int pathLength = wikiCategoryResource.getShortestPathLength(catEntity1, catEntity2);
                    if (pathLength < shortestPathLength) {
                        shortestPathLength = pathLength;
                    }
                } catch (UnsupportedOperationException e) {
                    throw new LexicalSemanticResourceException(e);
                } catch (WikiTitleParsingException e) {
                    throw new LexicalSemanticResourceException(e);
                }
            }
        }

        if (shortestPathLength == Integer.MAX_VALUE) {
            return -1;
        }
        else {
            return shortestPathLength;
        }

    }

    // gloss is defined here as the text of the article's first paragraph
    // TODO how does the getFirstParagraph() behave for disambiguation pages?
    public String getGloss(Entity entity) throws LexicalSemanticResourceException {
        if (!this.containsEntity(entity)) {
            return null;
        }

        Page p = WikipediaArticleUtils.entityToPage(wiki, entity, isCaseSensitive);
        ParsedPage pp = p.getParsedPage();
        if (pp == null) {
            return "";
        }
        Paragraph paragraph = pp.getFirstParagraph();
        if (paragraph == null) {
            return "";
        }

        return paragraph.getText();
    }


    //----------------------
    // unimplemented methods
    //----------------------

    public Set<Entity> getRelatedEntities(Entity entity, SemanticRelation semanticRelation) {
        throw new UnsupportedOperationException();
    }

    private void initializeCategoryResource() throws LexicalSemanticResourceException {
        this.wikiCategoryResource = new WikipediaCategoryResource(databaseConfiguration.getHost(), databaseConfiguration.getDatabase(), databaseConfiguration.getUser(), databaseConfiguration.getPassword(), databaseConfiguration.getLanguage());
    }

    @Override
    public Entity getRoot() throws LexicalSemanticResourceException {
        return null;
    }

    @Override
    public Entity getRoot(PoS pos) throws LexicalSemanticResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity getMostFrequentEntity(String lexeme)
        throws LexicalSemanticResourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity getMostFrequentEntity(String lexeme, PoS pos)
        throws LexicalSemanticResourceException
    {
        throw new UnsupportedOperationException();
    }
}