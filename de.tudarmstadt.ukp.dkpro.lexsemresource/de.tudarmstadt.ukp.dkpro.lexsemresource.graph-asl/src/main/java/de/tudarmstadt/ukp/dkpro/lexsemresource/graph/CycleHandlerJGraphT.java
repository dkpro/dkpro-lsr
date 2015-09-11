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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.graph.DefaultEdge;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;

/**
 * Methods for handling cycles in the entity graph build using JGraphT.
 *
 * @author zesch
 *
 */
public class CycleHandlerJGraphT {
	private Log logger = LogFactory.getLog(getClass());

    EntityGraphJGraphT entityGraph;

    private enum Color {white, grey, black};
    private Map<Entity, Color> colorMap;

    /**
     * Creates a cycle handler object.
     * @param entityGraph The entity graph in which cycles should be handeled.
     */
    public CycleHandlerJGraphT(EntityGraphJGraphT entityGraph) {
        this.entityGraph = entityGraph;
    }

    /**
     * The JGraphT cycle detection seems not to find all cycles. Thus, I wrote my own cycle detection.
     * It is a colored DFS and should find all (viscious :) cycles.
     * @return True, if the graph contains a cycle.
     */
    public boolean containsCycle() {
        DefaultEdge edge = findCycle();
        if (edge != null) {
            Entity sourceEntity = entityGraph.directedGraph.getEdgeSource(edge);
            Entity targetEntity = entityGraph.directedGraph.getEdgeTarget(edge);

            logger.info("Cycle: " + sourceEntity.getId() + " - " + targetEntity.getId());
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes cycles from the graph that was used to construct the cycle handler.
     */
    public void removeCycles() {
        DefaultEdge edge = null;
        while ((edge = findCycle()) != null) {
            Entity sourceEntity = entityGraph.directedGraph.getEdgeSource(edge);
            Entity targetEntity = entityGraph.directedGraph.getEdgeTarget(edge);

            logger.info("Removing cycle: " + sourceEntity.getId() + " - " + targetEntity.getId());

            entityGraph.undirectedGraph.removeEdge(edge);
        }
    }

    private DefaultEdge findCycle() {
        colorMap = new HashMap<Entity, Color>();
        // initialize all nodes with white
        for (Entity node : entityGraph.directedGraph.vertexSet()) {
            colorMap.put(node, Color.white);
        }

        for (Entity node : entityGraph.directedGraph.vertexSet()) {
            if (colorMap.get(node).equals(Color.white)) {
                DefaultEdge e = visit(node);
                if (e != null) {
                    return e;
                }
            }
        }
        return null;
    }

    private DefaultEdge visit(Entity node) {
        colorMap.put(node, Color.grey);
        Set<DefaultEdge> outgoingEdges = entityGraph.directedGraph.outgoingEdgesOf(node);
        for (DefaultEdge edge : outgoingEdges) {
            Entity outNode = entityGraph.directedGraph.getEdgeTarget(edge);
            if (colorMap.get(outNode).equals(Color.grey)) {
                return edge;
            }
            else if (colorMap.get(outNode).equals(Color.white)) {
                DefaultEdge e = visit(outNode);
                if (e != null) {
                    return e;
                }
            }
        }
        colorMap.put(node, Color.black);
        return null;
    }
}