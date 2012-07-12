/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util;

import java.util.Iterator;
import java.util.Set;

import org.tud.sir.gn.GermaNetObject;
import org.tud.sir.util.Constant;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;


/**
 * @author Anouar
 *
 */
public class GermaNetEntityIterable extends LexicalSemanticEntityIterable{


	private Set synsets;
	
	@SuppressWarnings("unchecked")
    public GermaNetEntityIterable(GermaNetObject gnObject){
		this.synsets = gnObject.getAllSynsets(Constant.ADJECTIVE);
		this.synsets.addAll(gnObject.getAllSynsets(Constant.VERB));
		this.synsets.addAll(gnObject.getAllSynsets(Constant.NOUN));
	}
	
	@Override
	public Iterator<Entity> iterator() {
		return new GermaNetEntityIterator(synsets.iterator());
	}

}
