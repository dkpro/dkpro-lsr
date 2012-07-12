package de.tudarmstadt.ukp.dkpro.lexsemresource.wiktionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity.PoS;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource.LexicalRelation;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource.SemanticRelation;
import de.tudarmstadt.ukp.wiktionary.api.Language;

//ae 	U+00E4
//oe 	U+00F6
//ue 	U+00FC
//AE 	U+00C4
//OE 	U+00D6
//UE 	U+00DC
//ss    U+00DF

// TODO the whole unit test has case sensitivity problems -- the tests were written with an
// case-sensitive wiktionary in mind
// See Bug 146
@Ignore("Currently does not work since the wiktionary data is huge and not easily available")
public class WiktionaryResourceTest
{

	private static LexicalSemanticResource wiktionaryGerman;
	private static LexicalSemanticResource wiktionaryEnglish;

	@BeforeClass
	public static void initializeWiktionary()
		throws Exception
	{
		wiktionaryGerman = new WiktionaryResource(Language.GERMAN, "resource/Wiktionary/latest_de/");
		wiktionaryEnglish = new WiktionaryResource(Language.ENGLISH,
				"resource/Wiktionary/latest_en/");
	}

	@Test
	public void testContainsLexeme()
		throws Exception
	{
		wiktionaryGerman.setIsCaseSensitive(true);
		assertTrue("Baum", wiktionaryGerman.containsLexeme("Baum"));
		assertFalse("baum", wiktionaryGerman.containsLexeme("baum"));
		assertFalse("tree", wiktionaryGerman.containsLexeme("tree"));
		assertFalse("tree", wiktionaryGerman.containsLexeme("Tree"));
		assertFalse("grhphafah", wiktionaryGerman.containsLexeme("grhphafah"));

		wiktionaryGerman.setIsCaseSensitive(false);
		assertTrue("Baum", wiktionaryGerman.containsLexeme("Baum"));
		assertTrue("baum", wiktionaryGerman.containsLexeme("baum"));
		assertFalse("tree", wiktionaryGerman.containsLexeme("tree"));
		assertFalse("tree", wiktionaryGerman.containsLexeme("Tree"));

		wiktionaryEnglish.setIsCaseSensitive(true);
		assertTrue("tree", wiktionaryEnglish.containsLexeme("tree"));
		assertFalse("Tree", wiktionaryEnglish.containsLexeme("Tree"));
		assertFalse("grhphafah", wiktionaryEnglish.containsLexeme("grhphafah"));

		wiktionaryEnglish.setIsCaseSensitive(false);
		assertTrue("tree", wiktionaryEnglish.containsLexeme("tree"));
		assertTrue("Tree", wiktionaryEnglish.containsLexeme("Tree"));
		assertFalse("grhphafah", wiktionaryEnglish.containsLexeme("grhphafah"));

		// "zur Zeit" is a German entry in the German Wiktionary. The reason why it is not found
		// as a lexeme here
		// is that the Wiktionary API does not parse the entry language properly and returns
		// "UNSPECIFIED" language.
		// I guess the problem in the JWKTL language parser is that it fails to deal with the
		// " " between the two words,
		// expecting the language specification right after it.
		// assertFalse("zur Zeit", wiktionaryEnglish.containsLexeme("zur Zeit"));
		// assertTrue("zur Zeit", wiktionaryGerman.containsLexeme("zur Zeit"));
		// assertFalse("zur_Zeit", wiktionaryGerman.containsLexeme("zur_Zeit"));

		// assertTrue("Abbesche Zahl", wiktionaryGerman.containsLexeme("Abbesche Zahl"));
		// assertFalse("Abbesche_Zahl", wiktionaryGerman.containsLexeme("Abbesche_Zahl"));
	}

	/*
	 * Default Entity constructor will set the PoS of the Entity to 'UNKNOWN' But 'UNKNOWN' PoS in
	 * Wiktionary API doesn't mean 'any'
	 */

