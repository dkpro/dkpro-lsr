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
package de.tudarmstadt.ukp.dkpro.lexsemresource.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.graph.EntityGraph.DirectionMode;

/**
 * Provides a number of methods for analyzing the structural properties of a graph.
 *
 * @author garoufi
 *
 */
public class GraphAnalyzer {

	public Entity vertex;
	public Entity source, target;

	private final EntityGraph entityGraph;

	public GraphAnalyzer(EntityGraph entityGraph) {
		this.entityGraph = entityGraph;
	}

    /**
     * Get the number of nodes in the graph.
     * @return The number of nodes in the graph.
     */
    public int getNumberOfNodes() throws LexicalSemanticResourceException {
        return entityGraph.getNumberOfNodes();
    }

    /**
     * Get the number of edges in the graph.
     * @return The number of edges in the graph.
     */
    public int getNumberOfEdges() {
        return entityGraph.getNumberOfEdges();
    }

    /**
     * Check whether the graph contains a certain vertex.
     * @param The vertex to check in Entity representation.
     * @return True if vertex contained in graph; false otherwise
     */
    public boolean containsVertex(Entity vertex) {
    	return entityGraph.containsVertex(vertex);
    }

    /**
     * Check whether the graph contains an edge between two certain vertices.
     * @param The source vertex in Entity representation.
     * @param The target vertex in Entity representation.
     * @return True if edge contained in graph; false otherwise
     */
    public boolean containsEdge(Entity source, Entity target) {
        return entityGraph.containsEdge(source, target);
    }

    /**
     * Get the children vertices.
     * @param The vertex in Entity representation.
     * @return A set of child vertices of the given vertex in Entity representation
     */
    public Set<Entity> getChildren(Entity vertex) {
        return entityGraph.getChildren(vertex);
    }

    /**
     * Get the parent vertices.
     * @param The vertex in Entity representation.
     * @return A set of parent vertices of the given vertex in Entity representation
     */
    public Set<Entity> getParents(Entity vertex) {
        return entityGraph.getParents(vertex);
    }

    /**
     * Get the neighbor vertices as the union of children and parents
     * @param The vertex in Entity representation.
     * @return A set of neighbor vertices of the given vertex in Entity representation
     */
    public Set<Entity> getNeighbors(Entity vertex) {
    	return entityGraph.getNeighbors(vertex);
    }

    /**
     * Get the indegree of the vertex, i.e. the number of its parents
     * @param The vertex in Entity representation.
     */
    public int getInDegree(Entity vertex) {
    	return entityGraph.getInDegree(vertex);
    }

    /**
     * Get the outdegree of the vertex, i.e. the number of its children
     * @param The vertex in Entity representation.
     */
    public int getOutDegree(Entity vertex) {
    	return entityGraph.getOutDegree(vertex);
    }

    /**
     * Get the degree of the vertex, i.e. the number of its neighbors
     * @param The vertex in Entity representation.
     */
    public int getDegree(Entity vertex) {
    	return entityGraph.getDegree(vertex);
    }

    /**
     * Get the average degree of the graph, i.e. the average number of neighbors of its nodes
     * @return The average node degree of the graph.
     */
    public double getAverageDegree() throws LexicalSemanticResourceException{
    	return entityGraph.getAverageDegree();
    }

    /**
     * @return The leaf nodes of the graph, i.e. nodes with outdegree = 0.
     */
    public Set<Entity> getLeaves() {
    	return entityGraph.getLeaves();
    }

    /**
     * @return The number of leaf nodes of the graph, i.e. the number of nodes with outdegree = 0.
     */
    public int getNumberOfLeaves() {
    	return entityGraph.getNumberOfLeaves();
    }

    /**
     * @return The root nodes of the graph, i.e. nodes with indegree = 0.
     */
    public Set<Entity> getRoots() {
    	return entityGraph.getRoots();
    }

    /**
     * @return The number of root nodes of the graph, i.e. the number of nodes with indegree = 0.
     */
    public int getNumberOfRoots() {
    	return entityGraph.getNumberOfRoots();
    }

    /**
     * Get the shortest path between two nodes, treating the graph as undirected
     * @param source the first node
     * @param target the second node
     * @return A list of the edges in the path between the two nodes
     */
    public List<Entity> getShortestPathUndirected(Entity source, Entity target) {
    	return entityGraph.getShortestPath(source, target, DirectionMode.undirected);
    }

    /**
     * Get the shortest path between two nodes in the directed version of the graph
     * @param source the first node
     * @param target the second node
     * @return A list of the edges in the path between the two nodes
     */
    public List<Entity> getShortestPathDirected(Entity source, Entity target) {
    	return entityGraph.getShortestPath(source, target, DirectionMode.directed);
    }

