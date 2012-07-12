package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.WordNetResource;

public class DomainResolverTest {

	private static LexicalSemanticResource wordnet;
	private static DomainResolver dr;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			wordnet = new WordNetResource("src/main/resources/resource/WordNet_3/wordnet_properties.xml");
			dr = new DomainResolver();
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Values measured with WN-Domains 3.2 and WN 3.0 Synsets
	 */
	@Test
	public void testGetDomain() {
		Dictionary dict = Dictionary.getInstance();
		assertTrue(2737 >= test(dict,dr,POS.NOUN));
		assertTrue(971 >= test(dict,dr,POS.ADJECTIVE));
		assertTrue(72 >= test(dict,dr,POS.ADVERB));
		assertTrue(361 >= test(dict,dr,POS.VERB));
	}
	
	private static int test(Dictionary dict,DomainResolver dr,POS p) {
		System.out.println("Type: "+p.toString());
		Iterator<Synset> iter;
		int cntF=0;
		try {
			iter = dict.getSynsetIterator(p);
			int cntS=0;
			while(iter.hasNext()) {
				Synset next=iter.next();
				if(dr.getDomain(next)==null) cntF++;
				else {
					//System.out.println(next.toString()+" : "+dr.getDomain(next));
					cntS++;
				}
			}
			System.out.println("Failed: "+cntF+" "+"Succ: "+cntS+" fail quota: "+(100f/(cntF+cntS))*cntF);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		return cntF;
	}

}
