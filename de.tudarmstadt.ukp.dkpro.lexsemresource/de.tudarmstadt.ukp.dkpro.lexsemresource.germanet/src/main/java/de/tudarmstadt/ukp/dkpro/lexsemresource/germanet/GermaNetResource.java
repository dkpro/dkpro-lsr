package de.tudarmstadt.ukp.dkpro.lexsemresource.germanet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tud.sir.gn.GermaNetObject;
import org.tud.sir.gn.GermaNetParser;
import org.tud.sir.gn.Synset;
import org.tud.sir.gn.WordSense;
import org.tud.sir.util.Constant;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.AbstractResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util.GermaNetEntityIterable;
import de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util.GermaNetUtils;

/**
 * @author zesch
 *
 */
public class GermaNetResource extends AbstractResource {

	private final static String resourceName = "GermaNet";
	private final static String resourceVersion = "5.0";

	private final GermaNetParser gnParser;
	private final GermaNetObject gnObject;

	public GermaNetResource(String path){
    	// Check if we got an URL (file URL)
    	String dir = null;
    	try {
    		URL url = new URL(path);
    		if ("file".equals(url.getProtocol())) {
    			dir = new File(url.getPath()).getAbsolutePath();
    		}
    		else {
    			throw new IllegalArgumentException(
    					"GermaNet resources have to reside on the file "+
    					"system, but are at ["+url+"]");
    		}
    	}
    	catch (IOException e) {
    		// Ignore
    	}

    	if (dir == null) {
    		dir = path;
    	}

		this.gnParser = new GermaNetParser(true);
		this.gnObject = gnParser.parse(dir);
		setIsCaseSensitive(isCaseSensitive);     //set isCaseSensitive to default value
	}


