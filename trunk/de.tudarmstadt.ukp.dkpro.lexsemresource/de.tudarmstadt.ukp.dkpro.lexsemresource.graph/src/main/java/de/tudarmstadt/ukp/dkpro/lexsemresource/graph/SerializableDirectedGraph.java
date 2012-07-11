package de.tudarmstadt.ukp.dkpro.lexsemresource.graph;

import java.io.Serializable;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;

/**
 * Serializable Wrapper for a DirectedGraph object, that has Integer objects as vertices and DefaultEdge objects as edges.<br>
 * There is no need in this case to serializale vertices and edges separately, because they already implement the interface Serializable.
 * 
 * @author Anouar
 *
 */
public final class SerializableDirectedGraph implements Serializable {

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -6982915662986424315L;

    private DirectedGraph<Entity,DefaultEdge> graph;
    
    /**
     * This Constructor is intended to be used before the serialization of the <br>
     * directed graph.
     * @param graph
     */
    public SerializableDirectedGraph(DirectedGraph<Entity,DefaultEdge> graph){
        this.graph = graph;
    }
    
    /**
     * Returns the graph. 
     * @return
     */
    public DirectedGraph<Entity,DefaultEdge> getGraph(){
        return graph;
    }
}