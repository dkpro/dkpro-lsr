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
package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

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
	 * @throws JWNLException
	 * @throws FileNotFoundException
	 */
	@Test
	public void testGetDomain() throws FileNotFoundException, JWNLException {
        Dictionary dict = Dictionary.getInstance(new FileInputStream(new File(
                "src/main/resources/resource/WordNet_3/wordnet_properties.xml")
                ));
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
				if(dr.getDomain(next)==null) {
                    cntF++;
                }
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