	@Test
	public void testContainsEntity()
		throws Exception
	{
		wiktionaryGerman.setIsCaseSensitive(true);
		assertTrue("Auto", wiktionaryGerman.containsEntity(new Entity("Baum", Entity.PoS.n)));
		assertTrue("Auto", wiktionaryGerman.containsEntity(new Entity("Auto", Entity.PoS.n)));
		assertFalse("auto", wiktionaryGerman.containsEntity(new Entity("auto", Entity.PoS.n)));
		assertFalse("car", wiktionaryGerman.containsEntity(new Entity("car", Entity.PoS.n)));
		assertFalse("humbelpfh", wiktionaryGerman.containsEntity(new Entity("humbelpfh",
				Entity.PoS.n)));

		wiktionaryGerman.setIsCaseSensitive(false);
		assertTrue("Auto", wiktionaryGerman.containsEntity(new Entity("Auto", Entity.PoS.n)));
		assertTrue("auto", wiktionaryGerman.containsEntity(new Entity("auto", Entity.PoS.n)));
		assertFalse("car", wiktionaryGerman.containsEntity(new Entity("car", Entity.PoS.n)));
		assertFalse("humbelpfh", wiktionaryGerman.containsEntity(new Entity("humbelpfh",
				Entity.PoS.n)));

		wiktionaryEnglish.setIsCaseSensitive(true);
		assertTrue("car", wiktionaryEnglish.containsEntity(new Entity("car", Entity.PoS.n)));
		assertFalse("Car", wiktionaryEnglish.containsEntity(new Entity("CaR", Entity.PoS.n)));
		assertFalse("humbelpfh", wiktionaryEnglish.containsEntity(new Entity("humbelpfh",
				Entity.PoS.n)));

		wiktionaryEnglish.setIsCaseSensitive(false);
		assertTrue("car", wiktionaryEnglish.containsEntity(new Entity("car", Entity.PoS.n)));
		assertTrue("car", wiktionaryEnglish.containsEntity(new Entity("Car", Entity.PoS.n)));
		assertFalse("humbelpfh", wiktionaryEnglish.containsEntity(new Entity("humbelpfh",
				Entity.PoS.n)));

		// assertTrue("zur Zeit", wiktionaryGerman.containsEntity(new Entity("zur Zeit")));
	}

	@Test
	public void testGetEntity()
		throws Exception
	{
		Set<String> expectedResults = new HashSet<String>();
		expectedResults.add("mine#-|---n");
		expectedResults.add("mine#-|---v");
		expectedResults.add("mine#-|---unk");

		Set<Entity> entities;
		wiktionaryEnglish.setIsCaseSensitive(false);
		entities = wiktionaryEnglish.getEntity("mine");
		System.out.println(entities);
		assertEquals(expectedResults.size(), entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.toString(), expectedResults.contains(entity.toString()));
		}

