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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * A random walk in the graph.
 *
 * In Random Walk (RW) sampling, we uniformly at random pick a starting node and
 * then simulate a random walk on the graph. At every step with probability c =
 * 0.15 (the value commonly used in literature) we fly back to the starting node
 * and re-start the random walk. There is problem of getting stuck, for example,
 * if the starting node is a sink, and/or it belongs to a small, isolated
 * component. The solution is: If, after a very long number of steps, we do not
 * visit enough nodes to meet the required sample size, we select another
 * starting node and repeat the procedure. In our experiments we run the random
 * walk for 100 * n steps. (Leskovec and Faloutsos, 2006)
 *
 * @author garoufi
 *
 */

public class AdjMatrixRandomWalkJUNG
{
	private final Log logger = LogFactory.getLog(getClass());
	public DirectedGraph<Entity, EntityGraphEdge> entityGraph;
	PersistentAdjacencyMatrix adjMatrix;
	private Entity startEntity;
	private Entity startEntityVertex;
//	private final Set<String> edgeSet = new HashSet<String>();
	int resourceSize = 0;
	int graphSize = 0;

	/**
	 * A random walk graph from a resource with a desired size
	 *
	 * @param resource
	 * @param size
	 *            The size of the RW graph. It takes a double value from 0 to 1
	 *            (exclusively) that corresponds to the fraction of the original
	 *            graph's size that we want the sample to have. Values around
	 *            .15 should be OK (Leskovec and Faloutsos, 2006)
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 */
	public AdjMatrixRandomWalkJUNG(LexicalSemanticResource resource, double size)
		throws LexicalSemanticResourceException
	{

		// size of a sample must be between 0 and 1
		if (size <= 0 || size >= 1) {
			logger.error("Requested sample size not acceptable. Setting the sample size to 15%");
			size = .15;
		}

		adjMatrix = new PersistentAdjacencyMatrix(resource);

		this.resourceSize = adjMatrix.resourceSize;
		this.entityGraph = createRWGraph(resource, size);
	}

	/**
	 * Create a random walk graph from a resource with a desired size
	 *
	 * @param resource
	 * @param size
	 *            The size of the RW graph
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 */
	private DirectedGraph<Entity, EntityGraphEdge> createRWGraph(
			LexicalSemanticResource resource, double size)
		throws LexicalSemanticResourceException
	{

		DirectedGraph<Entity, EntityGraphEdge> graph = new DirectedSparseGraph<Entity, EntityGraphEdge>();

		// for Wikipedia-AG-DE: start with node "Weltfrieden":
		Map<String, String> startEntityLexemes = new HashMap<String, String>();
		startEntityLexemes.put("Weltfriede", "-");
		startEntityLexemes.put("Weltfrieden", "-");
		startEntity = resource.getEntity(startEntityLexemes, PoS.unk);

		// output resource and sample size
		int sampleSize = (int) (resourceSize * size);
		logger.info("The size of the resource is " + resourceSize);
		logger.info("The size of the desired sample is " + sampleSize);

		graph.addVertex(startEntity);

		logger.info("Starting the RW with node: " + startEntity);

		// // pause for a few sec so that i read the output:
		// Long stoptime = 5000L;
		// try {
		// Thread.sleep(stoptime);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		Entity JUNGEntityVertex = startEntityVertex;
		int numIterations = 100 * resourceSize;
		graphSize = 1;
		int progress = 0;

		// run the random walk:
		for (int i = 0; i < numIterations; i++) {

			progress = 100 * i / numIterations;
			if (i % 10000 == 0) {
				logger.info("Sample size progress: " + graphSize + " ("
						+ (100 * graphSize / sampleSize)
						+ "%). Iteration progress: " + progress + "%");
			}

			// now pick a random number from 0 to 1 in order to implement
			// nondeterminism:
			double r = Math.random();

			// check if you got enough nodes:
			if (graphSize >= sampleSize) {
				logger.info("Desired sample size reached! Breaking the loop.");
				break;
			}

			// with probability 85% burn child and make it the current node:
			else if (r > 0.15) {
				JUNGEntityVertex = burnChild(JUNGEntityVertex, graph);
			}

			// with probability 15% fly back to start:
			else {
				JUNGEntityVertex = startEntityVertex;
			}
		}
		return graph;
	}

	/**
	 * Randomly picks a child of the given source node, and burns it, i.e. puts
	 * it into the graph, along with the corresponding edge
	 *
	 * @param source
	 * @param graph
	 * @throws LexicalSemanticResourceException
	 * @throws UnsupportedOperationException
	 * @return The child burned, if the source has children. The source itself,
	 *         otherwise.
	 */
	private Entity burnChild(Entity sourceVertex,
			DirectedGraph<Entity, EntityGraphEdge> graph)
		throws LexicalSemanticResourceException
	{

		// Set<Entity> children =
		// lexSemResource.getChildren(sourceVertex.getVertexEntity());
		Set<Entity> children = adjMatrix.getAdjacencies(sourceVertex);

		int numChildren = children.size();
		Entity child = null;
		Random generator = new Random();

		if (numChildren != 0) {

			// randomly select one of the children:
			int pickedChild = generator.nextInt(numChildren);
			child = (Entity) children.toArray()[pickedChild];

			// check if the child is already in the graph. If not, create it:
			if (!graph.getVertices().contains(child)) {

				graph.addVertex(child);
				// logger.info("Burned " +
				// childVertex.getVertexEntity().toString());

				// graph.addEdge(new DirectedSparseEdge(sourceVertex,
				// childVertex));
				// edgeSet.add("(" + sourceVertex.getVertexEntity() + ", " +
				// child + ")");
				graphSize++;
			}

			// if the child is already in the graph, find it:

			// now check for edge existence. if edge not there, add it
			if (graph.findEdge(sourceVertex, child) == null) {
				graph.addEdge(new EntityGraphEdge(sourceVertex, child),
						sourceVertex, child);
			}

			return child;
		}
		else {
			return sourceVertex;
		}
	}
}
