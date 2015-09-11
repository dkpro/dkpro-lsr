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

    private final DirectedGraph<Entity,DefaultEdge> graph;

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
     * @return The graph
     */
    public DirectedGraph<Entity,DefaultEdge> getGraph(){
        return graph;
    }
}