		wiktionaryEnglish.setIsCaseSensitive(true);
		entities = wiktionaryEnglish.getEntity("mine");
		System.out.println(entities);
		assertEquals(expectedResults.size(), entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.toString(), expectedResults.contains(entity.toString()));
		}

		// "gerade" has four different PoS: Adjektiv, Adverb, Fokuspartikel

		Set<String> expectedResults2 = new HashSet<String>();
		expectedResults2.add("gerade#-|---adj");
		expectedResults2.add("gerade#-|---n");
		expectedResults2.add("gerade#-|---adv");
		expectedResults2.add("gerade#-|---unk");

		wiktionaryGerman.setIsCaseSensitive(false);
		Set<Entity> entities2 = wiktionaryGerman.getEntity("gerade");
		System.out.println("***gerade Entities: " + entities2);
		assertEquals(expectedResults2.size(), entities2.size());
		for (Entity entity : entities2) {
			assertTrue(entity.toString(), expectedResults2.contains(entity.toString()));
		}

		Set<String> expectedResults3 = new HashSet<String>();
		expectedResults3.add("gerade#-|---adj");
		expectedResults3.add("gerade#-|---adv");
		expectedResults3.add("gerade#-|---unk");
		wiktionaryGerman.setIsCaseSensitive(true);
		entities2 = wiktionaryGerman.getEntity("gerade");
		System.out.println("***gerade Entities: " + entities2);
		assertEquals(expectedResults3.size(), entities2.size());
		for (Entity entity : entities2) {
			assertTrue(entity.toString(), expectedResults2.contains(entity.toString()));
		}

		/*
		 * // "bloU+00DF" is an entry with PoS "Adjektiv, Adverb, Partikel" all in one. //
		 * Wiktionary API deals with such cases by returning only the first PoS from the list and
		 * ignoring the rest, // i.e. here it returns "Adjektiv". // I think this is a reasonable
		 * approach, as a single entity can only have one PoS. // The test however fails, because
		 * the language is not read correctly and is set as UNSPECIFIED. Set<String>
		 * expectedResultsIV = new HashSet<String>(); expectedResultsIV.add("blo\u00df#- ---adj");
		 *
		 * //Set<Entity> entitiesIV = wiktionaryUnspecified.getEntity("blo\u00df"); Set<Entity>
		 * entitiesIV = wiktionaryGerman.getEntity("blo\u00df");
		 *
		 * System.out.println("***blo\u00df Entities: " + entitiesIV);
		 * assertEquals(expectedResultsIV.size(), entitiesIV.size()); for (Entity entity :
		 * entitiesIV) { assertTrue(entity.toString(),
		 * expectedResultsIV.contains(entity.toString())); }
		 *
		 * // "Abbesche Zahl" has PoS "Wortverbindung" // Same problem here is above, i.e. incorrect
		 * parsing of entry language Set<String> expectedResultsV = new HashSet<String>();
		 * expectedResultsV.add("Abbesche Zahl#- ---unk");
		 *
		 * //Set<Entity> entitiesV = wiktionaryUnspecified.getEntity("Abbesche Zahl"); Set<Entity>
		 * entitiesV = wiktionaryGerman.getEntity("Abbesche Zahl");
		 *
		 * System.out.println("***Abbesche Zahl Entities: " + entitiesV);
		 * assertEquals(expectedResultsV.size(), entitiesV.size()); for (Entity entity : entitiesV)
		 * { assertTrue(entity.toString(), expectedResultsV.contains(entity.toString())); }
		 */
	}

	@Test
	public void testGetEntityPos()
		throws Exception
	{
		Set<String> expectedResults = new HashSet<String>();
		expectedResults.add("mine#-|---v");

		Map<String, String> mineLexemes = new HashMap<String, String>();
		mineLexemes.put("mine", Entity.UNKNOWN_SENSE);
		Entity halloEntity = new Entity(mineLexemes, PoS.v);

		Set<Entity> entities;
		wiktionaryEnglish.setIsCaseSensitive(true);
		entities = wiktionaryEnglish.getEntity("mine", PoS.v);
		assertEquals(1, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.toString(), expectedResults.contains(entity.toString()));
			assertEquals(0, halloEntity.compareTo(entity));
		}

		entities = wiktionaryEnglish.getEntity("Mine", PoS.v);
		assertEquals(0, entities.size());

		wiktionaryEnglish.setIsCaseSensitive(false);
		entities = wiktionaryEnglish.getEntity("mine", PoS.v);
		assertEquals(1, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.toString(), expectedResults.contains(entity.toString()));
			assertEquals(0, halloEntity.compareTo(entity));
		}
	}

	@Test
	public void testGetEntityPosSense()
		throws Exception
	{
		Set<String> expectedResults = new HashSet<String>();
		expectedResults.add("mine#-|---v");

		Map<String, String> mineLexemes = new HashMap<String, String>();
		mineLexemes.put("mine", Entity.UNKNOWN_SENSE);
		Entity halloEntity = new Entity(mineLexemes, PoS.v);

		Set<Entity> entities;
		wiktionaryEnglish.setIsCaseSensitive(false);
		entities = wiktionaryEnglish.getEntity("mine", PoS.v, Entity.UNKNOWN_SENSE);
		assertEquals(1, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.toString(), expectedResults.contains(entity.toString()));
			assertEquals(0, halloEntity.compareTo(entity));
		}

		entities = wiktionaryEnglish.getEntity("mine", PoS.v, Entity.UNKNOWN_SENSE);
		assertEquals(1, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.toString(), expectedResults.contains(entity.toString()));
			assertEquals(0, halloEntity.compareTo(entity));
		}

		wiktionaryEnglish.setIsCaseSensitive(true);
		entities = wiktionaryEnglish.getEntity("mine", PoS.v, Entity.UNKNOWN_SENSE);
		assertEquals(1, entities.size());
		for (Entity entity : entities) {
			assertTrue(entity.toString(), expectedResults.contains(entity.toString()));
			assertEquals(0, halloEntity.compareTo(entity));
		}

		entities = wiktionaryEnglish.getEntity("mine", PoS.v, Entity.UNKNOWN_SENSE);
	}

	@Test
	public void testGetRelatedLexemes()
		throws Exception
	{

		Set<String> expectedAntonyms = new HashSet<String>();
		expectedAntonyms.add("rot");
		expectedAntonyms.add("gelb");
		expectedAntonyms.add("blau");

		Set<String> expectedSynonyms = new HashSet<String>();
		expectedSynonyms.add("orangen");
		expectedSynonyms.add("orangefarben");
		expectedSynonyms.add("orangefarbig");
		expectedSynonyms.add("antiquiert");
		expectedSynonyms.add("gelbrot");
		expectedSynonyms.add("rotgelb");

		Set<String> antonyms;
		wiktionaryGerman.setIsCaseSensitive(false);
		antonyms = wiktionaryGerman.getRelatedLexemes("orange", PoS.adj, Entity.UNKNOWN_SENSE,
				LexicalRelation.antonymy);
		assertEquals(expectedAntonyms.size(), antonyms.size());
		int i = 0;
		for (String antonym : antonyms) {
			assertTrue(antonym, expectedAntonyms.contains(antonym));
			i++;
		}
		assertEquals(3, i);

		antonyms = wiktionaryGerman.getRelatedLexemes("Orange", PoS.adj, Entity.UNKNOWN_SENSE,
				LexicalRelation.antonymy);
		assertEquals(expectedAntonyms.size(), antonyms.size());
		i = 0;
		for (String antonym : antonyms) {
			assertTrue(antonym, expectedAntonyms.contains(antonym));
			i++;
		}
		assertEquals(3, i);

		Set<String> synonyms = wiktionaryGerman.getRelatedLexemes("orange", PoS.adj,
				Entity.UNKNOWN_SENSE, LexicalRelation.synonymy);
		assertEquals(expectedSynonyms.size(), synonyms.size());
		int j = 0;
		for (String synonym : synonyms) {
			assertTrue(synonym, expectedSynonyms.contains(synonym));
			j++;
		}
		assertEquals(6, j);

		synonyms = wiktionaryGerman.getRelatedLexemes("Orange", PoS.adj, Entity.UNKNOWN_SENSE,
				LexicalRelation.synonymy);
		assertEquals(expectedSynonyms.size(), synonyms.size());
		j = 0;
		for (String synonym : synonyms) {
			assertTrue(synonym, expectedSynonyms.contains(synonym));
			j++;
		}
		assertEquals(6, j);

		wiktionaryGerman.setIsCaseSensitive(true);
		antonyms = wiktionaryGerman.getRelatedLexemes("orange", PoS.adj, Entity.UNKNOWN_SENSE,
				LexicalRelation.antonymy);
		assertEquals(expectedAntonyms.size(), antonyms.size());
		i = 0;
		for (String antonym : antonyms) {
			assertTrue(antonym, expectedAntonyms.contains(antonym));
			i++;
		}
		assertEquals(3, i);

		antonyms = wiktionaryGerman.getRelatedLexemes("Orange", PoS.adj, Entity.UNKNOWN_SENSE,
				LexicalRelation.antonymy);
		assertEquals(0, antonyms.size());

		synonyms = wiktionaryGerman.getRelatedLexemes("orange", PoS.adj, Entity.UNKNOWN_SENSE,
				LexicalRelation.synonymy);
		assertEquals(expectedSynonyms.size(), synonyms.size());
		j = 0;
		for (String synonym : synonyms) {
			assertTrue(synonym, expectedSynonyms.contains(synonym));
			j++;
		}
		assertEquals(6, j);

		synonyms = wiktionaryGerman.getRelatedLexemes("Orange", PoS.adj, Entity.UNKNOWN_SENSE,
				LexicalRelation.synonymy);
		assertEquals(0, synonyms.size());
	}

	@Test
	public void testGetSemanticRelations()
		throws Exception
	{
		Map<String, String> autoLexemes = new HashMap<String, String>();
		autoLexemes.put("Auto", Entity.UNKNOWN_SENSE);
		Entity autoEntity = new Entity(autoLexemes, PoS.n);

		Map<String, String> autoLexemes2 = new HashMap<String, String>();
		autoLexemes2.put("auto", Entity.UNKNOWN_SENSE);
		Entity autoEntity2 = new Entity(autoLexemes2, PoS.n);

		Set<String> expectedHypernyms = new HashSet<String>();
		expectedHypernyms.add("Kraftfahrzeug#-|---n");
		expectedHypernyms.add("Verkehrsmittel#-|---n");

		Set<String> expectedHyponyms = new HashSet<String>();
		expectedHyponyms.add("Jahreswagen#-|---n");
		expectedHyponyms.add("Cabrio#-|---n");
		expectedHyponyms.add("Limousine#-|---n");
		expectedHyponyms.add("Kombi#-|---n");
		expectedHyponyms.add("Feuerwehrauto#-|---n");
		expectedHyponyms.add("Geländewagen#-|---n");

//		Set<String> expectedMeronyms = new HashSet<String>();
//		Set<String> expectedHolonyms = new HashSet<String>();

		Set<Entity> hypernyms;
		wiktionaryGerman.setIsCaseSensitive(false);
		hypernyms = wiktionaryGerman.getRelatedEntities(autoEntity, SemanticRelation.hypernymy);
		assertEquals(2, hypernyms.size());
		for (Entity hypernym : hypernyms) {
			System.out.println(hypernym);
			assertTrue(hypernym.toString(), expectedHypernyms.contains(hypernym.toString()));
		}

		hypernyms = wiktionaryGerman.getRelatedEntities(autoEntity2, SemanticRelation.hypernymy);
		assertEquals(2, hypernyms.size());
		for (Entity hypernym : hypernyms) {
			System.out.println(hypernym);
			assertTrue(hypernym.toString(), expectedHypernyms.contains(hypernym.toString()));
		}

		wiktionaryGerman.setIsCaseSensitive(true);
		hypernyms = wiktionaryGerman.getRelatedEntities(autoEntity, SemanticRelation.hypernymy);
		assertEquals(2, hypernyms.size());
		for (Entity hypernym : hypernyms) {
			System.out.println(hypernym);
			assertTrue(hypernym.toString(), expectedHypernyms.contains(hypernym.toString()));
		}

		hypernyms = wiktionaryGerman.getRelatedEntities(autoEntity2, SemanticRelation.hypernymy);
		assertEquals(0, hypernyms.size());

		// ////////////////////////////////////////////////////////////////////////////////////////////

		wiktionaryGerman.setIsCaseSensitive(false);
		Set<Entity> hyponyms = wiktionaryGerman.getRelatedEntities(autoEntity,
				SemanticRelation.hyponymy);
		assertEquals(6, hyponyms.size());
		for (Entity hyponym : hyponyms) {
			System.out.println(hyponym);
			assertTrue(hyponym.toString(), expectedHyponyms.contains(hyponym.toString()));
		}

		hyponyms = wiktionaryGerman.getRelatedEntities(autoEntity2, SemanticRelation.hyponymy);
		assertEquals(6, hyponyms.size());
		for (Entity hyponym : hyponyms) {
			System.out.println(hyponym);
			assertTrue(hyponym.toString(), expectedHyponyms.contains(hyponym.toString()));
		}

		wiktionaryGerman.setIsCaseSensitive(true);
		hyponyms = wiktionaryGerman.getRelatedEntities(autoEntity, SemanticRelation.hyponymy);
		assertEquals(6, hyponyms.size());
		for (Entity hyponym : hyponyms) {
			System.out.println(hyponym);
			assertTrue(hyponym.toString(), expectedHyponyms.contains(hyponym.toString()));
		}

		hyponyms = wiktionaryGerman.getRelatedEntities(autoEntity2, SemanticRelation.hyponymy);
		assertEquals(0, hyponyms.size());
		// ///////////////////////////////////////////////////////////////////////////////////////////

		Set<Entity> meronyms = wiktionaryGerman.getRelatedEntities(autoEntity,
				SemanticRelation.meronymy);
		assertEquals(0, meronyms.size());

		Set<Entity> holonyms = wiktionaryGerman.getRelatedEntities(autoEntity,
				SemanticRelation.holonymy);
		assertEquals(0, holonyms.size());

		// //////////////////////////////////////////////////////////////////////////////////////////

		Map<String, String> orangeLexemes = new HashMap<String, String>();
		orangeLexemes.put("orange", Entity.UNKNOWN_SENSE);
		Entity orangeEntity = new Entity(orangeLexemes, PoS.adj);

		Map<String, String> orangeLexemes2 = new HashMap<String, String>();
		orangeLexemes2.put("Orange", Entity.UNKNOWN_SENSE);
		Entity orangeEntity2 = new Entity(orangeLexemes2, PoS.adj);

		expectedHypernyms.clear();
		expectedHypernyms.add("Farbe#-|---n");

		expectedHyponyms.clear();
		expectedHyponyms.add("apricot#-|---adj");
		// These may be not in the new wiktionary database any more.
		// expectedHyponyms.add("gelborange#- ---a");
		// expectedHyponyms.add("m\u00f6hrenfarben#- ---a");
		// expectedHyponyms.add("safranfarben#- ---a");

		wiktionaryGerman.setIsCaseSensitive(false);
		hypernyms = wiktionaryGerman.getRelatedEntities(orangeEntity, SemanticRelation.hypernymy);
		assertEquals(1, hypernyms.size());
		for (Entity hypernym : hypernyms) {
			System.out.println(hypernym);
			System.out.println(expectedHypernyms);
			assertTrue(hypernym.toString(), expectedHypernyms.contains(hypernym.toString()));
		}

		hypernyms = wiktionaryGerman.getRelatedEntities(orangeEntity2, SemanticRelation.hypernymy);
		assertEquals(1, hypernyms.size());
		for (Entity hypernym : hypernyms) {
			System.out.println(hypernym);
			System.out.println(expectedHypernyms);
			assertTrue(hypernym.toString(), expectedHypernyms.contains(hypernym.toString()));
		}

		wiktionaryGerman.setIsCaseSensitive(true);
		hypernyms = wiktionaryGerman.getRelatedEntities(orangeEntity, SemanticRelation.hypernymy);
		assertEquals(1, hypernyms.size());
		for (Entity hypernym : hypernyms) {
			System.out.println(hypernym);
			System.out.println(expectedHypernyms);
			assertTrue(hypernym.toString(), expectedHypernyms.contains(hypernym.toString()));
		}

		hypernyms = wiktionaryGerman.getRelatedEntities(orangeEntity2, SemanticRelation.hypernymy);
		assertEquals(0, hypernyms.size());
		// //////////////////////////////////////////////////////////////////////////////////////////////////////

		wiktionaryGerman.setIsCaseSensitive(false);
		hyponyms = wiktionaryGerman.getRelatedEntities(orangeEntity, SemanticRelation.hyponymy);
		// can i know the exact date of wiktionary DB so that i can judge the result?
		assertEquals(1, hyponyms.size());
		for (Entity hyponym : hyponyms) {
			System.out.println(hyponym);
			assertTrue(hyponym.toString(), expectedHyponyms.contains(hyponym.toString()));
		}

		hyponyms = wiktionaryGerman.getRelatedEntities(orangeEntity2, SemanticRelation.hyponymy);
		// can i know the exact date of wiktionary DB so that i can judge the result?
		assertEquals(1, hyponyms.size());
		for (Entity hyponym : hyponyms) {
			System.out.println(hyponym);
			assertTrue(hyponym.toString(), expectedHyponyms.contains(hyponym.toString()));
		}

		wiktionaryGerman.setIsCaseSensitive(true);
		hyponyms = wiktionaryGerman.getRelatedEntities(orangeEntity, SemanticRelation.hyponymy);
		// can i know the exact date of wiktionary DB so that i can judge the result?
		assertEquals(1, hyponyms.size());
		for (Entity hyponym : hyponyms) {
			System.out.println(hyponym);
			assertTrue(hyponym.toString(), expectedHyponyms.contains(hyponym.toString()));
		}

		hyponyms = wiktionaryGerman.getRelatedEntities(orangeEntity2, SemanticRelation.hyponymy);
		// can i know the exact date of wiktionary DB so that i can judge the result?
		assertEquals(0, hyponyms.size());
	}

	@Test
	public void testGetGloss()
		throws Exception
	{

		Set<Entity> entities;
		wiktionaryEnglish.setIsCaseSensitive(false);
		entities = wiktionaryEnglish.getEntity("mine");
		String caseInSensitiveGloss = new String();
		for (Entity e : entities) {
			System.out.println(e);
			System.out.println(wiktionaryEnglish.getGloss(e));
			caseInSensitiveGloss += wiktionaryEnglish.getGloss(e);
		}

		String caseSensitiveGloss = new String();
		wiktionaryEnglish.setIsCaseSensitive(true);
		entities = wiktionaryEnglish.getEntity("mine");
		for (Entity e : entities) {
			System.out.println(e);
			System.out.println(wiktionaryEnglish.getGloss(e));
			caseSensitiveGloss += wiktionaryEnglish.getGloss(e);
		}

		assertTrue(caseSensitiveGloss.equals(caseInSensitiveGloss));
	}

	@Test
	public void testGetEntities()
		throws Exception
	{
		wiktionaryGerman.setIsCaseSensitive(false);
		int i = 0;
		for (Entity entity : wiktionaryGerman.getEntities()) {
			entity.getId();
			i++;
		}
		assertEquals(88436, i);

		wiktionaryGerman.setIsCaseSensitive(true);
		i = 0;
		for (Entity entity : wiktionaryGerman.getEntities()) {
			entity.getId();
			i++;
		}
		assertEquals(88436, i);
	}

	@Test
	public void testGetNumberOfEntities()
		throws Exception
	{
		wiktionaryGerman.setIsCaseSensitive(false);
		assertEquals(88436, wiktionaryGerman.getNumberOfEntities());

		wiktionaryGerman.setIsCaseSensitive(true);
		assertEquals(88436, wiktionaryGerman.getNumberOfEntities());
	}
}