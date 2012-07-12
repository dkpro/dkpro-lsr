package de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.AbstractResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util.WikipediaCategoryEntityIterable;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util.WikipediaCategoryUtils;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.CategoryGraph;
import de.tudarmstadt.ukp.wikipedia.api.CategoryGraphManager;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.MetaData;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class WikipediaCategoryResource extends AbstractResource {

    private static final String resourceName = "WikipediaCG";

    private Wikipedia wiki;
    private CategoryGraph catGraph;

    public WikipediaCategoryResource(DatabaseConfiguration dbConfig) throws LexicalSemanticResourceException {
        try {
            this.wiki = new Wikipedia(dbConfig);
        } catch (WikiInitializationException e) {
            throw new LexicalSemanticResourceException("Wikipedia could not be initialized.",e);
        }

        initialSetup();
    }

    public WikipediaCategoryResource(Wikipedia wiki) throws LexicalSemanticResourceException {
        this.wiki = wiki;

        initialSetup();
    }

    public WikipediaCategoryResource(String host, String database, String user, String password, Language language) throws LexicalSemanticResourceException {
        this(new DatabaseConfiguration(host, database, user, password, language));
    }

    private void initialSetup() {
        setIsCaseSensitive(isCaseSensitive);    // set case sensitive to default value
        this.catGraph = null;                   // initialize category graph only if needed
    }

    public boolean containsEntity(Entity entity) throws LexicalSemanticResourceException {
        return containsLexeme(entity.getFirstLexeme());
    }

    public boolean containsLexeme(String lexeme) throws LexicalSemanticResourceException {
        lexeme = WikipediaCategoryUtils.getCaseSensitiveLexeme(lexeme, isCaseSensitive);
    	try {
            if(wiki.getCategory(lexeme) == null) {
                return false;
            }
        } catch (WikiApiException e) {
            return false;
        }
        return true;
    }

    public Set<Entity> getEntity(String lexeme) throws LexicalSemanticResourceException {

    	if (!containsLexeme(lexeme)) {
            return Collections.emptySet();
        }
        // we use a shortcut here, as a lexeme that is contained in Wikipedia always corresponds to a single entity that contains that lexeme
        Entity e = new Entity(lexeme);
        Set<Entity> eSet = new HashSet<Entity>();
        eSet.add(e);
        return eSet;
    }

    public Set<Entity> getEntity(String lexeme, PoS pos) throws LexicalSemanticResourceException {
        return getEntity(lexeme);
    }

    public Set<Entity> getEntity(String lexeme, PoS pos, String sense) throws LexicalSemanticResourceException {
        return getEntity(lexeme);
    }

//    public Entity getEntity(Set<String> lexemes) throws LexicalSemanticResourceException {
//        if (lexemes.size() != 1) {
//            logger.warn("Wikipedia does not support multiple lexemes in an entity. Silently taking only the first.");
//        }
//        String queryItem = lexemes.iterator().next();
//
//        if (!containsLexeme(queryItem)) {
//            return null;
//        }
//        return new Entity(queryItem);
//    }
//
//    public Entity getEntity(Set<String> lexemes, String pos) throws LexicalSemanticResourceException {
//        return getEntity(lexemes);
//    }
//
//    public Entity getEntity(Set<String> lexemes, String pos, String sense) throws LexicalSemanticResourceException {
//        return getEntity(lexemes);
//    }


    public Set<Entity> getParents(Entity entity) throws LexicalSemanticResourceException {
        Set<Entity> parents = new HashSet<Entity>();
        Category cat;
        String lexeme = WikipediaCategoryUtils.getCaseSensitiveLexeme(entity.getFirstLexeme(), isCaseSensitive);
        if (lexeme == null) {
            return parents;
        }

        try {
            cat = wiki.getCategory(lexeme);
            if (cat == null)  {
                return parents;
            }
            Set<Category> parentCategories = cat.getParents();
            for (Category parentCategory : parentCategories) {
                parents.add(new Entity(parentCategory.getTitle().getWikiStyleTitle()));
            }
        } catch (WikiApiException e) {
            throw new LexicalSemanticResourceException(e);
        }
        return parents;
    }

    public int getNumberOfEntities() {
        return new Long(wiki.getMetaData().getNumberOfCategories()).intValue();
    }

    public Iterable<Entity> getEntities() throws LexicalSemanticResourceException {
        return new WikipediaCategoryEntityIterable(wiki);
    }

    public Set<Entity> getChildren(Entity entity) throws LexicalSemanticResourceException {
        Set<Entity> children = new HashSet<Entity>();
        Category cat = null;
        String lexeme = WikipediaCategoryUtils.getCaseSensitiveLexeme(entity.getFirstLexeme(), isCaseSensitive);
        if (lexeme == null) {
            return children;
        }

        try {
            cat = wiki.getCategory(lexeme);
            if (cat == null) {
                return children;
            }
            Set<Category> childCategories = cat.getChildren();
            for (Category childCategory : childCategories) {
                children.add(new Entity(childCategory.getTitle().getWikiStyleTitle()));
            }
        } catch (WikiApiException e) {
            throw new LexicalSemanticResourceException(e);
        }
        return children;
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

    public int getShortestPathLength(Entity firstEntity, Entity secondEntity) throws LexicalSemanticResourceException {


        String lexeme1 = WikipediaCategoryUtils.getCaseSensitiveLexeme(firstEntity.getFirstLexeme(), isCaseSensitive);
        String lexeme2 = WikipediaCategoryUtils.getCaseSensitiveLexeme(secondEntity.getFirstLexeme(), isCaseSensitive);

    	if (lexeme1 == null || lexeme2 == null) {
            return -1;
    	}
    	if (this.catGraph == null) {
            initializeCategoryGraph();
        }

        Category c1;
        Category c2;
        try {
            c1 = wiki.getCategory(lexeme1);
            c2 = wiki.getCategory(lexeme2);
            return catGraph.getPathLengthInEdges(c1, c2);
        } catch (WikiApiException e) {
            return -1;
        }
    }

    @Override
    public Entity getRoot() throws LexicalSemanticResourceException {
        try {
            String rootTitle = this.wiki.getMetaData().getMainCategory().getTitle().getWikiStyleTitle();

            Map<String,String> rootLexemes = new HashMap<String,String>();
            rootLexemes.put(rootTitle, Entity.UNKNOWN_SENSE);

            try {
                return this.getEntity(rootLexemes, Entity.UNKNOWN_POS);
            } catch (UnsupportedOperationException e) {
                return null;
            }
        } catch (WikiTitleParsingException e) {
            throw new LexicalSemanticResourceException(e);
        } catch (WikiApiException e) {
            throw new LexicalSemanticResourceException(e);
        }
    }


    @Override
    public Entity getRoot(PoS pos) throws LexicalSemanticResourceException {
        if (pos.equals(PoS.n)) {
            return getRoot();
        }
        else {
            return null;
        }
    }

    //----------------------
    // unimplemented methods
    //----------------------
    public String getGloss(Entity entity) {
        throw new UnsupportedOperationException();
    }

    public Set<String> getRelatedLexemes(String lexeme, PoS pos, String sense, LexicalRelation lexicalRelation) throws LexicalSemanticResourceException {
        throw new UnsupportedOperationException();
    }

    public Set<Entity> getRelatedEntities(Entity entity, SemanticRelation semanticRelation) {
        throw new UnsupportedOperationException();
    }


    //----------------------
    // private methods
    //----------------------
    private void initializeCategoryGraph() throws LexicalSemanticResourceException  {
        try {
            this.catGraph = CategoryGraphManager.getCategoryGraph(wiki);
        } catch (WikiApiException e) {
            e.printStackTrace();
            throw new LexicalSemanticResourceException("Category graph could not be initialized.",e);
        }
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
