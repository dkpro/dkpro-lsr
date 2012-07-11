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