	/**
	 * Get the length of the shortest path between two nodes in the undirected version of the graph
	 * @param source the first node
	 * @param target the second node
	 * @return The number of edges of the path between the two nodes
	 */
	public double getShortestPathLengthUndirected(Entity source, Entity target) {
		return entityGraph.getShortestPathLength(source, target, DirectionMode.undirected);
	}

	/**
	 * Get the length of the shortest path between two nodes in the directed version of the graph
	 * @param source the first node
	 * @param target the second node
	 * @return The number of edges of the path between the two nodes
	 */
	public double getShortestPathLengthDirected(Entity source, Entity target) {
		return entityGraph.getShortestPathLength(source, target, DirectionMode.directed);
	}

    /**
     * Check whether the graph contains a symmetric link between two certain vertices,
     * i.e., both a link from source to target and a link from target to source
     * @param The source vertex in Entity representation.
     * @param The target vertex in Entity representation.
     * @return True if there is a symmetric link; false otherwise
     */
	public boolean isSymmetricLink(Entity source, Entity target) {
		return entityGraph.isSymmetricLink(source, target);
	}

    /**
     * @return The number of symmetric links in the graph, i.e. the number of links that are defined
     * bidirectionally between two nodes
     */
    public int getNumberOfSymmetricLinks() {
    	return entityGraph.getNumberOfSymmetricLinks();
    }

    /**
     * @return The set of isolated nodes in the graph, i.e. the set of nodes with degree = 0
     */
    public Set<Entity> getIsolatedNodes() {
    	return entityGraph.getIsolatedNodes();
    }

    /**
     * @return The number of isolated nodes in the graph, i.e. the number of nodes with degree = 0
     */
    public int getNumberOfIsolatedNodes() {
    	return entityGraph.getNumberOfIsolatedNodes();
    }

    /**
     * @return The average of the path lengths of all node pairs
     */
    public double getAverageShortestPathLength() {
    	return entityGraph.getAverageShortestPathLength();
    }

    /**
     * @return The diameter of the graph, i.e. the maximum of the shortest path lengths of all node pairs
     */
    public double getDiameter() {
    	return entityGraph.getDiameter();
    }

    /**
     * @return The radius of the graph, i.e. the minimum eccentricity over all pairs in the graph
     */
    public double getRadius() {
    	return entityGraph.getRadius();
    }

    /**
     * @param node The given node
     * @return The eccentricity of the graph, i.e. the maximum of lengths of the shortest path between the
     * given node and any other node
     */
    public double getEccentricity(Entity node) {
    	return entityGraph.getEccentricity(node);
    }

    public Set<Entity> getCenter() {
    	return entityGraph.getCenter();
    }

    /**
     * @return The cluster coefficient of the graph
     */
    public double getClusterCoefficient() {
    	return entityGraph.getClusterCoefficient();
    }

    /**
     * @return A map with the degree distribution of the graph.
     */
    public Map<Integer, Integer> getDegreeDistribution() {
        return entityGraph.getDegreeDistribution();

    }

    /**
     * @return Returns the largest connected component as a new graph. If the base graph already
     * is connected, it simply returns the whole graph.
     * @throws LexicalSemanticResourceException
     * @throws UnsupportedOperationException
     */
    public EntityGraph getLargestConnectedComponent() throws LexicalSemanticResourceException {
    	return entityGraph.getLargestConnectedComponent();
    }

    /**
     * This parameter is already set in the constructor as it is needed for computation of relatedness values.
     * Therefore its computation does not trigger setGraphParameters (it is too slow), even if the depth is
     * implicitly determined there, too.
     * @return The depth of the graph, i.e. the maximum path length starting with the root node (if a single
     * one exists).
     * @throws UnsupportedOperationException
     * @throws LexicalSemanticResourceException
     */
    public double getDepth() throws LexicalSemanticResourceException {
        return entityGraph.getDepth();
    }

    public void containsCycles() throws LexicalSemanticResourceException {
    	entityGraph.containsCycles();
    }

    public List<String> getPageRank() {
    	return entityGraph.getPageRank();
    }

    public List<String> getHITS() {
    	return entityGraph.getHITS();
    }

 	/**
	 * Checks a graph for sets of structurally equivalent vertices: vertices that share all the same edges.
	 * Specifically, In order for a pair of vertices  i  and j  to be structurally equivalent, the set of
	 * i 's neighbors must be identical to the set of j 's neighbors, with the exception of i  and j
	 * themselves. This algorithm finds all sets of equivalent vertices in O(V^2) time.
	 * @return A set of structurally equivalent node sets.
	 * @throws UnsupportedOperationException
	 */
	public Set<Set<Entity>> getStructuralEquivalences() {
		return entityGraph.getStructuralEquivalences();
	}
	}

