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
package de.tudarmstadt.ukp.dkpro.lexsemresource.graph;

import java.util.Iterator;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;

public class JGraphTEntityEdgeIterator implements Iterator<EntityGraphEdge> {

	Graph<Entity, DefaultEdge> graph;
	Iterator<DefaultEdge> iterator;


	public JGraphTEntityEdgeIterator(Graph<Entity, DefaultEdge> graph, Set<DefaultEdge> edges) {
		this.graph = graph;
		iterator = edges.iterator();
	}

    @Override
	public boolean hasNext() {
    	return iterator.hasNext();
    }

    @Override
	public EntityGraphEdge next() {
    	DefaultEdge edge = iterator.next();
		EntityGraphEdge entityGraphEdge = new EntityGraphEdge(
				graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
    	return entityGraphEdge;
    }

	@Override
	public void remove() {
        throw new UnsupportedOperationException();
	}

}
