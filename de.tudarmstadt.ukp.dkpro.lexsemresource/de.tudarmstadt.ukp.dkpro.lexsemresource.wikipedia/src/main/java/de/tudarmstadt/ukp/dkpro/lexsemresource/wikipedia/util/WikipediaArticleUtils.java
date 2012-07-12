package de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Title;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class WikipediaArticleUtils {


    public static Set<Entity> pagesToEntities(Wikipedia wiki, Set<Page> pages, boolean isCaseSensitive) throws LexicalSemanticResourceException {
        Set<Entity> e = new HashSet<Entity>();
        for (Page p : pages) {
            e.add(pageToEntity(wiki, p, isCaseSensitive));
        }
        return e;
    }



    public static Entity pageToEntity(Wikipedia wiki, Page p, boolean isCaseSensitive) throws LexicalSemanticResourceException {
        // get the lexemes
        try {
            Entity e = lexemeToEntity(wiki, p.getTitle().getPlainTitle(), isCaseSensitive);
            return e;
        } catch (WikiTitleParsingException e) {
            throw new LexicalSemanticResourceException(e);
        }
    }

    public static Page entityToPage(Wikipedia wiki, Entity entity, boolean isCaseSensitive) throws LexicalSemanticResourceException {
        String lexeme = getCaseSensitiveLexeme(entity.getFirstLexeme(), isCaseSensitive);
      	try {
            return wiki.getPage(lexeme);
        } catch (WikiApiException e) {
            throw new LexicalSemanticResourceException(e);
        }
    }

    /**
     * The lexemes of an entity consist of that lexeme and the redirects the Wikipedia article which title equals the lexeme.
     * @param wiki
     * @param lexeme
     * @return Entity
     * @throws LexicalSemanticResourceException
     */
    public static Entity lexemeToEntity(Wikipedia wiki, String lexeme, boolean isCaseSensitive) throws LexicalSemanticResourceException {

        lexeme = getCaseSensitiveLexeme(lexeme, isCaseSensitive);
    	if (lexeme == null) {
            return null;
    	}

        // I used TreeMap deliberately to keep entries sorted
        Map<String,String> lexemes = new TreeMap<String,String>();
        lexemes.put(lexeme, Entity.UNKNOWN_SENSE);

        // also add redirects
        try {
            Page p = wiki.getPage(lexeme);

			try {
				// if the lexeme that was used to create the page is a redirect itself, we have to
				// find the real page's name first
				if (p.isRedirect()) {
					p = wiki.getPage(p.getTitle().getPlainTitle());
					lexemes.put(p.getTitle().getPlainTitle(), Entity.UNKNOWN_SENSE);
				}

				Set<String> redirects = p.getRedirects();
				for (String redirect : redirects) {
					// transform String to plain representation before adding
					String caseSensitiveLexeme = getCaseSensitiveLexeme(plainString(redirect),
							isCaseSensitive);
					if (caseSensitiveLexeme != null) {
						lexemes.put(caseSensitiveLexeme, Entity.UNKNOWN_SENSE);
					}
				}
			}
			catch (WikiApiException e) {
				throw new LexicalSemanticResourceException(e);
			}
		}
		catch (WikiApiException e) {
			// An exception while getting the initial page is likely to be cause by a page that
			// was not found. It is cheaper to catch and ignore this exception than to explicitly
			// check if the page exists using wiki.existsPage(lexme)
		}

        return new Entity(lexemes);
    }



    //decode the wiki style string
    public static String plainString(String wikiStyleString) throws LexicalSemanticResourceException {
        try {
        	Title t = new Title(wikiStyleString);
        	String str = t.getPlainTitle();
        	if (wikiStyleString.substring(0,1).toLowerCase().equals(wikiStyleString.substring(0,1))) {
        		return str.substring(0, 1).toLowerCase() + str.substring(1, str.length());
        	}
        	else {
                return str;
        	}
        } catch (WikiTitleParsingException e) {
            throw new LexicalSemanticResourceException(e);
        }
    }

    public static String getCaseSensitiveLexeme(String lexeme, boolean isCaseSensitive){
    	if(isCaseSensitive) {
            return genCaseSensitiveString(lexeme);
    	}
    	else {
            return genCaseInsensitiveString(lexeme);
    	}
    }


    private static String genCaseSensitiveString(String lexeme){
    	if (lexeme == null || lexeme.length() == 0) {
            return lexeme;
    	}

    	// TODO TZ: why do we need that condition?
    	// the first letter is low case
    	if(lexeme.substring(0,1).toLowerCase().equals(lexeme.substring(0,1))) {
            return null;
    	}
    	return lexeme;
    }

    private static String genCaseInsensitiveString(String lexeme) {
        if(lexeme == null || lexeme.length() == 0) {
            return lexeme;
        }

        //make the first letter upper cased
    	return lexeme.substring(0,1).toUpperCase() + lexeme.substring(1, lexeme.length());
    }
}