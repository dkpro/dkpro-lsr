package de.tudarmstadt.ukp.dkpro.lexsemresource.core;

import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;

@SuppressWarnings("unused")
public class ResourceFactoryTest
{
	@Test
	@Ignore("We do not want to have all the resources on the Hudson server atm")
	public void testGermaNet()
		throws Exception
	{
		ResourceFactory loader = new ResourceFactory("resources.xml");
		LexicalSemanticResource germanetDe = loader.get("germanet", "de");
	}

	@Test
	@Ignore("We do not want to have all the resources on the Hudson server atm")
	public void testWordNet()
		throws Exception
	{
		ResourceFactory loader = new ResourceFactory("resources.xml");
		LexicalSemanticResource wordnetEn = loader.get("wordnet", "en");
	}

	@Test
	@Ignore("We do not want to have all the resources on the Hudson server atm")
	public void testWiktionary()
		throws Exception
	{
		ResourceFactory loader = new ResourceFactory("resources.xml");
        LexicalSemanticResource wikitionaryEn = loader.get("wiktionary", "en");
		LexicalSemanticResource wikitionaryDe = loader.get("wiktionary", "de");
	}

	@Test
	@Ignore("We do not want to have all the resources on the Hudson server atm")
	public void testWikipedia()
		throws Exception
	{
		ResourceFactory loader = new ResourceFactory("resources.xml");
		LexicalSemanticResource wikipediaDe = loader.get("wikipedia", "de");
		LexicalSemanticResource wikipediaEn = loader.get("wikipedia", "en");
		LexicalSemanticResource wikipediaTest = loader.get("wikipedia", "test");
		LexicalSemanticResource wikipediaCategoryDe = loader.get("wikipedia_category", "de");
		LexicalSemanticResource wikipediaCategoryEn = loader.get("wikipedia_category", "en");
		LexicalSemanticResource wikipediaCategoryTest = loader.get("wikipedia_category", "test");
	}
}
