/*******************************************************************************
 * Copyright 2016
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

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.util.LoggingUtils;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import edu.uci.ics.jung.algorithms.blockmodel.StructurallyEquivalent;
import edu.uci.ics.jung.algorithms.blockmodel.VertexPartition;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.algorithms.scoring.HITSWithPriors;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.util.SelfLoopEdgePredicate;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Context;

/**
 * A graph constructed from all entities in a lexical semantic resource. The
 * graph is directed but some of the algorithms (for eccentricity and related)
 * run on its undirected version.
 *
 * @author Tristan Miller
 * @author garoufi
 */
public class EntityGraphJUNG
	implements EntityGraph
{
	private final Log logger = LogFactory.getLog(getClass());

	protected DirectedGraph<Entity, EntityGraphEdge> directedGraph;
	protected UndirectedGraph<Entity, EntityGraphEdge> undirectedGraph;

	// a map holding the degree distribution of the graph
	private final Map<Integer, Integer> degreeDistribution = new HashMap<Integer, Integer>();

	// a map from nodes to their eccentricity values
	private final Map<Entity, Integer> eccentricityMap = new HashMap<Entity, Integer>();

	private String graphId;

	private double averageShortestPathLength = Double.NEGATIVE_INFINITY;
	private double diameter = Double.NEGATIVE_INFINITY;
	private double averageDegree = Double.NEGATIVE_INFINITY;
	private double clusterCoefficient = Double.NEGATIVE_INFINITY;
	private final double depth = Double.NEGATIVE_INFINITY;

	private LexicalSemanticResource lexSemRes;

	private static Comparator<Set<Entity>> sortListBySizeDescending = new Comparator<Set<Entity>>()
	{
		@Override
		public int compare(Set<Entity> s1, Set<Entity> s2)
		{
			return (s1.size() < s2.size() ? 1 : (s1.size() == s2.size() ? 0
					: -1));
		}
	};

	protected EntityGraphJUNG getEntityGraphJUNG(
			LexicalSemanticResource lexSemResource)
		throws LexicalSemanticResourceException
	{

		lexSemRes = lexSemResource;

		graphId = lexSemResource.getResourceName() + "_"
				+ lexSemResource.getResourceVersion();

		logger.info("Creating entity graph.");
		createGraph(lexSemResource);
		logger.info("Finished creating entity graph.");

		logger.info("The graph is simple: " + isSimple(directedGraph));
		logger.info("The graph is connected: " + isConnected(directedGraph));
		logger.info("The graph contains parallel edges: "
				+ containsParallelEdges(directedGraph));
		logger.info("The graph contains loops: "
				+ containsSelfLoops(directedGraph));

		return this;
	}

	protected EntityGraphJUNG getEntityGraphJUNG(
			LexicalSemanticResource lexSemResource,
			Set<Set<Entity>> vertexCluster)
		throws LexicalSemanticResourceException
	{

		logger.info("Creating LCC.");
		createGraph(lexSemResource, vertexCluster);
		logger.info("Finished creating LCC.");

		return this;
	}

	/**
	 * This algorithm measures the importance of a node in terms of the fraction
	 * of time spent at that node relative to all other nodes.
	 *
	 * @return A list of nodes with their PageRank rankings in descending order.
	 */
	@Override
	public List<String> getPageRank()
	{

		logger.info("Getting PageRank...");
		PageRank<Entity, EntityGraphEdge> ranker = new PageRank<Entity, EntityGraphEdge>(
				directedGraph, 0.15);
		logger.info("Ranker was created.");
		logger.info("Evaluating...");
		ranker.evaluate();

		logger.info("Got PageRank.");
		Collection<Entity> vertices = directedGraph.getVertices();
		List<String> rankingList = new ArrayList<String>();
		Format formatter = new DecimalFormat("%7.6f");

		for (Entity vertex : vertices) {
			rankingList.add(formatter.format(ranker.getVertexScore(vertex))
					+ " " + vertex);
			// logger.info("PageRanking for " + vertex.getVertexEntity() + " : "
			// + ranker.getRankScore(vertex));
		}

		Collections.sort(rankingList);
		Collections.reverse(rankingList);
		// logger.info("Sorted PageRankings: " + rankingList);
		// ranker.printRankings(false, true);

		return rankingList;
	}

	/**
	 * Calculates the "hubs-and-authorities" importance measures for each node
	 * in a graph. These measures are defined recursively as follows: 1. The
	 * *hubness* of a node is the degree to which a node links to other
	 * important authorities 2. The *authoritativeness* of a node is the degree
	 * to which a node is pointed to by important hubs
	 *
	 * @return A list of nodes with their HITS rankings in descending order.
	 */
	@Override
	public List<String> getHITS()
	{

		logger.info("Getting HITS...");
		HITS<Entity, EntityGraphEdge> ranker = new HITS<Entity, EntityGraphEdge>(
				directedGraph);
		logger.info("Ranker was created.");
		logger.info("Evaluating...");
		ranker.evaluate();

		logger.info("Got HITS.");
		Collection<Entity> vertices = directedGraph.getVertices();
		List<String> rankingList = new ArrayList<String>();
		Format formatter = new DecimalFormat("%7.6f");

		for (Entity vertex : vertices) {
			rankingList.add("hub="
					+ formatter.format(ranker.getVertexScore(vertex).hub)
					+ " authority="
					+ formatter.format(ranker.getVertexScore(vertex).authority)
					+ " " + vertex);
			// logger.info("HITS ranking for " + vertex.getVertexEntity() +
			// " : " + ranker.getRankScore(vertex));
		}

		Collections.sort(rankingList);
		Collections.reverse(rankingList);
		// logger.info("Sorted PageRankings: " + rankingList);
		// ranker.printRankings(false, true);

		return rankingList;
	}

	/**
	 * HITS with priors. Query-specific!
	 */
	public List<String> getHITSWithPriors(
			Transformer<Entity, HITS.Scores> priors)
	{

		logger.info("Getting HITS with priors...");
		HITSWithPriors<Entity, EntityGraphEdge> ranker = new HITSWithPriors<Entity, EntityGraphEdge>(
				directedGraph, priors, 0.3);
		logger.info("Ranker was created.");
		logger.info("Evaluating...");
		ranker.evaluate();
		// ranker.printRankings(true, true);

		logger.info("Got HITS with priors.");
		Collection<Entity> vertices = directedGraph.getVertices();
		List<String> rankingList = new ArrayList<String>();
		Format formatter = new DecimalFormat("%7.6f");

		for (Entity vertex : vertices) {
			rankingList.add("hub="
					+ formatter.format(ranker.getVertexScore(vertex).hub)
					+ " authority="
					+ formatter.format(ranker.getVertexScore(vertex).authority)
					+ " " + vertex);
			// logger.info("HITS with priors ranking for " +
			// vertex.getVertexEntity() + " : " + ranker.getRankScore(vertex));
		}

		Collections.sort(rankingList);
		Collections.reverse(rankingList);
		// logger.info("Sorted PageRankings: " + rankingList);
		// ranker.printRankings(false, true);

		return rankingList;
	}

	/**
	 * Create a graph representation of the lexical semantic resource. The graph
	 * may contain vertices that do not represent an entity in Wiktionary, but
	 * tokens that are linked in an Wiktionary entry without having been
	 * created, yet.
	 *
	 * @param lexSemResource
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 */
	private DirectedGraph<Entity, EntityGraphEdge> createGraph(
			LexicalSemanticResource lexSemResource)
		throws LexicalSemanticResourceException
	{
		// JGraphT: multiple vertices or edges are not allowed in a
		// DefaultDirectedGraph, so we do not have
		// to check for existence before inserting

		directedGraph = new DirectedSparseGraph<Entity, EntityGraphEdge>();
		undirectedGraph = new UndirectedSparseGraph<Entity, EntityGraphEdge>();
		Iterator<Entity> entityIter = lexSemResource.getEntities().iterator();

		while (entityIter.hasNext()) {
			Entity entity = entityIter.next();

			directedGraph.addVertex(entity);
			undirectedGraph.addVertex(entity);

			Set<Entity> children = lexSemResource.getChildren(entity);

			if (children == null) {
				continue;
			}

			for (Entity child : children) {

				EntityGraphEdge edge = directedGraph.findEdge(entity, child);

				if (edge == null) {
					edge = new EntityGraphEdge(entity, child);
					directedGraph.addEdge(edge, entity, child);
				}

				if (!undirectedGraph.containsEdge(edge)) {
					undirectedGraph.addEdge(edge, entity, child);
				}
			}
		}

		// logger.info("The first directed graph " + directedGraph +
		// " was created.");
		return directedGraph;
	}

	/**
	 * Create a graph representation of the lexical semantic resource. The graph
	 * may contain vertices that do not represent an entity in Wiktionary, but
	 * tokens that are linked in an Wiktionary entry without having been
	 * created, yet.
	 *
	 * @param lexSemResource
	 * @param vertexCluster
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 */
	private DirectedGraph<Entity, EntityGraphEdge> createGraph(
			LexicalSemanticResource lexSemResource,
			Set<Set<Entity>> vertexCluster)
		throws LexicalSemanticResourceException
	{

		directedGraph = (DirectedGraph<Entity, EntityGraphEdge>) new VertexPartition<Entity, EntityGraphEdge>(
				directedGraph, vertexCluster).getGraph();
		undirectedGraph = new UndirectedSparseGraph<Entity, EntityGraphEdge>();
		Entity dirStart, dirEnd;

		// logger.info("A new directed graph " + directedGraph +
		// " was created from cluster " + vertexCluster.getCluster(0));
		Collection<Entity> vertices = directedGraph.getVertices();
		Collection<EntityGraphEdge> edges = directedGraph.getEdges();
		// logger.info("New graph's vertices: " + vertices);

		for (Entity vertex : vertices) {
			undirectedGraph.addVertex(vertex);
		}

		for (EntityGraphEdge edge : edges) {
			dirStart = directedGraph.getSource(edge);
			dirEnd = directedGraph.getDest(edge);

			if (undirectedGraph.findEdge(dirStart, dirEnd) == null) {
				undirectedGraph.addEdge(edge, dirStart, dirEnd);
			}
		}

		return directedGraph;
	}

	@Override
	public boolean containsEdge(Entity source, Entity target)
	{

		return directedGraph.findEdge(source, target) == null ? false : true;
	}

	@Override
	public boolean containsVertex(Entity vertex)
	{
		return directedGraph.containsVertex(vertex);
	}

	@Override
	public double getAverageDegree()
	{
		int degreeSum = 0;
		for (Entity e : directedGraph.getVertices()) {
			degreeSum += directedGraph.degree(e);
		}
		return degreeSum / (double) getNumberOfNodes();
	}

	/**
	 * Computes the average of the path lengths of all node pairs The graph is
	 * treated as an undirected graph. Computing graph parameters requires
	 * touching all node pairs. Therefore, if one parameter is called the others
	 * are computed as well and stored for later retrieval.
	 *
	 * @return The average of the shortest path lengths between all pairs of
	 *         nodes.
	 */
	@Override
	public double getAverageShortestPathLength()
	{

		if (averageShortestPathLength < 0) { // has not been initialized
			logger.debug("Calling setGraphParameters");
			setGraphParameters();
			// logger.info("Sum = " + (averageShortestPathLength *
			// (getNumberOfNodes() * (getNumberOfNodes()-1) / 2)));
		}

		return averageShortestPathLength;
	}

	public double getAverageShortestPathLengthJUNG()
	{
		// implementation with JUNG's GraphStatistics
		Transformer<Entity, Double> distanceTransformer = DistanceStatistics
				.averageDistances(undirectedGraph);
		// logger.info("Average distances: " + distances);
		double distanceSum = 0.0;
		for (Entity e : undirectedGraph.getVertices()) {
			distanceSum += distanceTransformer.transform(e);
		}
		return distanceSum / getNumberOfNodes();
	}

	@Override
	public Set<Entity> getCenter()
	{
		Set<Entity> center = new HashSet<Entity>();
		Collection<Entity> vertices = directedGraph.getVertices();
		double radius = getRadius();

		for (Entity node : vertices) {
			if (getEccentricity(node) == radius) {
				center.add(node);
			}
		}
		return center;
	}

	@Override
	public Set<Entity> getChildren(Entity vertex)
	{
		if (containsVertex(vertex)) {
			return new HashSet<Entity>(directedGraph.getSuccessors(vertex));
		}
		else {
			return new HashSet<Entity>();
		}
	}

	/**
	 * Compute the cluster coefficient of the graph (after Watts and Strogatz
	 * 1998) Cluster coefficient C is defined as the average of C_v over all
	 * edges. C_v is the fraction of the connections that exist between the
	 * neighbor nodes (k_v) of a vertex v and all allowable connections between
	 * the neighbors (k_v(k_v -1)/2). C_v = 2 * number of connections between /
	 * k_v*(k_v -1)
	 *
	 * @return The cluster coefficient.
	 */
	@Override
	public double getClusterCoefficient()
	{
		if (clusterCoefficient < 0) { // has not been initialized
			logger.debug("Calling setGraphParameters");
			setGraphParameters();
		}

		return clusterCoefficient;
	}

	public double getClusterCoefficientJUNG()
	{
		// implementation with JUNG's GraphStatistics
		// different definition from Torsten's: for nodes with degree 0 or 1
		// Torsten assigns no CC
		// but JUNG assigns 0 if degree = 0 and 1 if degree = 1
		Map<Entity, Double> coefficients = Metrics
				.clusteringCoefficients(undirectedGraph);
		double coefficientSum = 0.0;
		// logger.info("Clustering coefficients: " + coefficients);
		for (Entity vertex : coefficients.keySet()) {
			// logger.info(vertex + ((JUNGEntityVertex)
			// vertex).getVertexEntity());
			coefficientSum += coefficients.get(vertex);
		}
		return coefficientSum / getNumberOfNodes();
	}

	@Override
	public int getDegree(Entity vertex)
	{
		return getInDegree(vertex) + getOutDegree(vertex);
	}

	@Override
	public Map<Integer, Integer> getDegreeDistribution()
	{
		Integer nodeDegree;

		for (Entity e : directedGraph.getVertices()) {
			nodeDegree = directedGraph.degree(e);

			if (degreeDistribution.containsKey(nodeDegree)) {
				// logger.info("Updating existing key in degree distribution map.");
				degreeDistribution.put(nodeDegree,
						(degreeDistribution.get(nodeDegree) + 1));
			}
			else {
				// logger.info("Creating new key in degree distribution map.");
				degreeDistribution.put(nodeDegree, 1);
			}
		}

		return degreeDistribution;
	}

	/**
	 * Computes the depth of the graph, i.e. the maximum path length starting
	 * with the root node (if a single root exists)
	 *
	 * @return The depth of the hierarchy.
	 */
	@Override
	public double getDepth()
	{

		List<Entity> roots = new Stack<Entity>();
		roots.addAll(getRoots());

		if (roots.size() == 0) {
			logger.error("There is no root for this lexical semantic resource.");
			return Double.NaN;
		}
		else if (roots.size() > 1) {
			logger.error("There are several roots for this lexical semantic resource.");
			return Double.NaN;
		}
		else {
			Entity root = roots.get(0);
			// return getEccentricity(root);

			double maxPathLength = 0.0;
			double[] returnValues = computeShortestPathLengths(root, 0.0,
					maxPathLength, new HashSet<Entity>());
			maxPathLength = returnValues[1];
			return maxPathLength;
		}
	}

	/**
	 * Computes the diameter of the graph (the maximum of the shortest path
	 * length between all pairs of nodes) The graph is treated as an undirected
	 * graph. Computing graph parameters requires touching all node pairs.
	 * Therefore, if one parameter is called the others are computed as well and
	 * stored for later retrieval.
	 *
	 * @return The diameter of the graph.
	 */
	@Override
	public double getDiameter()
	{
		if (diameter < 0) { // has not been initialized
			logger.debug("Calling setGraphParameters");
			setGraphParameters();
		}
		return diameter;
	}

	public double getDiameterJUNG()
	{
		// implementation with JUNG's GraphStatistics
		return DistanceStatistics.diameter(undirectedGraph);
	}

	// The eccentricity of the vertex v is the maximum distance from v to any
	// vertex.
	// That is, e(v) = max{d(v,w):w in V(G)}
	// Torsten's implementation
	@Override
	public double getEccentricity(Entity node)
	{
		if (eccentricityMap == null) {
			logger.debug("Calling setGraphParameters");
			setGraphParameters();

			return eccentricityMap.get(node);
		}
		else if (!eccentricityMap.containsKey(node)) {
			return Double.NaN;
		}
		else {
			return eccentricityMap.get(node);
		}
	}

	// JUNG's implementation
	public double getEccentricityJUNG(Entity source)
	{
		Double distance = 0.0;
		double maxDistance = 0.0;
		Collection<Entity> vertices = undirectedGraph.getVertices();
		Entity entSource = source;

		// Create an instance of DijkstraShortestPath which caches results
		// locally.
		// Distances thus calculated may be invalidated by changes to the graph,
		// in which case reset() should be
		// invoked so that the distances are recalculated.
		DijkstraDistance<Entity, EntityGraphEdge> dijkstra = new DijkstraDistance<Entity, EntityGraphEdge>(
				undirectedGraph, false);

		// logger.info("Current source vertex: " + source +
		// stringToVertex.get(source));

		int progress = 0;
		for (Entity target : vertices) {

			// logger.info("Current target vertex: " + target.getVertexEntity()
			// + target);
			distance = (Double) dijkstra.getDistance(entSource, target);
			if (distance != null && distance > maxDistance) {
				maxDistance = distance;
			}

			progress++;
			LoggingUtils.printProgressInfo(progress, vertices.size(), 100,
					LoggingUtils.ProgressInfoMode.TEXT,
					"Getting JUNG eccentricities.");

			// if (getShortestPathUndirected(source, target.getVertexEntity())
			// != null) {
			// distance = getShortestPathLengthUndirected(source,
			// target.getVertexEntity());
			// if (distance > maxDistance) {
			// maxDistance = distance;
			// }
			// }
		}

		// logger.info("Eccentricity for node " + source + " : " + maxDistance);
		return maxDistance;
	}

	@Override
	public Iterable<EntityGraphEdge> getEdges()
	{
		// TODO Auto-generated method stub
		// do we need two separate objects JUNGEntityEdgeIterable and
		// JUNGEntityEdgeIterator?
		// Iterator<EntityGraphEdge> edgeIter =
		// directedGraph.getEdges().iterator();
		return null;
	}

	public Set<String> getHyponyms(String vertex)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Map<Entity, Integer> computeHyponymCountMap()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInDegree(Entity vertex)
	{
		if (containsVertex(vertex)) {
			return directedGraph.inDegree(vertex);
			// if there is no such node define the indegree as -1
		}
		else {
			return -1;
		}
	}

	@Override
	public Set<Entity> getIsolatedNodes()
	{
		Set<Entity> isolatedNodes = new HashSet<Entity>();
		Collection<Entity> vertices = directedGraph.getVertices();
		for (Entity node : vertices) {
			if (directedGraph.degree(node) == 0) {
				isolatedNodes.add(node);
			}
		}
		return isolatedNodes;
	}

	@Override
	public EntityGraph getLargestConnectedComponent()
		throws LexicalSemanticResourceException
	{

		WeakComponentClusterer<Entity, EntityGraphEdge> clusterer = new WeakComponentClusterer<Entity, EntityGraphEdge>();
		Set<Entity> largestComponent, secondLargestComp;

		List<Set<Entity>> clusters = new ArrayList<Set<Entity>>(
				clusterer.transform(directedGraph));
		logger.info("Number of connected components: " + clusters.size());

		Collections.sort(clusters, sortListBySizeDescending);
		largestComponent = clusters.get(0);

		double largestComponentRatio = largestComponent.size() * 100.0
				/ this.getNumberOfNodes();
		logger.info("Largest connected component contains "
				+ largestComponentRatio + "% (" + largestComponent.size() + "/"
				+ this.getNumberOfNodes() + ") of the nodes in the graph.");

		if (clusters.size() > 1) {

			secondLargestComp = clusters.get(1);
			double secondLargestCompRatio = secondLargestComp.size() * 100
					/ this.getNumberOfNodes();
			logger.info("Second largest connected component contains "
					+ secondLargestCompRatio + "% (" + secondLargestComp.size()
					+ "/" + this.getNumberOfNodes()
					+ ") of the nodes in the graph.");
		}

		EntityGraphJUNG entGraph = getEntityGraphJUNG(lexSemRes,
				new HashSet<Set<Entity>>(clusters));
		directedGraph = entGraph.directedGraph;
		undirectedGraph = entGraph.undirectedGraph;
		// entityToVertex = entGraph.entityToVertex;

		logger.info("The LCC is simple: " + isSimple(directedGraph));
		logger.info("The LCC is connected: " + isConnected(directedGraph));
		logger.info("The LCC contains parallel edges: "
				+ containsParallelEdges(directedGraph));
		logger.info("The LCC contains loops: "
				+ containsSelfLoops(directedGraph));

		return entGraph;
	}

	private static <V, E> boolean isConnected(Graph<V, E> g)
	{
		WeakComponentClusterer<V, E> clusterer = new WeakComponentClusterer<V, E>();
		return (clusterer.transform(g).size() == 1);
	}

	private static <V, E> boolean isSimple(Graph<V, E> g)
	{
		return (containsParallelEdges(g) == false)
				&& (containsSelfLoops(g) == false);
	}

	private static <V, E> boolean containsParallelEdges(Graph<V, E> g)
	{
		for (V v1 : g.getVertices()) {
			for (V v2 : g.getVertices()) {
				Collection<E> edges = g.findEdgeSet(v1, v2);
				if (edges == null) {
					continue;
				}
				if (edges.size() > 1) {
					return true;
				}
			}
		}
		return false;
	}

	private static <V, E> boolean containsSelfLoops(Graph<V, E> g)
	{
		SelfLoopEdgePredicate<V, E> s = new SelfLoopEdgePredicate<V, E>();
		for (E edge : g.getEdges()) {
			if (s.evaluate(Context.getInstance(g, edge))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Entity> getLeaves()
	{
		Set<Entity> leafNodes = new HashSet<Entity>();
		Collection<Entity> vertices = directedGraph.getVertices();
		for (Entity node : vertices) {
			if (directedGraph.outDegree(node) == 0) {
				leafNodes.add(node);
			}
		}
		return leafNodes;
	}

	@Override
	public Set<Entity> getNeighbors(Entity vertex)
	{
		Set<Entity> union = new HashSet<Entity>(getChildren(vertex));
		union.addAll(getParents(vertex));
		return union;
	}

	@Override
	public Iterable<Entity> getNodes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfEdges()
	{
		return directedGraph.getEdgeCount();
	}

	@Override
	public int getNumberOfIsolatedNodes()
	{
		return getIsolatedNodes().size();
	}

	@Override
	public int getNumberOfLeaves()
	{
		return getLeaves().size();
	}

	@Override
	public int getNumberOfNodes()
	{
		return directedGraph.getVertexCount();

	}

	@Override
	public int getNumberOfRoots()
	{
		return getRoots().size();
	}

	// pitfall: this currently doesn't work correctly for RWs
	@Override
	public int getNumberOfSymmetricLinks()
	{
		Collection<Entity> vertices = directedGraph.getVertices();
		int symLinksSum = 0;

		// this can be done still more efficiently if i put the nodes in a list
		// and remove from the
		// list current node as well as its children after each loop
		// int progress = 0;
		// int max = vertices.size();

		for (Entity source : vertices) {
			// ApiUtilities.printProgressInfo(progress, max, 100,
			// ApiUtilities.ProgressInfoMode.TEXT, "Counting symmetric links");
			for (Entity target : getChildren(source)) {
				// progress++;

				if (isSymmetricLink(source, target)) {
					symLinksSum++;
				}
			}
		}
		return symLinksSum / 2;
	}

	@Override
	public int getOutDegree(Entity vertex)
	{
		if (containsVertex(vertex)) {
			return directedGraph.outDegree(vertex);
			// if there is no such node define the indegree as -1
		}
		else {
			return -1;
		}
	}

	@Override
	public Set<Entity> getParents(Entity vertex)
	{
		if (containsVertex(vertex)) {
			return new HashSet<Entity>(directedGraph.getPredecessors(vertex));
		}
		else {
			return new HashSet<Entity>();
		}
	}

	@Override
	public double getRadius()
	{
		double currEcc = Double.POSITIVE_INFINITY;
		double minEcc = Double.POSITIVE_INFINITY;
		Collection<Entity> vertices = undirectedGraph.getVertices();

		for (Entity vertex : vertices) {
			// logger.info("Current vertex: " + vertex.getVertexEntity() +
			// vertex);
			currEcc = getEccentricity(vertex);
			if (currEcc < minEcc) {
				minEcc = currEcc;
			}
		}
		return minEcc;
	}

	@Override
	public Set<Entity> getRoots()
	{
		Set<Entity> rootNodes = new HashSet<Entity>();
		Collection<Entity> vertices = directedGraph.getVertices();
		for (Entity node : vertices) {
			if (getInDegree(node) == 0) {
				rootNodes.add(node);
			}
		}
		return rootNodes;
	}

	@Override
	public List<Entity> getShortestPath(Entity source, Entity target,
			DirectionMode mode)
	{

		// if source == target than return a list with just one entity
		if (source.equals(target)) {
			List<Entity> resultList = new ArrayList<Entity>();
			resultList.add(source);
			return resultList;
		}

		Graph<Entity, EntityGraphEdge> graph;
		if (mode.equals(DirectionMode.directed)) {
			graph = directedGraph;
		}
		else {
			graph = undirectedGraph;
		}

		if (containsVertex(source) && containsVertex(target)
				&& !source.equals(target)) {

			// Create an instance of DijkstraShortestPath for the directed graph
			// which caches results locally.
			// Distances thus calculated may be invalidated by changes to the
			// graph, in which case reset() should be
			// invoked so that the distances are recalculated.
			DijkstraShortestPath<Entity, EntityGraphEdge> dijkstraPath = new DijkstraShortestPath<Entity, EntityGraphEdge>(
					graph, true);
			List<EntityGraphEdge> edgeList = dijkstraPath.getPath(source,
					target);
			List<Entity> entityList = new ArrayList<Entity>();

			for (int i = 0; i < edgeList.size(); i++) {
				if (i == 0) {
					Entity fromNode = edgeList.get(i).getSource();
					Entity toNode = edgeList.get(i).getTarget();
					entityList.add(fromNode);
					entityList.add(toNode);
				}
				else {
					Entity toNode = edgeList.get(i).getTarget();
					entityList.add(toNode);
				}
			}

			return entityList;
		}
		// if either source or target are not contained in the graph define the
		// path as null
		// this is different than the empty path, which occurs if source =
		// target
		else {
			return null;
		}
	}

	@Override
	public double getShortestPathLength(Entity source, Entity target,
			DirectionMode mode)
	{
		List<Entity> path = getShortestPath(source, target, mode);
		if (path != null) {
			return path.size() - 1; // we need to decrease by one, as we want to
									// return the distance in edges, not nodes
		}
		else {
			return Double.POSITIVE_INFINITY;
		}
	}

	// pitfall: this currently doesn't work correctly for RWs
	@Override
	public boolean isSymmetricLink(Entity source, Entity target)
	{
		return (directedGraph.findEdge(source, target) != null)
				&& (directedGraph.findEdge(target, source) != null);
	}

	// JUNG does not support cycle detection, so the code for this method
	// will have to be written manually.
	@Override
	public boolean containsCycles()
		throws LexicalSemanticResourceException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeCycles()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Computes the shortest path from node to all other nodes. Paths to nodes
	 * that have already been the source of the shortest path computation are
	 * omitted (the path was already added to the path sum). Updates the sum of
	 * shortest path lengths and the diameter of the graph. As the JGraphT
	 * BreadthFirstIterator does not provide information about the distance to
	 * the start node in each step, we will use our own BFS implementation.
	 *
	 * @param pStartNode
	 *            The start node of the search.
	 * @param pShortestPathLengthSum
	 *            The sum of the shortest path lengths.
	 * @param pMaxPathLength
	 *            The maximum path length found so far.
	 * @param pWasSource
	 *            A set of nodes which have been the start node of the
	 *            computation process. For such nodes all path lengths have been
	 *            already computed.
	 * @return An array of double values. The first value is the
	 *         shortestPathLengthSum The second value is the maxPathLength They
	 *         are returned as an array for performance reasons. I do not want
	 *         to create an object, as this function is called *very* often.
	 */
	private double[] computeShortestPathLengths(Entity pStartNode,
			double pShortestPathLengthSum, double pMaxPathLength,
			Set<Entity> pWasSource)
	{

		int pStartNodeMaxPathLength = 0;

		// a set of nodes that have already been expanded -> algorithm should
		// expand nodes monotonically and not go back
		Set<Entity> alreadyExpanded = new HashSet<Entity>();

		// a queue holding the newly discovered nodes with their and their
		// distance to the start node
		List<Entity[]> queue = new ArrayList<Entity[]>();

		// initialize queue with start node
		Entity[] innerList = new Entity[2];
		innerList[0] = pStartNode; // the node
		innerList[1] = new Entity("0"); // the distance to the start node
		queue.add(innerList);

		// while the queue is not empty
		while (!queue.isEmpty()) {
			// remove first element from queue
			Entity[] queueElement = queue.get(0);
			Entity currentNode = queueElement[0];
			Entity distance = queueElement[1];
			queue.remove(0);

			// if the node was not already expanded
			if (!alreadyExpanded.contains(currentNode)) {

				// the node gets expanded now
				alreadyExpanded.add(currentNode);

				// if the node was a source node in a previous run, we already
				// have added this path
				if (!pWasSource.contains(currentNode)) {
					// add the distance of this node to shortestPathLengthSum
					// check if maxPathLength must be updated
					int tmpDistance = new Integer(distance.getFirstLexeme());
					pShortestPathLengthSum += tmpDistance;
					if (tmpDistance > pMaxPathLength) {
						pMaxPathLength = tmpDistance;
					}
				}
				// even if the node was a source node in a previous run there
				// can be a path to
				// other nodes over this node, so go on:

				// get the neighbors of the queue element
				Set<Entity> neighbors = getNeighbors(currentNode);

				// iterate over all neighbors
				for (Entity neighbor : neighbors) {
					// if the node was not already expanded
					if (!alreadyExpanded.contains(neighbor)) {
						// add the node to the queue, increase node distance by
						// one
						Entity[] tmpList = new Entity[2];
						tmpList[0] = neighbor;
						Integer tmpDistance = new Integer(
								distance.getFirstLexeme()) + 1;
						tmpList[1] = new Entity(tmpDistance.toString());
						queue.add(tmpList);
					}
				}
			}
			pStartNodeMaxPathLength = new Integer(distance.getFirstLexeme());
		}
		eccentricityMap.put(pStartNode, pStartNodeMaxPathLength);

		double returnArray[] = { pShortestPathLengthSum, pMaxPathLength };
		return returnArray;
	}

	/**
	 * Computes and sets the diameter, the average degree and the average
	 * shortest path length of the graph. Do not call this in the constructor.
	 * May run a while. It is called in the getters, if parameters are not yet
	 * initialized when retrieved.
	 */
	private void setGraphParameters()
	{

		logger.info("Setting graph parameters.");
		logger.info("Treating the graph as undirected.");

		// Diameter is the maximum of all shortest path lengths
		// Average shortest path length is (as the name says) the average of the
		// shortest path length between all node pairs

		double maxPathLength = 0.0;
		double shortestPathLengthSum = 0.0;
		double degreeSum = 0.0;
		double clusterCoefficientSum = 0.0;

		// iterate over all node pairs
		Collection<Entity> nodes = undirectedGraph.getVertices();

		// a hashset of the nodes which have been the start node of the
		// computation process
		// for such nodes all path lengths have been already computed
		Set<Entity> wasSource = new HashSet<Entity>();

		int progress = 0;
		for (Entity node : nodes) {

			progress++;
			LoggingUtils.printProgressInfo(progress, nodes.size(), 100,
					LoggingUtils.ProgressInfoMode.TEXT,
					"Getting graph parameters");

			int nodeDegree = getDegree(node);
			// degreeSum += nodeDegree;

			// logger.info("Updating degree distribution.");
			// updateDegreeDistribution(nodeDegree);

			// cluster coefficient C_v of a node v is the fraction of the
			// connections that exist between the
			// neighbor nodes (k_v) of this node and all allowable connections
			// between the neighbors (k_v(k_v -1)/2)
			// for degrees 0 or 1 there is no cluster coefficient, as there can
			// be no connections between neighbors
			if (nodeDegree > 1) {
				double numberOfNeighborConnections = getNumberOfNeighborConnections(node);
				clusterCoefficientSum += (numberOfNeighborConnections / (nodeDegree * (nodeDegree - 1)));
			}

			// Returns the new shortestPathLengthSum and the new maxPathLength.
			// They are returned as an double array for performance reasons.
			// I do not want to create an object, as this function is called
			// *very* often
			// logger.info("Computing shortest path lengths.");
			double[] returnValues = computeShortestPathLengths(node,
					shortestPathLengthSum, maxPathLength, wasSource);
			shortestPathLengthSum = returnValues[0];
			maxPathLength = returnValues[1];

			// save the info that the node was already used as the source of
			// path computation
			wasSource.add(node);
		}

		if (nodes.size() > 1) {
			this.averageShortestPathLength = shortestPathLengthSum
					/ (nodes.size() * (nodes.size() - 1) / 2);
			// sum of path lengths / (number of node pairs)
		}
		else {
			this.averageShortestPathLength = 0; // there is only one node
		}
		this.diameter = maxPathLength;
		this.averageDegree = degreeSum / nodes.size();
		this.clusterCoefficient = clusterCoefficientSum / nodes.size();
	}

	/**
	 * Get the number of connections that exist between the neighbors of a node.
	 *
	 * @param node
	 *            The node under consideration.
	 * @return The number of connections that exist between the neighbors of
	 *         node.
	 */
	private int getNumberOfNeighborConnections(Entity node)
	{

		// The directed graph is treated as a undirected graph to compute these
		// parameters.
		// UndirectedGraph<String, DefaultEdge> undirectedGraph = new
		// AsUndirectedGraph<String, DefaultEdge>(directedGraph);

		int numberOfConnections = 0;

		// get the set of neighbors
		Set<Entity> neighbors = getNeighbors(node);

		if (neighbors.size() > 0) {
			// for each pair of neighbors, test if there is a connection
			Object[] nodeArray = neighbors.toArray();
			// sort the Array so we can use a simple iteration with two for
			// loops to access all pairs
			Arrays.sort(nodeArray);

			for (int i = 0; i < neighbors.size(); i++) {
				Entity outerNode = (Entity) nodeArray[i];
				for (int j = i + 1; j < neighbors.size(); j++) {
					Entity innerNode = (Entity) nodeArray[j];
					// in case of a connection increase connection counter
					// order of the nodes doesn't matter for undirected graphs

					// check if the neighbors are connected:
					if (containsEdge(innerNode, outerNode)
							|| containsEdge(outerNode, innerNode)) {
						// logger.info("There is a connection between the neighbors");
						numberOfConnections++;
					}
				}
			}
		}

		// logger.info(neighbors.size() + " - " + numberOfConnections);

		return numberOfConnections;
	}

	public Set<Entity> getHyponyms(Entity vertex)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Checks a graph for sets of structurally equivalent vertices: vertices
	 * that share all the same edges. Specifically, In order for a pair of
	 * vertices i and j to be structurally equivalent, the set of i 's neighbors
	 * must be identical to the set of j 's neighbors, with the exception of i
	 * and j themselves. This algorithm finds all sets of equivalent vertices in
	 * O(V^2) time.
	 *
	 * @return A set of structurally equivalent node sets.
	 */
	@Override
	public Set<Set<Entity>> getStructuralEquivalences()
	{
		StructurallyEquivalent<Entity, EntityGraphEdge> equivalent = new StructurallyEquivalent<Entity, EntityGraphEdge>();
		VertexPartition<Entity, EntityGraphEdge> vertexPartition = equivalent
				.transform(directedGraph);

		return new HashSet<Set<Entity>>(vertexPartition.getVertexPartitions());
	}

	/**
	 * An algorithm for computing clusters (community structure) in graphs based
	 * on edge betweenness. [Note: The betweenness of an edge measures the
	 * extent to which that edge lies along shortest paths between all pairs of
	 * nodes.] Edges which are least central to communities are progressively
	 * removed until the communities have been adequately separated.
	 *
	 * @param ratioEdgesToRemove
	 *            The ratio of the total number of edges that we want to remove.
	 *            E.g., 0.2 for removing 20% of all edges in the graph.
	 * @return
	 */
	public Set<Set<Entity>> getEdgeBetweennessClusters(double ratioEdgesToRemove)
	{
		int numEdgesToRemove = (int) (getNumberOfEdges() * ratioEdgesToRemove);
		EdgeBetweennessClusterer<Entity, EntityGraphEdge> betweenClusterer = new EdgeBetweennessClusterer<Entity, EntityGraphEdge>(
				numEdgesToRemove);

		List<Set<Entity>> clusters = new ArrayList<Set<Entity>>(
				betweenClusterer.transform(directedGraph));
		logger.info("Number of edge-betweenness clusters: " + clusters.size());
		Collections.sort(clusters, sortListBySizeDescending);

		Iterator<Set<Entity>> clusterIter = clusters.iterator();
		Set<Set<Entity>> clusterSet = new HashSet<Set<Entity>>();

		while (clusterIter.hasNext()) {
			Set<Entity> nodeCluster = clusterIter.next();
			Set<Entity> entCluster = new HashSet<Entity>();
			for (Entity node : nodeCluster) {
				entCluster.add(node);
			}
			logger.info("Cluster's size: " + entCluster.size());
			clusterSet.add(entCluster);
		}
		return clusterSet;
	}

	@Override
	public String getGraphId()
	{
		return graphId;
	}

	@Override
	public Entity getLCS(Entity root, Entity e1, Entity e2)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getIntrinsicInformationContent(Entity e)
	{
		throw new UnsupportedOperationException();
	}
}