    public boolean containsEntity(Entity entity) throws LexicalSemanticResourceException {
        Set<Synset> synset = GermaNetUtils.entityToSynsets(gnObject, entity, isCaseSensitive);
        if (synset.size() == 0) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean containsLexeme(String lexeme) throws LexicalSemanticResourceException {

        if (lexeme == null) {
            return false;
        }

    	List<WordSense> al = gnObject.getWordSenses(lexeme);

    	if (al == null || al.size() == 0) {
			return false;
		}

    	if (!isCaseSensitive) {
    		return true;
    	}
    	else {
    		for(WordSense ws : al) {
    			if (lexeme.equals(ws.getGrapheme())) {
                    return true;
    			}
    		}
    	}
        return false;
    }


    public Set<Entity> getEntity(String lexeme) throws LexicalSemanticResourceException {
        Entity e = new Entity(lexeme);
        Set<Entity> entities = GermaNetUtils.synsetsToEntities( GermaNetUtils.entityToSynsets(gnObject, e, isCaseSensitive) );
        return entities;
    }

    public Set<Entity> getEntity(String lexeme, PoS pos) throws LexicalSemanticResourceException {
        Entity e = new Entity(lexeme, pos);
        Set<Entity> entities = GermaNetUtils.synsetsToEntities( GermaNetUtils.entityToSynsets(gnObject, e , isCaseSensitive) );
        return entities;
    }

    public Set<Entity> getEntity(String lexeme, PoS pos, String sense) throws LexicalSemanticResourceException {
        Entity e = new Entity(lexeme, pos, sense);
        Set<Entity> entities = GermaNetUtils.synsetsToEntities( GermaNetUtils.entityToSynsets(gnObject, e, isCaseSensitive) );
        return entities;
    }

    public Set<Entity> getChildren(Entity entity) throws LexicalSemanticResourceException {
        Set<Entity> children = new HashSet<Entity>();
        children.addAll(getRelatedEntities(entity, SemanticRelation.hyponymy));
        return children;
	}

	public Iterable<Entity> getEntities() throws LexicalSemanticResourceException {
		return new GermaNetEntityIterable(gnObject);
	}

	public int getNumberOfEntities() throws LexicalSemanticResourceException {
		return gnObject.getWordSensesAmount(Constant.ADJECTIVE)
		+gnObject.getWordSensesAmount(Constant.VERB)
		+gnObject.getWordSensesAmount(Constant.NOUN);
	}

    public Set<Entity> getParents(Entity entity) throws LexicalSemanticResourceException {
        Set<Entity> parents = new HashSet<Entity>();
        parents.addAll(getRelatedEntities(entity, SemanticRelation.hypernymy));
        return parents;
	}

	public String getResourceName() {
		return resourceName;
	}

	public String getResourceVersion() {
		return resourceVersion;
	}

	public int getShortestPathLength(Entity firstEntity, Entity secondEntity) {
	    throw new UnsupportedOperationException();
	}
    public String getGloss(Entity entity) {
        throw new UnsupportedOperationException();
    }




    @SuppressWarnings("unchecked")
    public Set<String> getRelatedLexemes(String lexeme, PoS pos, String sense, LexicalRelation lexicalRelation) throws LexicalSemanticResourceException {
        WordSense wordSense = gnObject.getWordSense(lexeme, GermaNetUtils.mapPos(pos), new Integer(sense));

        if (wordSense == null) {
            return Collections.emptySet();
        }

        if(isCaseSensitive) {
            if(!wordSense.getGrapheme().equals(lexeme)) {
                return Collections.emptySet();
            }
        }

        Set<String> resultLexemes = new HashSet<String>();

        if (lexicalRelation.equals(LexicalRelation.antonymy)) {
            List<WordSense> antonyms = wordSense.getAntonyms();
            if (antonyms != null) {
                for (WordSense antonym : antonyms) {
                    resultLexemes.add(antonym.getGrapheme());
                }
            }
        }
        else if (lexicalRelation.equals(LexicalRelation.synonymy)) {
            Synset synset = wordSense.getSynset();
            List<WordSense> synonyms = synset.getWordSenses();
            if (synonyms != null) {
                for (WordSense synonym : synonyms) {
                    // a lexeme is not a synonym to itself
                    if (!synonym.getGrapheme().equals(lexeme)) {
                        resultLexemes.add(synonym.getGrapheme());
                    }
                }
            }
        }
        else {
            return Collections.emptySet();
        }

        return resultLexemes;
    }


    @SuppressWarnings("unchecked")
    public Set<Entity> getRelatedEntities(Entity entity, SemanticRelation semanticRelation) throws LexicalSemanticResourceException {
        if(!containsEntity(entity)) {
            return Collections.emptySet();
        }
        Set<Synset> resultSynsets = new HashSet<Synset>();

        Set<Synset> synsets = GermaNetUtils.entityToSynsets(gnObject, entity, isCaseSensitive);
        for (Synset synset : synsets) {
            if (semanticRelation.equals(SemanticRelation.holonymy)) {
                if (synset.getHolonyms() != null) {
                    resultSynsets.addAll(synset.getHolonyms());
                }
            }
            else if (semanticRelation.equals(SemanticRelation.hypernymy)) {
                if (synset.getHyperonyms() != null) {
                    resultSynsets.addAll(synset.getHyperonyms());
                }
            }
            else if (semanticRelation.equals(SemanticRelation.hyponymy)) {
                if (synset.getHyponyms() != null) {
                    resultSynsets.addAll(synset.getHyponyms());
                }
            }
            else if (semanticRelation.equals(SemanticRelation.meronymy)) {
                if (synset.getMeronyms() != null) {
                    resultSynsets.addAll(synset.getMeronyms());
                }
            }
            else if (semanticRelation.equals(SemanticRelation.cohyponymy)) {
                if (synset.getHyperonyms() != null) {
                    for (Object hyperonym : synset.getHyperonyms()) {
                        resultSynsets.addAll(((Synset) hyperonym).getHyponyms());
                    }
                }
            }
        }

        assert(resultSynsets != null);

        Set<Entity> resultEntities = GermaNetUtils.synsetsToEntities(resultSynsets);
        return resultEntities;
    }

    @Override
    public Entity getRoot() throws LexicalSemanticResourceException {
        Map<String,String> rootLexemes = new HashMap<String,String>();
        rootLexemes.put("Entität", "1");
        try {
            return this.getEntity(rootLexemes, PoS.n);
        } catch (UnsupportedOperationException e) {
            return null;
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