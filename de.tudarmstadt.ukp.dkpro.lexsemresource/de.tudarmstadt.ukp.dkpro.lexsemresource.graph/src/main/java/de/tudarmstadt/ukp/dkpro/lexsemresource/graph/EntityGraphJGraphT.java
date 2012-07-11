package de.tudarmstadt.ukp.dkpro.lexsemresource.graph;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.util.LoggingUtils;
import de.tudarmstadt.ukp.dkpro.lexsemresource.core.util.ProgressMeter;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;

/**
 * A graph constructed from all entities in a lexical semantic resource.
 *
 * @author zesch
 *
 */
public class EntityGraphJGraphT
	implements EntityGraph
{
	private final Log logger = LogFactory.getLog(getClass());

	// the maximum depth of a path from a node to root
	// if no path was found after expanding to that depth, it is assumed that there is no path
	// should be higher than the deepest resource
	private static final int MAX_EXPANSION_DEPTH = 18;

	protected DirectedGraph<Entity, DefaultEdge> directedGraph;
	protected UndirectedGraph<Entity, DefaultEdge> undirectedGraph;

	// a map holding the degree distribution of the graph
	private final Map<Integer, Integer> degreeDistribution = new HashMap<Integer, Integer>();

	// a map from nodes to their eccentricity values
	private final Map<Entity, Integer> eccentricityMap = new HashMap<Entity, Integer>();

	// A map holding the (recursive) number of hyponyms for each node.
	// Recursive means that the hyponyms of hyponyms are also taken into account.
	private Map<String, Integer> hyponymCountMap;
	private final String hyponymCountMapFilename = "hypoCountMap";
	private boolean hyponymCountMapUseLcc = true;

	// a mapping from all nodes to a list of nodes on the path to the root
	private Map<String, List<String>> rootPathMap = null;
	private final String rootPathMapFilename = "rootPathMap";

	private File serializedGraphFile;
	private String graphId;

	private CycleHandlerJGraphT cycleHandler;

	private double averageShortestPathLength = Double.NEGATIVE_INFINITY;
	private double diameter = Double.NEGATIVE_INFINITY;
	private double clusterCoefficient = Double.NEGATIVE_INFINITY;
	private double depth = Double.NEGATIVE_INFINITY;

	private LexicalSemanticResource lexSemRes;

	protected EntityGraphJGraphT getEntityGraphJGraphT(LexicalSemanticResource aLsr)
		throws LexicalSemanticResourceException
	{
		return getEntityGraphJGraphT(aLsr, aLsr.getEntities(), "", aLsr.getNumberOfEntities());
	}

	protected EntityGraphJGraphT getEntityGraphJGraphT(LexicalSemanticResource lexSemResource,
			Iterable<Entity> nodesToConsider, String nameSuffix, int numEntities)
		throws LexicalSemanticResourceException
	{

		lexSemRes = lexSemResource;
		cycleHandler = new CycleHandlerJGraphT(this);

		graphId = "graphSer_" + lexSemResource.getResourceName() + nameSuffix + "_"
				+ lexSemResource.getResourceVersion();

		String defaultSerializedGraphLocation = graphId;
		serializedGraphFile = new File(defaultSerializedGraphLocation);
		if (serializedGraphFile.exists()) {
			try {
				logger.info("Loading entity graph: " + serializedGraphFile.getAbsolutePath());
				directedGraph = GraphSerialization.loadGraph(defaultSerializedGraphLocation);
				undirectedGraph = new AsUndirectedGraph<Entity, DefaultEdge>(directedGraph);
				logger.info("Finished loading entity graph.");
			}
			catch (IOException e) {
				throw new LexicalSemanticResourceException(e);
			}
			catch (ClassNotFoundException e) {
				throw new LexicalSemanticResourceException(e);
			}
		}
		else {
			logger.info("Creating entity graph.");
			directedGraph = createGraph(lexSemResource, nodesToConsider, numEntities);
			undirectedGraph = new AsUndirectedGraph<Entity, DefaultEdge>(directedGraph);
			logger.info("Finished creating entity graph.");

			try {
				GraphSerialization.saveGraph(directedGraph, serializedGraphFile);
			}
			catch (IOException e) {
				throw new LexicalSemanticResourceException(e);
			}
		}

		containsCycles();

		return this;
	}

	/**
	 * Create a graph representation of the lexical semantic resource. The graph may contain
	 * vertices that do not represent an entity in Wiktionary, but tokens that are linked in an
	 * Wiktionary entry without having been created, yet.
	 *
	 * @param lexSemResource
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 */
	private DirectedGraph<Entity, DefaultEdge> createGraph(LexicalSemanticResource lexSemResource,
			Iterable<Entity> entitiesToConsider, int numEntities)
		throws LexicalSemanticResourceException
	{
		DirectedGraph<Entity, DefaultEdge> graph = new DefaultDirectedGraph<Entity, DefaultEdge>(
				DefaultEdge.class);
		ProgressMeter progress = new ProgressMeter(numEntities);
		for (Entity entity : entitiesToConsider) {
			if (!graph.containsVertex(entity)) {
				graph.addVertex(entity);
			}

			Set<Entity> children = lexSemResource.getChildren(entity);
			if (children != null) {
				for (Entity child : children) {
					if (!graph.containsVertex(child)) {
						graph.addVertex(child);
					}
					if (!graph.containsEdge(entity, child) && !entity.equals(child)) {
						graph.addEdge(entity, child);
					}
				}
			}

			// sometimes an API does not deliver fully symmetric relations, thus check parents, but
			// also check for already existing edges
			Set<Entity> parents = lexSemResource.getParents(entity);
			if (parents != null) {
				for (Entity parent : parents) {
					if (!graph.containsVertex(parent)) {
						graph.addVertex(parent);
					}
					if (!graph.containsEdge(parent, entity) && !entity.equals(parent)) {
						graph.addEdge(parent, entity);
					}
				}
			}

			progress.next();
			if (logger.isDebugEnabled()) {
				logger.debug(progress);
			}
			else if (logger.isInfoEnabled() && (progress.getCount() % 100 == 0)) {
				logger.info(progress);
			}
		}
		return graph;
	}

	/**
	 * @return Returns the graph.
	 * @throws LexicalSemanticResourceException
	 */
	public DirectedGraph<Entity, DefaultEdge> getGraph()
		throws LexicalSemanticResourceException
	{
		return directedGraph;
	}

	@Override
	public int getNumberOfNodes()
	{
		return directedGraph.vertexSet().size();
	}

	@Override
	public int getNumberOfEdges()
	{
		return directedGraph.edgeSet().size();
	}

	@Override
	public boolean containsVertex(Entity vertex)
	{
		return directedGraph.containsVertex(vertex);
	}

	@Override
	public boolean containsEdge(Entity source, Entity target)
	{
		return directedGraph.containsEdge(source, target);
	}

	@Override
	public Set<Entity> getChildren(Entity vertex)
	{
		Set<Entity> outLinks = new HashSet<Entity>();
		if (directedGraph.containsVertex(vertex)) {
			Set<DefaultEdge> outgoingEdges = directedGraph.outgoingEdgesOf(vertex);
			for (DefaultEdge edge : outgoingEdges) {
				outLinks.add(directedGraph.getEdgeTarget(edge));
			}
		}
		else {
			logger.info("Graph does not contain vertex " + vertex);
		}
		return outLinks;
	}

	@Override
	public Set<Entity> getParents(Entity vertex)
	{
		Set<Entity> inLinks = new HashSet<Entity>();
		if (directedGraph.containsVertex(vertex)) {
			Set<DefaultEdge> incomingEdges = directedGraph.incomingEdgesOf(vertex);
			for (DefaultEdge edge : incomingEdges) {
				inLinks.add(directedGraph.getEdgeSource(edge));
			}
		}
		else {
			logger.info("Graph does not contain vertex " + vertex);
		}
		return inLinks;
	}

	@Override
	public Set<Entity> getNeighbors(Entity vertex)
	{
		Set<Entity> union = new HashSet<Entity>(getChildren(vertex));
		union.addAll(getParents(vertex));
		return union;
	}

	@Override
	public int getInDegree(Entity vertex)
	{
		if (directedGraph.containsVertex(vertex)) {
			return directedGraph.inDegreeOf(vertex);
			// if there is no such node define the indegree as -1
		}
		else {
			return -1;
		}
	}

	@Override
	public int getOutDegree(Entity vertex)
	{
		if (directedGraph.containsVertex(vertex)) {
			return directedGraph.outDegreeOf(vertex);
			// if there is no such node define the outdegree as -1
		}
		else {
			return -1;
		}
	}

	@Override
	public int getDegree(Entity vertex)
	{
		// implementation without JGraphT:
		return getInDegree(vertex) + getOutDegree(vertex);

		// implementation with JGraphT:
		// The directed graph is treated as a undirected graph
		// UndirectedGraph<String, DefaultEdge> undirectedGraph = new AsUndirectedGraph<String,
		// DefaultEdge>(graph);
		// if (undirectedGraph.containsVertex(vertex))
		// return undirectedGraph.degreeOf(vertex);
		// else
		// return -1;
	}

	@Override
	public double getAverageDegree()
	{
		// implementation without JGraphT:
		double degreeSum = 0.0;

		// iterate over all node pairs
		Set<Entity> nodes = directedGraph.vertexSet();
		for (Entity node : nodes) {
			degreeSum += getDegree(node);
		}

		return degreeSum / getNumberOfNodes();
	}

	@Override
	public Set<Entity> getLeaves()
	{
		Set<Entity> leafNodes = new HashSet<Entity>();
		for (Entity node : directedGraph.vertexSet()) {
			if (getOutDegree(node) == 0) {
				leafNodes.add(node);
			}
		}
		return leafNodes;
	}

	@Override
	public int getNumberOfLeaves()
	{
		return getLeaves().size();
	}

	@Override
	public Set<Entity> getRoots()
	{
		Set<Entity> rootNodes = new HashSet<Entity>();
		for (Entity node : directedGraph.vertexSet()) {
			if (getInDegree(node) == 0) {
				rootNodes.add(node);
			}
		}
		return rootNodes;
	}

	@Override
	public int getNumberOfRoots()
	{
		return getRoots().size();
	}

	@Override
	public List<Entity> getShortestPath(Entity source, Entity target, DirectionMode mode)
	{

		// if source == target than return a list with just one entity
		if (source.equals(target)) {
			List<Entity> resultList = new ArrayList<Entity>();
			resultList.add(source);
			return resultList;
		}

		Graph<Entity, DefaultEdge> graph;
		if (mode.equals(DirectionMode.directed)) {
			graph = directedGraph;
		}
		else {
			graph = undirectedGraph;
		}

		// The algorithm runs on the directed version of the graph
		// get the path from source to target
		if (graph.containsVertex(source) && graph.containsVertex(target)) {
			List<DefaultEdge> edgeList = DijkstraShortestPath
					.findPathBetween(graph, source, target);

			// no path between the vertices
			if (edgeList == null) {
				return null;
			}

			List<Entity> resultList = new ArrayList<Entity>();
			for (int i = 0; i < edgeList.size(); i++) {
				if (i == 0) {
					resultList.add(graph.getEdgeSource(edgeList.get(i)));
					resultList.add(graph.getEdgeTarget(edgeList.get(i)));
				}
				else {
					resultList.add(graph.getEdgeTarget(edgeList.get(i)));
				}
			}
			return resultList;
		}
		// if either source or target are not contained in the graph define the path as null
		// this is different than the empty path, which occurs if source = target
		else {
			return null;
		}
	}

	@Override
	public double getShortestPathLength(Entity source, Entity target, DirectionMode mode)
	{
		List<Entity> path = getShortestPath(source, target, mode);
		if (path != null) {
			return path.size() - 1; // we need to decrease by one, as we want to return the distance
									// in edges, not nodes
		}
		else {
			return Double.POSITIVE_INFINITY;
		}
	}

	@Override
	public Iterable<Entity> getNodes()
	{
		return directedGraph.vertexSet();
	}

	@Override
	public Iterable<EntityGraphEdge> getEdges()
	{
		return new JGraphTEntityEdgeIterable(directedGraph);
	}

	@Override
	public boolean isSymmetricLink(Entity source, Entity target)
	{
		if (containsEdge(source, target) && containsEdge(target, source)) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int getNumberOfSymmetricLinks()
	{
		int symLinksSum = 0;

		// this can be done still more efficiently if i put the nodes in a list and remove from the
		// list current node as well as its children after each loop
		int progress = 0;

		Set<Entity> nodes = directedGraph.vertexSet();
		for (Entity source : nodes) {
			for (Entity target : getChildren(source)) {
				progress++;
				// ApiUtilities.printProgressInfo(progress, max, 100,
				// ApiUtilities.ProgressInfoMode.TEXT, "Counting symmetric links");

				if (isSymmetricLink(source, target)) {
					symLinksSum++;
				}
			}
		}
		return symLinksSum / 2;
	}

	@Override
	public Set<Entity> getIsolatedNodes()
	{
		Set<Entity> isolatedNodes = new HashSet<Entity>();
		for (Entity node : directedGraph.vertexSet()) {
			if (getDegree(node) == 0) {
				isolatedNodes.add(node);
			}
		}
		return isolatedNodes;
	}

	@Override
	public int getNumberOfIsolatedNodes()
	{
		return getIsolatedNodes().size();
	}

	/**
	 * Computes the shortest path from node to all other nodes. Paths to nodes that have already
	 * been the source of the shortest path computation are omitted (the path was already added to
	 * the path sum). Updates the sum of shortest path lengths and the diameter of the graph. As the
	 * JGraphT BreadthFirstIterator does not provide information about the distance to the start
	 * node in each step, we will use our own BFS implementation.
	 *
	 * @param pStartNode
	 *            The start node of the search.
	 * @param pShortestPathLengthSum
	 *            The sum of the shortest path lengths.
	 * @param pMaxPathLength
	 *            The maximum path length found so far.
	 * @param pWasSource
	 *            A set of nodes which have been the start node of the computation process. For such
	 *            nodes all path lengths have been already computed.
	 * @return An array of double values. The first value is the shortestPathLengthSum The second
	 *         value is the maxPathLength They are returned as an array for performance reasons. I
	 *         do not want to create an object, as this function is called *very* often.
	 */
	private BigInteger[] computeShortestPathLengths(Entity pStartNode,
			BigInteger pBigShortestPathLengthSum, BigInteger pBigMaxPathLength,
			Set<Entity> pWasSource)
	{

		int pStartNodeMaxPathLength = 0;

		// a set of nodes that have already been expanded -> algorithm should expand nodes
		// monotonically and not go back
		Set<Entity> alreadyExpanded = new HashSet<Entity>();

		// a queue holding the newly discovered nodes and their distance to the start node
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

				// if the node was a source node in a previous run, we already have added this path
				if (!pWasSource.contains(currentNode)) {
					// add the distance of this node to shortestPathLengthSum
					// check if maxPathLength must be updated
					int tmpDistance = new Integer(distance.getFirstLexeme());
					pBigShortestPathLengthSum = pBigShortestPathLengthSum.add(BigInteger
							.valueOf(tmpDistance));

					if (pBigMaxPathLength.compareTo(BigInteger.valueOf(tmpDistance)) == -1) {
						pBigMaxPathLength = BigInteger.valueOf(tmpDistance);

						// logger.info("*TEST TRUE:* pBigShortestPathLengthSum = " +
						// pBigShortestPathLengthSum);
					}
					//
				}
				// even if the node was a source node in a previous run there can be a path to other
				// nodes over this node, so go on

				// get the neighbors of the queue element
				Set<Entity> neighbors = getNeighbors(currentNode);

				// iterate over all neighbors
				for (Entity neighbor : neighbors) {
					// if the node was not already expanded
					if (!alreadyExpanded.contains(neighbor)) {
						// add the node to the queue, increase node distance by one
						Entity[] tmpList = new Entity[2];
						tmpList[0] = neighbor;
						Integer tmpDistance = new Integer(distance.getFirstLexeme()) + 1;
						tmpList[1] = new Entity(tmpDistance.toString());
						queue.add(tmpList);
					}
				}
			}
			pStartNodeMaxPathLength = new Integer(distance.getFirstLexeme());
		}
		eccentricityMap.put(pStartNode, pStartNodeMaxPathLength);

		BigInteger returnArray[] = { pBigShortestPathLengthSum, pBigMaxPathLength };
		return returnArray;
	}

	/**
	 * Computes and sets the diameter, the average degree and the average shortest path length of
	 * the graph. Do not call this in the constructor. May run a while. It is called in the getters,
	 * if parameters are not yet initialized when retrieved.
	 */
	private void setGraphParameters()
	{

		logger.info("Setting graph parameters.");
		// The directed graph is treated as an undirected graph to compute these parameters.
		// UndirectedGraph<String, DefaultEdge> undirectedGraph = new AsUndirectedGraph<String,
		// DefaultEdge>(directedGraph);
		logger.info("Treating the graph as undirected.");

		// Diameter is the maximum of all shortest path lengths
		// Average shortest path length is (as the name says) the average of the shortest path
		// length between all node pairs

		BigInteger bigMaxPathLength = BigInteger.valueOf(0);
		BigInteger bigShortestPathLengthSum = BigInteger.valueOf(0);
		double degreeSum = 0.0;
		double clusterCoefficientSum = 0.0;

		// iterate over all node pairs
		Set<Entity> nodes = undirectedGraph.vertexSet();

		// a hashset of the nodes which have been the start node of the computation process
		// for such nodes all path lengths have been already computed
		Set<Entity> wasSource = new HashSet<Entity>();

		int progress = 0;
		double percent = 0.0;
		for (Entity node : nodes) {

			progress++;
			percent = (double) progress / nodes.size() * 100;

			if (percent % 10 == 0) {
				logger.info("Progress: " + percent);
			}
			LoggingUtils.printProgressInfo(progress, nodes.size(), 100,
					LoggingUtils.ProgressInfoMode.TEXT, "Getting graph parameters");

			int nodeDegree = undirectedGraph.degreeOf(node);
			degreeSum += nodeDegree;

			// logger.info("Updating degree distribution.");
			updateDegreeDistribution(nodeDegree);

			// cluster coefficient C_v of a node v is the fraction of the connections that exist
			// between the
			// neighbor nodes (k_v) of this node and all allowable connections between the neighbors
			// (k_v(k_v -1)/2)
			// for degrees 0 or 1 there is no cluster coefficient, as there can be no connections
			// between neighbors
			if (nodeDegree > 1) {
				double numberOfNeighborConnections = getNumberOfNeighborConnections(node);
				clusterCoefficientSum += (numberOfNeighborConnections / (nodeDegree * (nodeDegree - 1)));
			}

			// Returns the new shortestPathLengthSum and the new maxPathLength.
			// They are returned as an double array for performance reasons.
			// I do not want to create an object, as this function is called *very* often
			// logger.info("Computing shortest path lengths.");
			BigInteger[] returnValues = computeShortestPathLengths(node, bigShortestPathLengthSum,
					bigMaxPathLength, wasSource);
			bigShortestPathLengthSum = returnValues[0];
			bigMaxPathLength = returnValues[1];

			// save the info that the node was already used as the source of path computation
			wasSource.add(node);
		}

		if (nodes.size() > 1) {
			long denominator = nodes.size() * (nodes.size() - 1) / 2;
			this.averageShortestPathLength = bigShortestPathLengthSum.divide(
					BigInteger.valueOf(denominator)).doubleValue();
			// sum of path lengths / (number of node pairs)
		}
		else {
			this.averageShortestPathLength = 0; // there is only one node
		}
		this.diameter = bigMaxPathLength.doubleValue();
		this.clusterCoefficient = clusterCoefficientSum / nodes.size();
	}

	private void updateDegreeDistribution(int nodeDegree)
	{
		if (degreeDistribution.containsKey(nodeDegree)) {
			// logger.info("Updating existing key in degree distribution map.");
			degreeDistribution.put(nodeDegree, (degreeDistribution.get(nodeDegree) + 1));
		}
		else {
			// logger.info("Creating new key in degree distribution map.");
			degreeDistribution.put(nodeDegree, 1);
		}

	}

	/**
	 * Get the number of connections that exist between the neighbors of a node.
	 *
	 * @param node
	 *            The node under consideration.
	 * @return The number of connections that exist between the neighbors of node.
	 */
	private int getNumberOfNeighborConnections(Entity node)
	{

		// The directed graph is treated as a undirected graph to compute these parameters.
		// UndirectedGraph<String, DefaultEdge> undirectedGraph = new AsUndirectedGraph<String,
		// DefaultEdge>(directedGraph);

		int numberOfConnections = 0;

		// get the set of neighbors
		Set<Entity> neighbors = getNeighbors(node);

		if (neighbors.size() > 0) {
			// for each pair of neighbors, test if there is a connection
			Object[] nodeArray = neighbors.toArray();
			// sort the Array so we can use a simple iteration with two for loops to access all
			// pairs
			Arrays.sort(nodeArray);

			for (int i = 0; i < neighbors.size(); i++) {
				Entity outerNode = (Entity) nodeArray[i];
				for (int j = i + 1; j < neighbors.size(); j++) {
					Entity innerNode = (Entity) nodeArray[j];
					// in case of a connection increase connection counter
					// order of the nodes doesn't matter for undirected graphs

					if (undirectedGraph.containsEdge(innerNode, outerNode)) {
						// logger.info("There is a connection between the neighbors");
						numberOfConnections++;
					}
				}
			}
		}

		// logger.info(neighbors.size() + " - " + numberOfConnections);

		return numberOfConnections;
	}

	/**
	 * Computes the average of the path lengths of all node pairs The graph is treated as an
	 * undirected graph. Computing graph parameters requires touching all node pairs. Therefore, if
	 * one parameter is called the others are computed as well and stored for later retrieval.
	 *
	 * @return The average of the shortest path lengths between all pairs of nodes.
	 */
	@Override
	public double getAverageShortestPathLength()
	{

		if (averageShortestPathLength < 0) { // has not been initialized
			logger.debug("Calling setGraphParameters");
			setGraphParameters();
			// logger.info("Sum = " + (averageShortestPathLength * (getNumberOfNodes() *
			// (getNumberOfNodes()-1) / 2)));
		}
		return averageShortestPathLength;

	}

	/**
	 * Computes the diameter of the graph (the maximum of the shortest path length between all pairs
	 * of nodes) The graph is treated as an undirected graph. Computing graph parameters requires
	 * touching all node pairs. Therefore, if one parameter is called the others are computed as
	 * well and stored for later retrieval.
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

	// The eccentricity of the vertex v is the maximum distance from v to any vertex.
	// That is, e(v) = max{d(v,w):w in V(G)}
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

	// The radius of G is the minimum eccentricity among the vertices of G.
	// Therefore, radius(G) = min{e(v):v in V(G)}.
	@Override
	public double getRadius()
	{
		double currEcc = Double.POSITIVE_INFINITY;
		double minEcc = Double.POSITIVE_INFINITY;

		for (Entity node : getNodes()) {
			currEcc = getEccentricity(node);
			if (currEcc < minEcc) {
				minEcc = currEcc;
			}
		}
		return minEcc;
	}

	// The center of G is the set of vertices of eccentricity equal to the radius.
	// Hence, center(G)={v in V(G):e(v)=radius(G)}.
	@Override
	public Set<Entity> getCenter()
	{
		Set<Entity> center = new HashSet<Entity>();
		double radius = getRadius();

		for (Entity node : getNodes()) {
			if (getEccentricity(node) == radius) {
				center.add(node);
			}
		}
		return center;
	}

	/**
	 * Compute the cluster coefficient of the graph (after Watts and Strogatz 1998) Cluster
	 * coefficient C is defined as the average of C_v over all edges. C_v is the fraction of the
	 * connections that exist between the neighbor nodes (k_v) of a vertex v and all allowable
	 * connections between the neighbors (k_v(k_v -1)/2). C_v = 2 * number of connections between /
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

	/**
	 * Computes the degree distribution. The degree of a node is the number of edges that it is
	 * connected with. The graph is treated as an undirected graph. Computing graph parameters
	 * requires touching all node pairs. Therefore, if one is called the others are computed as well
	 * and stored for later retrieval.
	 *
	 * @return A map with the degree distribution of the graph.
	 */
	@Override
	public Map<Integer, Integer> getDegreeDistribution()
	{
		if (degreeDistribution == null || degreeDistribution.size() == 0) { // has not been
																			// initialized
			logger.debug("Calling setGraphParameters");
			setGraphParameters();
		}
		return degreeDistribution;
	}

	/**
	 * @return Returns the largest connected component as a new graph. If the base graph already is
	 *         connected, it simply returns the whole graph.
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 */
	@Override
	public EntityGraphJGraphT getLargestConnectedComponent()
		throws LexicalSemanticResourceException
	{

		ConnectivityInspector<Entity, DefaultEdge> connectInspect = new ConnectivityInspector<Entity, DefaultEdge>(
				directedGraph);

		// if the graph is connected, simply return the whole graph
		if (connectInspect.isGraphConnected()) {
			logger.info("The original graph is connected. Returning this as the LCC.");
			return this;
		}

		// else, get the largest connected component
		List<Set<Entity>> connectedComponentList = connectInspect.connectedSets();

		logger.info(connectedComponentList.size() + " connected components.");

		int i = 0;
		int maxSize = 0;
		Set<Entity> largestComponent = new HashSet<Entity>();
		for (Set<Entity> connectedComponent : connectedComponentList) {
			i++;
			if (connectedComponent.size() > maxSize) {
				maxSize = connectedComponent.size();
				largestComponent = connectedComponent;
			}
		}

		double largestComponentRatio = largestComponent.size() * 100 / this.getNumberOfNodes();
		logger.info("Largest connected component contains " + largestComponentRatio + "% ("
				+ largestComponent.size() + "/" + this.getNumberOfNodes()
				+ ") of the nodes in the graph.");

		return getEntityGraphJGraphT(lexSemRes, largestComponent, "lcc", largestComponent.size());
	}

	/**
	 * Computes the depth of the graph, i.e. the maximum path length starting with the root node (if
	 * a single root exists)
	 *
	 * @return The depth of the hierarchy.
	 * @throws UnsupportedOperationException
	 * @throws LexicalSemanticResourceException
	 */
	private double computeDepth()
		throws LexicalSemanticResourceException
	{
		List<Entity> roots = new Stack<Entity>();
		roots.addAll(getRoots());
		if (roots.size() == 0) {
			logger.error("There is no root for this lexical semantic resource.");
			return Double.NaN;
		}
		else if (roots.size() > 1) {
			logger.warn("There are " + roots.size() + " roots for this lexical semantic resource.");
			logger.info("Trying to get root from underlying lexical semantic resource.");

			Entity root = lexSemRes.getRoot();
			if (root == null) {
				EntityGraph lcc = getLargestConnectedComponent();
				int nrOfLccNodes = lcc.getNumberOfNodes();
				int nrOfGraphNodes = this.getNumberOfNodes();

				double ratio = (double) nrOfLccNodes / (double) nrOfGraphNodes;

				logger.info("Falling back to the depth of the LCC.");

				if (ratio < 0.7) {
					logger.warn("The largest connected component contains only " + ratio * 100
							+ "% of all nodes. Depth might not be meaningful.");
				}

				return lcc.getDepth();
			}
			else {
				roots.clear(); // we know the real root, so remove the others
				roots.add(root);
			}
		}

		Entity root = roots.get(0);
		BigInteger bigMaxPathLength = BigInteger.valueOf(0);
		BigInteger[] returnValues = computeShortestPathLengths(root, BigInteger.ZERO,
				bigMaxPathLength, new HashSet<Entity>());
		bigMaxPathLength = returnValues[1];
		return bigMaxPathLength.doubleValue();

	}

	/**
	 * This parameter is already set in the constructor as it is needed for computation of
	 * relatedness values. Therefore its computation does not trigger setGraphParameters (it is too
	 * slow), even if the depth is implicitly determined there, too.
	 *
	 * @return The depth of the graph, i.e. the maximum path length starting with the root node (if
	 *         a single one exists).
	 * @throws UnsupportedOperationException
	 * @throws LexicalSemanticResourceException
	 */
	@Override
	public double getDepth()
		throws LexicalSemanticResourceException
	{
		if (depth < 0) { // has not been initialized
			logger.info("Computing depth of the hierarchy.");
			depth = computeDepth();
		}
		return depth;
	}

	/**
	 * Gets the lowest common subsumer (LCS) of two nodes. The LCS of two nodes is first node on the
	 * path to the root, that has both nodes as sons. Nodes that are not in the same connected
	 * component as the root node are defined to have no LCS.
	 *
	 * @param root
	 *            The root entity.
	 * @param e1
	 *            The first entity.
	 * @param e2
	 *            The second entity.
	 * @return The lowest common subsumer of the two nodes, or null if there is no LCS.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Entity getLCS(Entity root, Entity e1, Entity e2)
		throws LexicalSemanticResourceException
	{

		if (e1.equals(e2)) {
			return e1;
		}

		// List<Entity> nodeList1 = getShortestPath(root, e1, DirectionMode.undirected);
		// List<Entity> nodeList2 = getShortestPath(root, e2, DirectionMode.undirected);

		List<String> nodeList1 = getRootPathMap().get(e1.getId());
		List<String> nodeList2 = getRootPathMap().get(e2.getId());

		// if one of the paths is null => return null
		if (nodeList1 == null || nodeList2 == null || nodeList1.size() == 0
				|| nodeList2.size() == 0) {
			logger.debug("One of the node lists is null or empty!");
			return null;
		}

		// node 1 subsumes node 2 ?
		for (String tmpNode2 : nodeList2) {
			if (tmpNode2.equals(e1)) {
				return e1;
			}
		}

		// node 2 subsumes node 1 ?
		for (String tmpNode1 : nodeList1) {
			if (tmpNode1.equals(e2)) {
				return e2;
			}
		}

		// // only needed when path is returned by getShortestPath in JGraphT implementation
		// // we need to reverse, otherwise searching starts at the wrong end
		// Collections.reverse(nodeList1);
		// Collections.reverse(nodeList2);

		// they have a lcs ?
		for (String tmpNode1 : nodeList1) {
			for (String tmpNode2 : nodeList2) {
				if (tmpNode1.equals(tmpNode2)) {
					return this.lexSemRes.getEntityById(tmpNode1);
				}
			}
		}

		logger.debug("No lcs found.");

		return null;
	}

	/**
	 * Intrinsic information content (Seco Etal. 2004) allows to compute information content from
	 * the structure of the taxonomy (no corpus needed). IC(n) = 1 - log( hypo(n) + 1) /
	 * log(#entities) hypo(n) is the (recursive) number of hyponyms of a node n. Recursive means
	 * that the hyponyms of hyponyms are also taken into account #entities is the number of entities
	 * in the graph
	 *
	 * @param entity
	 *            The entity node for which the intrinsic information content should be returned.
	 * @return The intrinsic information content for this entity node or 0.0 if the IIC could not be
	 *         computed.
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 */
	@Override
	public double getIntrinsicInformationContent(Entity entity)
		throws LexicalSemanticResourceException
	{
		double intrinsicIC = 0.0;

		Map<String, Integer> hcm = getHyponymCountMap();

		if (!hcm.containsKey(entity.getId())) {
			return intrinsicIC;
		}

		int hyponymCount = hcm.get(entity.getId());
		int numberOfNodes = hcm.size();

		if (hyponymCount > numberOfNodes) {
			throw new LexicalSemanticResourceException(
					"Something is wrong with the hyponymCountMap. " + hyponymCount
							+ " hyponyms, but only " + numberOfNodes + " nodes.");
		}

		logger.debug(entity.toString() + " has " + hyponymCount + " hyponyms.");

		if (hyponymCount >= 0) {
			intrinsicIC = (1 - (Math.log(hyponymCount + 1) / Math.log(numberOfNodes)));
		}
		return intrinsicIC;
	}

	/**
	 * Creates the hyponym map, that maps from nodes to their (recursive) number of hyponyms for
	 * each node. "recursive" means that the hyponyms of hyponyms are also taken into account.
	 *
	 * @throws UnsupportedOperationException
	 * @throws LexicalSemanticResourceException
	 */
	private Map<String, Integer> getHyponymCountMap()
		throws LexicalSemanticResourceException
	{
		// do only create hyponymMap, if it was not already computed
		if (hyponymCountMap != null) {
			return hyponymCountMap;
		}

		// work on the lcc, otherwise this is not going to work
		// EntityGraphJGraphT lcc = this;
		EntityGraphJGraphT lcc = this.getLargestConnectedComponent();
		lcc.removeCycles();
		int nrOfNodes = lcc.getNumberOfNodes();

		File hyponymCountMapSerializedFile = new File(getGraphId() + "_" + hyponymCountMapFilename
				+ (lexSemRes.getIsCaseSensitive() ? "-cs" : "-cis"));
		hyponymCountMap = new HashMap<String, Integer>();

		if (hyponymCountMapSerializedFile.exists()) {
			logger.info("Loading saved hyponymyCountMap ...");
			hyponymCountMap = EntityGraphUtils.deserializeMap(hyponymCountMapSerializedFile);
			if (hyponymCountMap.size() != nrOfNodes) {
				throw new LexicalSemanticResourceException(
						"HyponymCountMap does not contain an entry for each node in the graph."
								+ hyponymCountMap.size() + "/" + nrOfNodes);
			}
			logger.info("Done loading saved hyponymyCountMap");
			return hyponymCountMap;
		}

		hyponymCountMap = new HashMap<String, Integer>();

		// a queue holding the nodes to process
		Queue<String> queue = new LinkedList<String>();

		// In the entity graph a node may have more than one father.
		// Thus, we check whether a node was already visited.
		// Then, it is not expanded again.
		Set<String> visited = new HashSet<String>();

		// initialize the queue with all leaf nodes
		Set<String> leafNodes = new HashSet<String>();
		for (Entity leaf : lcc.getLeaves()) {
			leafNodes.add(leaf.getId());
		}
		queue.addAll(leafNodes);

		logger.info(leafNodes.size() + " leaf nodes.");

		ProgressMeter progress = new ProgressMeter(getNumberOfNodes());
		// while the queue is not empty
		while (!queue.isEmpty()) {
			// remove first element from queue
			String currNodeId = queue.poll();
			Entity currNode = lexSemRes.getEntityById(currNodeId);

			// in some rare cases, getEntityById might fail - so better check for nulls and fail
			// gracefully
			if (currNode == null) {
				visited.add(currNodeId);
				hyponymCountMap.put(currNodeId, 0);
			}

			logger.debug(queue.size());

			if (visited.contains(currNodeId)) {
				continue;
			}

			progress.next();

			if (logger.isDebugEnabled()) {
				logger.debug(progress + " - " + queue.size() + " left in queue");
			}
			else if (logger.isInfoEnabled() && (progress.getCount() % 100 == 0)) {
				logger.info(progress + " - " + queue.size() + " left in queue");
			}

			Set<Entity> children = lcc.getChildren(currNode);
			Set<String> invalidChildIds = new HashSet<String>();
			int validChildren = 0;
			int sumChildHyponyms = 0;
			boolean invalid = false;
			for (Entity child : children) {
				if (lcc.containsVertex(child)) {
					if (hyponymCountMap.containsKey(child.getId())) {
						sumChildHyponyms += hyponymCountMap.get(child.getId());
						validChildren++;
					}
					else {
						invalid = true;
						invalidChildIds.add(child.getId());
					}
				}
			}

			// we cannot use continue directly if invalid as this would continue the inner loop not
			// the outer loop
			if (invalid) {
				// One of the childs is not in the hyponymCountMap yet
				// Re-Enter the node into the queue and continue with next node
				// Also enter all the childs that are not in the queue yet
				queue.add(currNodeId);
				for (String childId : invalidChildIds) {
					if (!visited.contains(childId) && !queue.contains(childId)) {
						queue.add(childId);
					}
				}
				continue;
			}

			// mark as visited
			visited.add(currNodeId);

			// number of hyponomys of current node is the number of its own hyponyms and the sum of
			// the hyponyms of its children.
			int currNodeHyponomyCount = validChildren + sumChildHyponyms;
			hyponymCountMap.put(currNodeId, currNodeHyponomyCount);

			// add parents of current node to queue
			for (Entity parent : lcc.getParents(currNode)) {
				if (lcc.containsVertex(parent)) {
					queue.add(parent.getId());
				}
			}
		} // while queue not empty

		logger.info(visited.size() + " nodes visited");
		if (visited.size() != nrOfNodes) {
			List<Entity> missed = new ArrayList<Entity>();
			for (Entity e : lcc.getNodes()) {
				if (!visited.contains(e.getId())) {
					missed.add(e);
					System.out.println("Missed: [" + e + "]");
				}
			}

			throw new LexicalSemanticResourceException("Visited only " + visited.size()
					+ " out of " + nrOfNodes + " nodes.");
		}
		if (hyponymCountMap.size() != nrOfNodes) {
			throw new LexicalSemanticResourceException(
					"HyponymCountMap does not contain an entry for each node in the graph."
							+ hyponymCountMap.size() + "/" + nrOfNodes);
		}

		/*
		 * As an EntityGraph is a graph rather than a tree, the hyponymCount for top nodes can be
		 * greater than the number of nodes in the graph. This is due to the multiple counting of nodes
		 * having more than one parent. Thus, we have to scale hyponym counts to fall in
		 * [0,NumberOfNodes].
		 */
		for (String key : hyponymCountMap.keySet()) {
			if (hyponymCountMap.get(key) > hyponymCountMap.size()) {
				// TODO scaling function is not optimal (to say the least :)
				hyponymCountMap.put(key, (hyponymCountMap.size() - 1));
			}
		}

		logger.info("Computed hyponymCountMap");
		EntityGraphUtils.serializeMap(hyponymCountMap, hyponymCountMapSerializedFile);
		logger.info("Serialized hyponymCountMap");

		return hyponymCountMap;
	}

	/**
	 * Computes the paths from each entity node to the root. Computing n paths will take some time.
	 * Thus, efficient computing is based on the assumption that all subpaths in the shortest path
	 * to the root, are also shortest paths for the corresponding nodes. Starting with the leaf
	 * nodes gives the longest initial paths with most subpaths.
	 *
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 */
	private void createRootPathMap()
		throws LexicalSemanticResourceException
	{

		// do only create rootPathMap, if it was not already computed
		if (rootPathMap != null) {
			return;
		}

		File rootPathFile = new File(this.getGraphId() + "_" + this.rootPathMapFilename
				+ (lexSemRes.getIsCaseSensitive() ? "-cs" : "-cis"));

		// try to load rootPathMap from precomputed file
		if (rootPathFile.exists()) {
			logger.info("Loading saved rootPathMap ...");
			rootPathMap = EntityGraphUtils.deserializeMap(rootPathFile);
			logger.info("Done loading saved rootPathMap");
			return;
		}

		logger.info("Computing rootPathMap");
		rootPathMap = new HashMap<String, List<String>>();

		// a queue holding the nodes to process
		List<Entity> queue = new ArrayList<Entity>();

		// initialize the queue with all leaf nodes
		queue.addAll(this.getLeaves());

		logger.info(queue.size() + " leaf nodes.");
		fillRootPathMap(queue);

		queue.clear(); // queue should be empty now, but clear anyway

		// add non-leaf nodes that have not been on a shortest, yet
		for (Entity entity : this.getNodes()) {
			if (!rootPathMap.containsKey(entity.getId())) {
				queue.add(entity);
			}
		}

		logger.info(queue.size() + " non leaf nodes not on a shortest leaf-node to root path.");
		fillRootPathMap(queue);

		for (Entity entity : this.getNodes()) {
			if (!rootPathMap.containsKey(entity.getId())) {
				logger.info("no path for " + entity.getId());
			}
		}
		logger.info("Root path map contains " + rootPathMap.size() + " entries.");

		// from the root path map, we can very easily get the depth
		this.depth = getDepthFromRootPathMap();

		logger.info("Setting depth of entity graph: " + this.depth);

		logger.info("Serializing rootPathMap");
		EntityGraphUtils.serializeMap(rootPathMap, rootPathFile);
	}

	private void fillRootPathMap(List<Entity> queue)
		throws LexicalSemanticResourceException
	{
		Entity root = lexSemRes.getRoot();
		String rootId = root.getId();

		if (!this.getGraph().containsVertex(root)) {
			throw new LexicalSemanticResourceException(
					"Cannot fill rootPathMap if graph does not contain root.");
		}

		// while the queue is not empty
		while (!queue.isEmpty()) {
			// remove first element from queue
			Entity currentNode = queue.get(0);
			String currentNodeId = currentNode.getId();
			queue.remove(0);

			logger.debug("Queue size: " + queue.size());

			// if we have already insert a path for this node => continue with the next
			if (getRootPathMap().containsKey(currentNodeId)) {
				continue;
			}

			// compute path from current node to root
			List<String> nodesOnPath = getPathToRoot(root, currentNode);

			// if there is no path => skip
			if (nodesOnPath == null || nodesOnPath.size() == 0) {
				getRootPathMap().put(currentNodeId, new ArrayList<String>());
				continue;
			}

			// the first entry should be the current Node, the last entry should be the root
			// check whether this assumption is valid
			if (!nodesOnPath.get(0).equals(currentNodeId) || // the first node of the list should
																// always be the current node
					!nodesOnPath.get(nodesOnPath.size() - 1).equals(rootId)) { // the last node of
																				// the list should
																				// always be the
																				// root node
				logger.error("Something is wrong with the path to the root");
				logger.error(nodesOnPath.get(0) + " -- " + currentNodeId);
				logger.error(nodesOnPath.get(nodesOnPath.size() - 1) + " -- " + rootId);
				logger.error(nodesOnPath.size());
				throw new LexicalSemanticResourceException(
						"Something is wrong with a path to the root");
			}

			int i = 0;
			for (String nodeOnPath : nodesOnPath) {
				// if we have already insert a path for this node => continue with the next
				if (getRootPathMap().containsKey(nodeOnPath)) {
					continue;
				}
				// insert path
				else {
					getRootPathMap().put(nodeOnPath,
							new ArrayList<String>(nodesOnPath.subList(i, nodesOnPath.size())));
				}
				i++;
			}
		} // while queue not empty
	}

	/**
	 * This parameter is already set in the constructor as it is needed for computation of
	 * relatedness values. Therefore its computation does not trigger setGraphParameters (it is too
	 * slow), even if the depth is implicitly determined there, too.
	 *
	 * @return The depth of the category graph, i.e. the maximum path length starting with the root
	 *         node.
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 */
	private double getDepthFromRootPathMap()
		throws LexicalSemanticResourceException
	{
		int max = 0;
		for (List<String> path : getRootPathMap().values()) {
			if (path.size() > max) {
				max = path.size();
			}
		}

		max = max - 1; // depth is measured in nodes, not edges

		if (max < 0) {
			return 0;
		}
		else {
			return max;
		}
	}

	/**
	 * Returns the shortest path from node to root as a list of pageIds of the nodes on the path.
	 * Node and root are included in the path node list.
	 *
	 * @param root
	 *            The root node of the graph.
	 * @param node
	 *            A node of the graph.
	 * @return The shortest path from node to root as a list of pagIs of the nodes on the path; or
	 *         null if no path exists
	 * @throws LexicalSemanticResourceException
	 */
	private List<String> getPathToRoot(Entity root, Entity node)
		throws LexicalSemanticResourceException
	{
		List<String> pathToRoot = new LinkedList<String>();
		List<String> shortestPath = new ArrayList<String>();

		expandPath(root, node, pathToRoot, shortestPath, 0);

		if (shortestPath.size() == 0) {
			return null;
		}
		else {
			return shortestPath;
		}
	}

	private void expandPath(Entity root, Entity currentNode, List<String> currentPath,
			List<String> shortestPath, int expansionDepth)
		throws LexicalSemanticResourceException
	{

		if (expansionDepth > MAX_EXPANSION_DEPTH) {
			return;
		}

		// add the current node to the path
		currentPath.add(currentNode.getId());

		// if root node reached, check whether it is a shortest path
		if (currentNode.getId().equals(root.getId())) {
			logger.debug("found root");

			if (shortestPath.size() != 0) {
				if (currentPath.size() < shortestPath.size()) {
					logger.debug("setting new shortest path");
					shortestPath.clear();
					shortestPath.addAll(currentPath);
				}
			}
			else {
				logger.debug("initializing shortest path");
				shortestPath.addAll(currentPath);
			}
		}

		// do not expand paths that are longer or equal than the current shortest path
		// this is a runtime efficiency optimization!
		if (shortestPath.size() != 0 && currentPath.size() >= shortestPath.size()) {
			return;
		}

		Set<DefaultEdge> incomingEdges = this.getGraph().incomingEdgesOf(currentNode);

		// no incoming edges => return path without adding this node
		if (incomingEdges == null || incomingEdges.size() == 0) {
			logger.debug("found non-root source");
			return;
		}

		for (DefaultEdge incomingEdge : incomingEdges) {
			Entity sourceNode = this.getGraph().getEdgeSource(incomingEdge);

			if (sourceNode == currentNode) {
				logger.warn("Source node equals current node. Fatal error.");
				throw new LexicalSemanticResourceException(
						"Source node equals current node. Fatal error.");
			}
			List<String> savedPath = new LinkedList<String>(currentPath);
			expandPath(root, sourceNode, currentPath, shortestPath, ++expansionDepth);
			currentPath.clear();
			currentPath.addAll(savedPath);
		}

		return;
	}

	protected Map<String, List<String>> getRootPathMap()
		throws LexicalSemanticResourceException
	{
		if (rootPathMap == null) {
			createRootPathMap();
		}
		return this.rootPathMap;
	}

	@Override
	public List<String> getPageRank()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getHITS()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsCycles()
		throws LexicalSemanticResourceException
	{
		boolean hasCycles = cycleHandler.containsCycle();
		logger.info("The graph contains cycles: " + hasCycles);
		return hasCycles;
	}

	@Override
	public void removeCycles()
		throws LexicalSemanticResourceException
	{
		if (cycleHandler.containsCycle()) {
			cycleHandler.removeCycles();
			logger.info("Overwriting serialized graph file with cycle-free version.");
			try {
				GraphSerialization.saveGraph(directedGraph, serializedGraphFile);
			}
			catch (IOException e) {
				throw new LexicalSemanticResourceException(e);
			}

		}
	}

	@Override
	public String getGraphId()
	{
		return this.graphId;
	}

	@Override
	public Set<Set<Entity>> getStructuralEquivalences()
	{
		throw new UnsupportedOperationException();
	}

	public void setHyponymCountMapUseLcc(boolean aHyponymCountMapUseLcc)
	{
		hyponymCountMapUseLcc = aHyponymCountMapUseLcc;
	}

	public boolean isHyponymCountMapUseLcc()
	{
		return hyponymCountMapUseLcc;
	}
}