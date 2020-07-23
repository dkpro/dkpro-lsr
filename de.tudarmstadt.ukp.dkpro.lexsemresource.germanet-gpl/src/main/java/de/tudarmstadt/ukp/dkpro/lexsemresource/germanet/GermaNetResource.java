/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.lexsemresource.germanet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.AbstractResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util.GermaNetEntityIterable;
import de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util.GermaNetUtils;
import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.LexRel;
import de.tuebingen.uni.sfs.germanet.api.LexUnit;
import de.tuebingen.uni.sfs.germanet.api.Synset;

/**
 * @author zesch
 *
 */
public class GermaNetResource extends AbstractResource {

	private final static String resourceName = "GermaNet";
	private final static String resourceVersion = "15.0";

	private final GermaNet gn;

	public GermaNetResource(String path, boolean ignoreCase)
	    throws LexicalSemanticResourceException
	{
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

        try {
            this.gn = new GermaNet(dir, ignoreCase);
            setIsCaseSensitive(!ignoreCase);
        }
        catch (FileNotFoundException e) {
            throw new LexicalSemanticResourceException(e);
        }
        catch (XMLStreamException e) {
            throw new LexicalSemanticResourceException(e);
        }
        catch (IOException e) {
            throw new LexicalSemanticResourceException(e);
        }
	}
	
	public GermaNetResource(String path) throws LexicalSemanticResourceException{
	    this(path, false);
	}


    public boolean containsEntity(Entity entity) throws LexicalSemanticResourceException {
        Set<Synset> synset = GermaNetUtils.entityToSynsets(gn, entity);
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

        return (gn.getLexUnits(lexeme, true).size() > 0);
    }


    public Set<Entity> getEntity(String lexeme) throws LexicalSemanticResourceException {
        Entity e = new Entity(lexeme);
        Set<Entity> entities = GermaNetUtils.synsetsToEntities( GermaNetUtils.entityToSynsets(gn, e) );
        return entities;
    }

    public Set<Entity> getEntity(String lexeme, PoS pos) throws LexicalSemanticResourceException {
        Entity e = new Entity(lexeme, pos);
        Set<Entity> entities = GermaNetUtils.synsetsToEntities( GermaNetUtils.entityToSynsets(gn, e) );
        return entities;
    }

    public Set<Entity> getEntity(String lexeme, PoS pos, String sense) throws LexicalSemanticResourceException {
        Entity e = new Entity(lexeme, pos, sense);
        Set<Entity> entities = GermaNetUtils.synsetsToEntities( GermaNetUtils.entityToSynsets(gn, e) );
        return entities;
    }

    public Set<Entity> getChildren(Entity entity) throws LexicalSemanticResourceException {
        Set<Entity> children = new HashSet<Entity>();
        children.addAll(getRelatedEntities(entity, SemanticRelation.hyponymy));
        return children;
	}

	public Iterable<Entity> getEntities() throws LexicalSemanticResourceException {
		return new GermaNetEntityIterable(gn);
	}

	public int getNumberOfEntities() throws LexicalSemanticResourceException {
		return gn.numSynsets();
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




    public Set<String> getRelatedLexemes(String lexeme, PoS pos, String sense, LexicalRelation lexicalRelation)
        throws LexicalSemanticResourceException
    {
        
        Set<String> resultLexemes = new HashSet<String>();

        for (LexUnit lexUnit : gn.getLexUnits(lexeme, GermaNetUtils.mapPos(pos), true)) {
            if (Integer.toString(lexUnit.getSense()).equals(sense)) {
                if (lexicalRelation.equals(LexicalRelation.antonymy)) {
                    for (LexUnit antonym : lexUnit.getRelatedLexUnits(LexRel.has_antonym)) {
                        resultLexemes.add(antonym.getOrthForm());
                    }
                }
                else if (lexicalRelation.equals(LexicalRelation.synonymy)) {
                    for (LexUnit synonym : lexUnit.getRelatedLexUnits(LexRel.has_synonym)) {
                        resultLexemes.add(synonym.getOrthForm());
                    }
                }
            }
        }

        return resultLexemes;
    }


    public Set<Entity> getRelatedEntities(Entity entity, SemanticRelation semanticRelation)
        throws LexicalSemanticResourceException
    {
        if(!containsEntity(entity)) {
            return Collections.emptySet();
        }
        Set<Synset> resultSynsets = new HashSet<Synset>();

        Set<Synset> synsets = GermaNetUtils.entityToSynsets(gn, entity);
        for (Synset synset : synsets) {
            if (semanticRelation.equals(SemanticRelation.holonymy)) {
                resultSynsets.addAll(synset.getRelatedSynsets(ConRel.has_component_holonym));
                resultSynsets.addAll(synset.getRelatedSynsets(ConRel.has_member_holonym));
                resultSynsets.addAll(synset.getRelatedSynsets(ConRel.has_portion_holonym));
            }
            else if (semanticRelation.equals(SemanticRelation.hypernymy)) {
                resultSynsets.addAll(synset.getRelatedSynsets(ConRel.has_hypernym));
            }
            else if (semanticRelation.equals(SemanticRelation.hyponymy)) {
                resultSynsets.addAll(synset.getRelatedSynsets(ConRel.has_hyponym));
            }
            else if (semanticRelation.equals(SemanticRelation.meronymy)) {
                resultSynsets.addAll(synset.getRelatedSynsets(ConRel.has_component_meronym));
                resultSynsets.addAll(synset.getRelatedSynsets(ConRel.has_member_meronym));
                resultSynsets.addAll(synset.getRelatedSynsets(ConRel.has_portion_meronym));
            }
            else if (semanticRelation.equals(SemanticRelation.cohyponymy)) {
                resultSynsets.addAll(synset.getRelatedSynsets(ConRel.has_hyponym));
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