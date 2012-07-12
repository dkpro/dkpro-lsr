package de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia;

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

public class WikipediaCategoryResourceTest
{

	private static LexicalSemanticResource wikiResource;

	/**
     * Made this static so that following tests don't run if assumption fails.
     * (With AT_Before, tests also would not be executed but marked as passed)
     * This could be changed back as soon as JUnit ignored tests after failed
     * assumptions
	 */
	@BeforeClass
	public static void initializeWikipedia()
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
	}

	@Test
	public void testContainsLexeme()
		throws Exception
	{
		wikiResource.setIsCaseSensitive(false);
		assertTrue(wikiResource.containsLexeme("UKP"));
		assertFalse(wikiResource.containsLexeme("UKT"));
		assertTrue(wikiResource.containsLexeme("People_of_UKP"));
		assertTrue(wikiResource.containsLexeme("People of UKP"));

		wikiResource.setIsCaseSensitive(true);
		assertTrue(wikiResource.containsLexeme("UKP"));
		assertFalse(wikiResource.containsLexeme("UKT"));
		assertTrue(wikiResource.containsLexeme("People_of_UKP"));
		assertTrue(wikiResource.containsLexeme("People of UKP"));

		assertFalse(wikiResource.containsLexeme("uKP"));
		assertFalse(wikiResource.containsLexeme("people_of_UKP"));
		assertFalse(wikiResource.containsLexeme("people of UKP"));
	}

	@Test
	public void testContainsEntity()
		throws Exception
	{
		wikiResource.setIsCaseSensitive(false);
		assertTrue(wikiResource.containsEntity(new Entity("UKP")));
		assertFalse(wikiResource.containsEntity(new Entity("UKT")));

		wikiResource.setIsCaseSensitive(true);
		assertTrue(wikiResource.containsEntity(new Entity("UKP")));
		assertFalse(wikiResource.containsEntity(new Entity("UKT")));

		assertFalse(wikiResource.containsEntity(new Entity("uKP")));
	}

	@Test
	public void testGetChildren()
		throws Exception
	{
		wikiResource.setIsCaseSensitive(false);
		Set<Entity> children = wikiResource.getChildren(new Entity("UKP"));
		for (Entity e : children) {
			System.out.println(e);
		}
		// Entity expectedElements[] = { new Entity("Projects_of_UKP"), new
		// Entity("Publications_of_UKP"),
		// new Entity("People_of_UKP") };
		// Set<Entity> expectedChildren = new
		// HashSet<Entity>(Arrays.asList(expectedElements));

		Set<String> expectedChildren = new HashSet<String>();
		expectedChildren.add("Projects_of_UKP#-|---unk");
		expectedChildren.add("People_of_UKP#-|---unk");
		expectedChildren.add("Publications_of_UKP#-|---unk");

		// System.out.println("---Children---");
		// for (Entity node : wikiResource.getEntities()) {
		// System.out.println(node + " : " +
		// wikiResource.getChildren(node).toString());
		// }

		assertEquals(expectedChildren.size(), children.size());
		for (Entity child : children) {
			assertTrue(child.toString(),
					expectedChildren.contains(child.toString()));
		}

		assertEquals(0,
				wikiResource.getChildren(new Entity("Research_Staff_of_UKP"))
						.size());

		wikiResource.setIsCaseSensitive(true);
		children = wikiResource.getChildren(new Entity("uKP"));
		assertEquals(0, children.size());
	}

	@Test
	public void testGetEntities()
		throws Exception
	{
		wikiResource.setIsCaseSensitive(false);
		int i = 0;
		int j = 0;
		for (Entity entity : wikiResource.getEntities()) {
			Set<String> testLexemes = entity.getLexemes();
			for (String t : testLexemes) {
				System.out.println(t + " " + entity.getSense(t));
				System.out.println(entity.getPos());
			}
			assertTrue(entity.toString(), wikiResource.containsEntity(entity));
			i++;
			// if (i == 1000) {
			// break;
			// }
		}

		wikiResource.setIsCaseSensitive(true);
		for (Entity entity : wikiResource.getEntities()) {
			Set<String> testLexemes = entity.getLexemes();
			for (String t : testLexemes) {
				System.out.println(t + " " + entity.getSense(t));
				System.out.println(entity.getPos());
			}
			assertTrue(entity.toString(), wikiResource.containsEntity(entity));
			j++;
		}

		assertEquals(i, j);
	}

	@Test
	public void testGetEntity()
		throws Exception
	{
		Set<String> expectedResults = new HashSet<String>();
		expectedResults.add("UKP#-|---unk");

		wikiResource.setIsCaseSensitive(false);
		Set<Entity> entities = wikiResource.getEntity("UKP");
		assertEquals(expectedResults.size(), entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.toString(),
					expectedResults.contains(entity.toString()));
		}

		wikiResource.setIsCaseSensitive(true);
		entities = wikiResource.getEntity("uKP");
		assertEquals(entities.size(), 0);
	}

	@Test
	public void testGetParents()
		throws Exception
	{
		wikiResource.setIsCaseSensitive(false);
		Set<Entity> parents = wikiResource.getParents(new Entity(
				"Publications_of_Telecooperation"));
		Set<String> expectedParents = new HashSet<String>();
		expectedParents.add("Telecooperation#-|---unk");

		// for (Entity node : wikiResource.getEntities()) {
		// System.out.println(node + " : " +
		// wikiResource.getParents(node).toString());
		// }

		assertEquals(expectedParents.size(), parents.size());
		for (Entity parent : parents) {
			assertTrue(parent.toString(),
					expectedParents.contains(parent.toString()));
		}
		assertEquals(0, wikiResource.getParents(new Entity("Telecooperation"))
				.size());

		wikiResource.setIsCaseSensitive(true);
		parents = wikiResource.getParents(new Entity(
				"Publications_of_Telecooperation"));
		expectedParents = new HashSet<String>();
		expectedParents.add("Telecooperation#-|---unk");

		// for (Entity node : wikiResource.getEntities()) {
		// System.out.println(node + " : " +
		// wikiResource.getParents(node).toString());
		// }

		assertEquals(expectedParents.size(), parents.size());
		for (Entity parent : parents) {
			assertTrue(parent.toString(),
					expectedParents.contains(parent.toString()));
		}
		assertEquals(0, wikiResource.getParents(new Entity("Telecooperation"))
				.size());

		parents = wikiResource.getParents(new Entity(
				"publications_of_Telecooperation"));
		assertEquals(parents.size(), 0);
	}

	@Test
	public void testGetNeighbors()
		throws Exception
	{
		wikiResource.setIsCaseSensitive(false);
		Set<Entity> neighbors = wikiResource.getNeighbors(new Entity(
				"People_of_Telecooperation"));
		Set<String> expectedNeighbors = new HashSet<String>();
		expectedNeighbors.add("Telecooperation#-|---unk");
		expectedNeighbors.add("People_of_UKP#-|---unk");

		// for (Entity node : wikiResource.getEntities()) {
		// System.out.println(node + " : " +
		// wikiResource.getNeighbors(node).toString());
		// }
		assertEquals(expectedNeighbors.size(), neighbors.size());
		for (Entity neighbor : neighbors) {
			assertTrue(neighbor.toString(),
					expectedNeighbors.contains(neighbor.toString()));
		}
		assertEquals(0,
				wikiResource.getNeighbors(new Entity("Unconnected_category"))
						.size());

		wikiResource.setIsCaseSensitive(true);
		neighbors = wikiResource.getNeighbors(new Entity(
				"People_of_Telecooperation"));
		expectedNeighbors = new HashSet<String>();
		expectedNeighbors.add("Telecooperation#-|---unk");
		expectedNeighbors.add("People_of_UKP#-|---unk");

		// for (Entity node : wikiResource.getEntities()) {
		// System.out.println(node + " : " +
		// wikiResource.getNeighbors(node).toString());
		// }
		assertEquals(expectedNeighbors.size(), neighbors.size());
		for (Entity neighbor : neighbors) {
			assertTrue(neighbor.toString(),
					expectedNeighbors.contains(neighbor.toString()));
		}
		assertEquals(0,
				wikiResource.getNeighbors(new Entity("Unconnected_category"))
						.size());

		neighbors = wikiResource.getNeighbors(new Entity(
				"people_of_Telecooperation"));
		assertEquals(neighbors.size(), 0);
	}

	@Test
	public void testGetNumberOfEntities()
		throws LexicalSemanticResourceException
	{
		wikiResource.setIsCaseSensitive(false);
		assertEquals(17, wikiResource.getNumberOfEntities());
		wikiResource.setIsCaseSensitive(true);
		assertEquals(17, wikiResource.getNumberOfEntities());
	}

	@Test
	public void testGetResourceVersion()
	{
		assertEquals("_test_unknown-version", wikiResource.getResourceVersion());
	}
}