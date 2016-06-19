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
package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource.LexicalRelation;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource.SemanticRelation;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.graph.EntityGraph;
import de.tudarmstadt.ukp.dkpro.lexsemresource.graph.EntityGraphManager;
import de.tudarmstadt.ukp.dkpro.lexsemresource.graph.EntityGraphManager.EntityGraphType;

public class WordNetResourceTest
{

	private static LexicalSemanticResource wordnet;

	@BeforeClass
	public static void initializeWordNet()
	{
		try {
			wordnet = new WordNetResource(
					"src/main/resources/resource/WordNet_3/wordnet_properties.xml");
		}
		catch (Exception e) {
		    e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testContainsLexeme() throws Exception
	{
		wordnet.setIsCaseSensitive(false);
		assertTrue(wordnet.containsLexeme("tree"));
		assertTrue(wordnet.containsLexeme("Tree"));
		assertTrue(wordnet.containsLexeme("cold"));
		assertTrue(wordnet.containsLexeme("Cold"));
		assertFalse(wordnet.containsLexeme("grhphafah"));
		assertFalse(wordnet.containsLexeme("Grhphafah"));

		wordnet.setIsCaseSensitive(true);
		assertTrue(wordnet.containsLexeme("tree"));
		assertFalse(wordnet.containsLexeme("Tree"));
		assertTrue(wordnet.containsLexeme("cold"));
		assertFalse(wordnet.containsLexeme("ColD"));
		assertFalse(wordnet.containsLexeme("grhphafah"));
		assertFalse(wordnet.containsLexeme("Grhphafah"));
	}

	@Test
	public void testContainsEntity() throws Exception
	{
		wordnet.setIsCaseSensitive(false);
		assertTrue(wordnet.containsEntity(new Entity("tree")));
		assertTrue(wordnet.containsEntity(new Entity("Tree")));
		assertTrue(wordnet.containsEntity(new Entity("tree", PoS.n)));
		assertTrue(wordnet.containsEntity(new Entity("Tree", PoS.n)));
		assertTrue(wordnet.containsEntity(new Entity("tree", PoS.v)));
		assertTrue(wordnet.containsEntity(new Entity("Tree", PoS.v)));
		assertTrue(wordnet.containsEntity(new Entity("knocked-out", PoS.adj, "680634")));
		assertTrue(wordnet.containsEntity(new Entity("KnoCked-ouT", PoS.adj, "680634")));
		assertFalse(wordnet.containsEntity(new Entity("tree", PoS.adj)));
		assertFalse(wordnet.containsEntity(new Entity("humbelpfh")));

		wordnet.setIsCaseSensitive(true);
		assertTrue(wordnet.containsEntity(new Entity("tree")));
		assertFalse(wordnet.containsEntity(new Entity("TrEe")));
		assertTrue(wordnet.containsEntity(new Entity("tree", PoS.n)));
		assertFalse(wordnet.containsEntity(new Entity("Tree", PoS.n)));
		assertTrue(wordnet.containsEntity(new Entity("tree", PoS.v)));
		assertFalse(wordnet.containsEntity(new Entity("Rree", PoS.v)));
		assertTrue(wordnet.containsEntity(new Entity("knocked-out", PoS.adj, "680634")));
		assertFalse(wordnet.containsEntity(new Entity("Knocked-out", PoS.adj, "680634")));
		assertFalse(wordnet.containsEntity(new Entity("tree", PoS.adj)));
		assertFalse(wordnet.containsEntity(new Entity("Tree", PoS.adj)));
		assertFalse(wordnet.containsEntity(new Entity("humbelpfh")));
	}

	// special cases that were found to give errors during usage
	@Test
	public void testContainsEntitySpecialCases() throws Exception
	{
		Map<String, String> automotiveVehicleLexemes = new HashMap<String, String>();
		automotiveVehicleLexemes.put("automotive_vehicle", "3791235");
		automotiveVehicleLexemes.put("motor_vehicle", "3791235");
		Entity automotiveVehicleEntity = new Entity(automotiveVehicleLexemes, PoS.n);

		wordnet.setIsCaseSensitive(false);
		assertTrue(automotiveVehicleEntity.toString(), wordnet
				.containsEntity(automotiveVehicleEntity));

		wordnet.setIsCaseSensitive(true);
		assertTrue(automotiveVehicleEntity.toString(), wordnet
				.containsEntity(automotiveVehicleEntity));
	}

	@Test
	public void testGetEntity()
		throws LexicalSemanticResourceException
	{
		wordnet.setIsCaseSensitive(false);
		Set<String> expectedResults = new HashSet<String>();
		expectedResults.add("tree#1145163|---v");
		expectedResults.add("tree#1616293|---v");
		expectedResults.add("shoetree#319111|tree#319111|---v");
		expectedResults.add("corner#1934205|tree#1934205|---v");
		expectedResults.add("tree#13104059|---n");
		expectedResults.add("tree#13912260|tree diagram#13912260|---n");
		expectedResults.add("Sir Herbert Beerbohm Tree#11348160|Tree#11348160|---n");

		Set<Entity> entities = wordnet.getEntity("tree");
		assertEquals(7, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.getId(), expectedResults.contains(entity.getId()));
		}

		wordnet.setIsCaseSensitive(true);
		entities = wordnet.getEntity("Tree");
		assertEquals(entities.size(), 0);
	}

	@Test
	@Ignore("this fails due to a bug in the WordNet API, it returns not only automotive_vehicle, "
			+ "but also any other synset containing automotive or vehicle - See Bug 160")
	public void testGetEntitySpecialCases()
		throws LexicalSemanticResourceException
	{
		wordnet.setIsCaseSensitive(false);
		Set<String> expectedResults = new HashSet<String>();
		expectedResults.add("automotive_vehicle#3791235|motor_vehicle#3791235|---n");

		Set<Entity> entities = wordnet.getEntity("automotive_vehicle");
		assertEquals(1, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.getId(), expectedResults.contains(entity.getId()));
		}

		// TODO this fails due to an bug in the toSynset method in WordNetUtils
		wordnet.setIsCaseSensitive(true);
		entities = wordnet.getEntity("automotive_vehicle");
		assertEquals(3, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.getId(), expectedResults.contains(entity.getId()));
		}
	}

	@Test
	public void testGetEntityPos()
		throws LexicalSemanticResourceException
	{

		wordnet.setIsCaseSensitive(false);
		Set<String> expectedResults = new HashSet<String>();
		expectedResults.add("tree#13104059|---n");
		expectedResults.add("tree#13912260|tree diagram#13912260|---n");
		expectedResults.add("Sir Herbert Beerbohm Tree#11348160|Tree#11348160|---n");

		Set<Entity> entities = wordnet.getEntity("tree", PoS.n);
		assertEquals(3, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.getId(), expectedResults.contains(entity.getId()));
		}

		entities = wordnet.getEntity("Tree", PoS.n);
		assertEquals(3, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.getId(), expectedResults.contains(entity.getId()));
		}

		wordnet.setIsCaseSensitive(true);
		expectedResults = new HashSet<String>();
		expectedResults.add("tree#13104059|---n");
		expectedResults.add("tree#13912260|tree diagram#13912260|---n");
		expectedResults.add("Sir Herbert Beerbohm Tree#11348160|Tree#11348160|---n");

		entities = wordnet.getEntity("tree", PoS.n);
		assertEquals(3, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.getId(), expectedResults.contains(entity.getId()));
		}

		entities = wordnet.getEntity("Tree", PoS.n);
	}

	@Test
	public void testGetEntityPosSense()
		throws LexicalSemanticResourceException
	{

		wordnet.setIsCaseSensitive(false);
		Set<String> expectedResults = new HashSet<String>();
		expectedResults.add("tree#13912260|tree diagram#13912260|---n");

		// also test whether compare
		Map<String, String> treeLexemes = new HashMap<String, String>();
		treeLexemes.put("tree", "13912260");
		treeLexemes.put("tree diagram", "13912260");
		Entity treeEntity = new Entity(treeLexemes, PoS.n);

		Set<Entity> entities = wordnet.getEntity("tree", PoS.n, "13912260");
		assertEquals(1, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.getId(), expectedResults.contains(entity.getId()));
			assertEquals(0, entity.compareTo(treeEntity));
		}

		entities = wordnet.getEntity("Tree", PoS.n, "13912260");
		assertEquals(1, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.getId(), expectedResults.contains(entity.getId()));
			assertEquals(0, entity.compareTo(treeEntity));
		}

		wordnet.setIsCaseSensitive(true);
		entities = wordnet.getEntity("tree", PoS.n, "13912260");
		assertEquals(1, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.getId(), expectedResults.contains(entity.getId()));
			assertEquals(0, entity.compareTo(treeEntity));
		}

		entities = wordnet.getEntity("Tree", PoS.n, "13912260");
		assertEquals(0, entities.size());
	}

	@Test
	public void testGetLexicalRelations()
		throws LexicalSemanticResourceException
	{

		wordnet.setIsCaseSensitive(false);
		Set<String> expectedAntonyms = new HashSet<String>();
		expectedAntonyms.add("cool");

		Set<String> expectedSynonyms = new HashSet<String>();
		expectedSynonyms.add("fond");
		expectedSynonyms.add("affectionate");
		expectedSynonyms.add("warm");
		expectedSynonyms.add("loving");
		expectedSynonyms.add("tender");

		Set<String> antonyms = wordnet.getRelatedLexemes("warm", PoS.adj, "2530861",
				LexicalRelation.antonymy);
		assertEquals(1, antonyms.size());
		for (String antonym : antonyms) {
			assertTrue(antonym, expectedAntonyms.contains(antonym));
		}

		Set<String> synonyms = wordnet.getRelatedLexemes("lovesome", PoS.adj, "1464700",
				LexicalRelation.synonymy);
		assertEquals(5, synonyms.size());
		for (String synonym : synonyms) {
			assertTrue(synonym, expectedSynonyms.contains(synonym));
		}

		wordnet.setIsCaseSensitive(true);
		antonyms = wordnet.getRelatedLexemes("warm", PoS.adj, "2530861", LexicalRelation.antonymy);
		assertEquals(1, antonyms.size());
		for (String antonym : antonyms) {
			assertTrue(antonym, expectedAntonyms.contains(antonym));
		}

		antonyms = wordnet.getRelatedLexemes("Warm", PoS.adj, "2530861", LexicalRelation.antonymy);
		assertEquals(0, antonyms.size());

		synonyms = wordnet.getRelatedLexemes("lovesome", PoS.adj, "1464700",
				LexicalRelation.synonymy);
		assertEquals(5, synonyms.size());
		for (String synonym : synonyms) {
			assertTrue(synonym, expectedSynonyms.contains(synonym));
		}
		synonyms = wordnet.getRelatedLexemes("Lovesome", PoS.adj, "1464700",
				LexicalRelation.synonymy);
		assertEquals(synonyms.size(), 0);

	}

	@Test
	public void testGetSemanticRelations()
		throws LexicalSemanticResourceException
	{

		wordnet.setIsCaseSensitive(false);
		Set<String> expectedHyponyms = new HashSet<String>();
		expectedHyponyms.add("cladogram#13912424|---n");
		expectedHyponyms.add("stemma#13912540|---n");

		Set<Entity> hyponyms = wordnet.getRelatedEntities(new Entity("tree", PoS.n, "13912260"),
				SemanticRelation.hyponymy);

		for (Entity hyponym : hyponyms) {
			assertTrue(hyponym.toString(), expectedHyponyms.contains(hyponym.toString()));
		}

		Set<String> expectedCohyponyms = new HashSet<String>();
		expectedHyponyms.add("cladogram#13912424|---n");
		expectedHyponyms.add("stemma#13912540|---n");

		Set<Entity> cohyponyms = wordnet.getRelatedEntities(
				new Entity("stemma", PoS.n, "13912540"), SemanticRelation.hyponymy);

		for (Entity cohyponym : cohyponyms) {
			assertTrue(cohyponym.toString(), expectedCohyponyms.contains(cohyponym.toString()));
		}

		wordnet.setIsCaseSensitive(true);
		expectedHyponyms = new HashSet<String>();
		expectedHyponyms.add("cladogram#13912424|---n");
		expectedHyponyms.add("stemma#13912540|---n");

		hyponyms = wordnet.getRelatedEntities(new Entity("tree", PoS.n, "13912260"),
				SemanticRelation.hyponymy);

		for (Entity hyponym : hyponyms) {
			assertTrue(hyponym.toString(), expectedHyponyms.contains(hyponym.toString()));
		}

		hyponyms = wordnet.getRelatedEntities(new Entity("Tree", PoS.n, "13912260"),
				SemanticRelation.hyponymy);
		assertEquals(hyponyms.size(), 0);

	}

	@Test
	public void testGetGloss()
		throws LexicalSemanticResourceException
	{
		wordnet.setIsCaseSensitive(false);
		Entity e = new Entity("knocked-out", PoS.adj, "680634");
		String gloss = "damaged; \"the gym has some of the most knocked-out equipment since Vic Tanny\"";
		assertEquals(gloss, wordnet.getGloss(e));

		Entity e2 = new Entity("laugh", PoS.n, "6778102");
		String gloss2 = "a humorous anecdote or remark intended to provoke laughter; \"he told a very funny joke\"; "
				+ "\"he knows a million gags\"; \"thanks for the laugh\"; \"he laughed unpleasantly at his own jest\"; "
				+ "\"even a schoolboy's jape is supposed to have some ascertainable point\"";
		assertEquals(gloss2, wordnet.getGloss(e2));

		wordnet.setIsCaseSensitive(true);
		e = new Entity("knocked-out", PoS.adj, "680634");
		gloss = "damaged; \"the gym has some of the most knocked-out equipment since Vic Tanny\"";
		assertEquals(gloss, wordnet.getGloss(e));

		e = new Entity("Knocked-out", PoS.adj, "680634");
		assertEquals("", wordnet.getGloss(e));

		e2 = new Entity("laugh", PoS.n, "6778102");
		gloss2 = "a humorous anecdote or remark intended to provoke laughter; \"he told a very funny joke\"; "
				+ "\"he knows a million gags\"; \"thanks for the laugh\"; \"he laughed unpleasantly at his own jest\"; "
				+ "\"even a schoolboy's jape is supposed to have some ascertainable point\"";
		assertEquals(gloss2, wordnet.getGloss(e2));

		e2 = new Entity("Laugh", PoS.n, "6778102");
		assertEquals("", wordnet.getGloss(e2));
	}

	@Test
	public void testGetPseudoGloss()
		throws LexicalSemanticResourceException
	{
		Set<LexicalRelation> lexRels = new HashSet<LexicalRelation>();
		lexRels.add(LexicalRelation.antonymy);
		lexRels.add(LexicalRelation.synonymy);

		Map<SemanticRelation, Integer> semRelMap = new HashMap<SemanticRelation, Integer>();
		semRelMap.put(SemanticRelation.holonymy, 2);
		semRelMap.put(SemanticRelation.meronymy, 2);
		semRelMap.put(SemanticRelation.hypernymy, 2);
		semRelMap.put(SemanticRelation.hyponymy, 3);

		Entity e = new Entity("warm", PoS.adj, "2530861");
		Set<String> gloss = new HashSet<String>(Arrays.asList("cool", "warm", "cordial", "hearty"));
		Set<String> pgloss = new HashSet<String>(Arrays.asList(wordnet.getPseudoGloss(e, lexRels, semRelMap).split(" ")));
		assertEquals(gloss, pgloss);
	}

	// this tests some error I found when trying to find entities returned as related to the string
	// "pick_up"
	@Test
	public void testPickUp()
		throws LexicalSemanticResourceException
	{
		Set<Entity> entities = wordnet.getEntity("pick-up");
		for (Entity e : entities) {
			System.out.println(e);
			for (String lexeme : e.getLexemes()) {
				System.out.println(lexeme);
				System.out.println(e.getPos());
				System.out.println(e.getSense(lexeme));
				wordnet.getRelatedLexemes(lexeme, e.getPos(), e.getSense(lexeme),
						LexicalRelation.synonymy);
			}
		}
	}

	@Test
	@Ignore("There seems to be a bug in the WordNet API causing containsEntity() return false "
			+ "on some entities- See Bug 161")
	public void testGetEntities()
		throws LexicalSemanticResourceException
	{
		wordnet.setIsCaseSensitive(false);
		int i = 0;
		for (Entity entity : wordnet.getEntities()) {
			Set<String> testLexemes = entity.getLexemes();
			StringBuilder sb = new StringBuilder();
			for (String t : testLexemes) {
				sb.append(t + " " + entity.getSense(t) + " ");
			}
			sb.append(entity.getPos());
			assertTrue(entity.toString(), wordnet.containsEntity(entity));
			i++;
		}
		assertEquals(117659, i);

		wordnet.setIsCaseSensitive(true);
		i = 0;
		for (Entity entity : wordnet.getEntities()) {
			Set<String> testLexemes = entity.getLexemes();
			StringBuilder sb = new StringBuilder();
			for (String t : testLexemes) {
				sb.append(t + " " + entity.getSense(t) + " ");
			}
			sb.append(entity.getPos());
			++i;
			assertTrue(entity.toString(), wordnet.containsEntity(entity));

		}
		assertEquals(117659, i);
	}

	// TODO - readd if implemented more efficiently
	// @Ignore
	@Test
	public void testGetNumberOfEntities()
		throws LexicalSemanticResourceException
	{
		assertEquals(117659, wordnet.getNumberOfEntities());
	}

	@Test
	public void testHyponymMap()
		throws Exception
	{
		wordnet.setIsCaseSensitive(false);
		EntityGraph eg = EntityGraphManager.getEntityGraph(wordnet, EntityGraphType.JGraphT);
		eg.getIntrinsicInformationContent(wordnet.getEntity("tree").iterator().next());
	}

	@Test
	public void testGetMostFrequentEntity()
	    throws Exception
	{
        wordnet.setIsCaseSensitive(false);
        System.out.println(wordnet.getMostFrequentEntity("car"));
        System.out.println(wordnet.getMostFrequentEntity("bat"));
        System.out.println(wordnet.getMostFrequentEntity("bank"));
    }
}
