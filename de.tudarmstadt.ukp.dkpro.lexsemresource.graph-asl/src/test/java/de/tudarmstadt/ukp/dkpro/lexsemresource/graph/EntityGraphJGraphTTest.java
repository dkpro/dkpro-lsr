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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.graph.EntityGraphManager.EntityGraphType;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.WikipediaCategoryResource;

public class EntityGraphJGraphTTest
{

	private static final double EPSILON = 0.00001;

	private static LexicalSemanticResource wikiResource;
	private static EntityGraph graph;

	/**
     * Made this static so that following tests don't run if assumption fails.
     * (With AT_Before, tests also would not be executed but marked as passed)
     * This could be changed back as soon as JUnit ignored tests after failed
     * assumptions
     *
	 * @throws LexicalSemanticResourceException
	 */
	@BeforeClass
	public static void initializeWikipedia()
		throws LexicalSemanticResourceException
	{
		try {
			wikiResource = new WikipediaCategoryResource(
					"bender.ukp.informatik.tu-darmstadt.de",
					"wikiapi_test",
					"student",
					"student",
					de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language._test);
		}
		catch (Exception e) {
			Assume.assumeNoException(e);
		}
		graph = EntityGraphManager.getEntityGraph(wikiResource,
				EntityGraphType.JGraphT);
	}

	@Test
	public void testGraphIntegrity()
	{
		assertEquals(17, graph.getNumberOfNodes());

		assertTrue(graph.containsVertex(new Entity("UKP")));
		assertFalse(graph.containsVertex(new Entity("UTP")));

		Set<Entity> children = graph.getChildren(new Entity("UKP"));

		Set<String> expectedChildren = new HashSet<String>();
		expectedChildren.add(new Entity("Projects_of_UKP").getId());
		expectedChildren.add(new Entity("People_of_UKP").getId());
		expectedChildren.add(new Entity("Publications_of_UKP").getId());

		assertEquals(expectedChildren.size(), children.size());
		for (Entity child : children) {
			assertTrue(child.toString(),
					expectedChildren.contains(child.toString()));
		}

		assertEquals(0, graph.getChildren(new Entity("Research_Staff_of_UKP"))
				.size());
	}

	@Test
	public void testIntrinsicInformationContent()
		throws LexicalSemanticResourceException
	{
		Set<Entity> roots = graph.getLargestConnectedComponent().getRoots();
		assertEquals(1, roots.size());

		Entity root = roots.iterator().next();

		// IIC of root should be 0.0
		assertEquals(0.0, graph.getIntrinsicInformationContent(root), EPSILON);

		// IIC of leaves should be 1.0
		Set<Entity> leaves = graph.getLargestConnectedComponent().getLeaves();
		for (Entity leaf : leaves) {
			assertEquals(1.0, graph.getIntrinsicInformationContent(leaf),
					EPSILON);
		}

		// IIC of selected node
		assertEquals(1 - (Math.log(10) / Math.log(16)),
				graph.getIntrinsicInformationContent(new Entity("UKP")),
				EPSILON);

	}
}