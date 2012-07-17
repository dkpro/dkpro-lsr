/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util;

import java.util.Iterator;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.Synset;

public class GermaNetEntityIterable extends LexicalSemanticEntityIterable{


	private List<Synset> synsets;
	
    public GermaNetEntityIterable(GermaNet gn){
		this.synsets = gn.getSynsets();
	}
	
	@Override
	public Iterator<Entity> iterator() {
		return new GermaNetEntityIterator(synsets.iterator());
	}
}