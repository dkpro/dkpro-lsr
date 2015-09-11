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

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;

/**
 * An interface for the representation of a graph. It is implemented
 * by EntityGraphJGraphT and EntityGraphJUNG.
 *
 * @author garoufi
 *
 */
public interface EntityGraph {

	    public enum DirectionMode {
	        directed,
	        undirected
	    }

	    /**
	     * @return A unique ID for a graph.
	     */
	    public String getGraphId();

		public Iterable<Entity> getNodes();
    	public Iterable<EntityGraphEdge> getEdges();

	    public int getNumberOfNodes();
	    public int getNumberOfEdges();

	    public boolean containsVertex(Entity vertex);
	    public boolean containsEdge(Entity source, Entity target);

	    public boolean isSymmetricLink(Entity source, Entity target);

        public EntityGraph getLargestConnectedComponent() throws LexicalSemanticResourceException;

        public Set<Entity> getChildren(Entity vertex);
	    public Set<Entity> getParents(Entity vertex);
	    public Set<Entity> getNeighbors(Entity vertex);

	    public Entity getLCS(Entity root, Entity e1, Entity e2) throws LexicalSemanticResourceException;
	    public double getIntrinsicInformationContent(Entity e) throws LexicalSemanticResourceException;

	    public Set<Entity> getLeaves();
	    public Set<Entity> getRoots();
	    public Set<Entity> getIsolatedNodes();
	    public Set<Entity> getCenter();

	    public int getNumberOfLeaves();
	    public int getNumberOfRoots();
	    public int getNumberOfSymmetricLinks();
	    public int getNumberOfIsolatedNodes();

	    public double getDepth() throws LexicalSemanticResourceException;

	    public int getInDegree(Entity vertex);
	    public int getOutDegree(Entity vertex);
	    public int getDegree(Entity vertex);
	    public double getEccentricity(Entity vertex);

	    public double getAverageDegree();
	    public Map<Integer, Integer> getDegreeDistribution();

	    /**
	     * @param source The source node.
	     * @param target The target node.
	     * @param mode Whether the graph should be treated as directed or undirected.
	     * @return The shortest path from source to target or null if no path was found.
	     */
	    public List<Entity> getShortestPath(Entity source, Entity target, DirectionMode mode);

	    /**
         * @param source The source node.
         * @param target The target node.
         * @param mode Whether the graph should be treated as directed or undirected.
         * @return The length of the shortest path from source to target measured in <b>edges</b>
	     */
	    public double getShortestPathLength(Entity source, Entity target, DirectionMode mode);

	    public double getAverageShortestPathLength();
	    public double getDiameter();
	    public double getRadius();
	    public double getClusterCoefficient();


	    public boolean containsCycles() throws LexicalSemanticResourceException;
        public void removeCycles() throws LexicalSemanticResourceException;

	    public List<String> getPageRank();
	    public List<String> getHITS();

	    public Set<Set<Entity>> getStructuralEquivalences();

	}