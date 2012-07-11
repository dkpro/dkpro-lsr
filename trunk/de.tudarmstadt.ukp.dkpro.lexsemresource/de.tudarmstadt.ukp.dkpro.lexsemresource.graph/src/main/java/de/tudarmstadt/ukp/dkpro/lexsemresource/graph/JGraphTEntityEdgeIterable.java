package de.tudarmstadt.ukp.dkpro.lexsemresource.graph;

import java.util.Iterator;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;

public class JGraphTEntityEdgeIterable implements Iterable<EntityGraphEdge> {
    
	Graph<Entity, DefaultEdge> graph;
	Set<DefaultEdge> edges;
    
    public JGraphTEntityEdgeIterable(Graph<Entity, DefaultEdge> graph) {
    	this.graph = graph;
    	this.edges = graph.edgeSet();
    }
    
    public Iterator<EntityGraphEdge> iterator() {
        return new JGraphTEntityEdgeIterator(graph, edges);
    }

}
