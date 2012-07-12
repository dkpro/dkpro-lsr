package de.tudarmstadt.ukp.dkpro.lexsemresource.wordnet.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.ResourceLoaderException;

/**
 * Takes a WN3.0 Synset and returns the domain according to wn-domains-3.2
 * 
 * @author cwirth
 *
 */
public class DomainResolver {

	HashMap<String,String> map;

	public DomainResolver() throws ResourceLoaderException {
		map = getData();
	}

	public String getDomain(Synset syn) {
		String off=Long.toString(syn.getOffset());
		while(off.length()<8)
			off="0"+off;
		if(syn.getPOS().equals(POS.NOUN))
			off="1"+off;
		if(syn.getPOS().equals(POS.VERB))
			off="2"+off;
		if(syn.getPOS().equals(POS.ADJECTIVE))
			off="3"+off;
		if(syn.getPOS().equals(POS.ADVERB))
			off="4"+off;
		return map.get(off);
	}

	private HashMap<String,String> getData() throws ResourceLoaderException {
		HashMap<String,String> map = new HashMap<String,String>();
		HashMap<String,String> synmap = getWNMapData();
		GZIPInputStream  zipstream=null;
		BufferedReader br=null;
		try {
			URL synsetmap = getClass().getResource("/resource/WordNet_3/domains/wndomains.gz");
			File file = new File(synsetmap.toURI());
			zipstream = new GZIPInputStream(new FileInputStream(file));
			br = new BufferedReader(new InputStreamReader(zipstream));
			String line;
			int cf=0,cs=0;
			while((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				//while(data[0].startsWith("0"))
				//	data[0]=data[0].substring(1);
				String[] key = data[0].split("-");
				if(key[1].equals("n"))
					key[0]="1"+key[0];
				if(key[1].equals("v"))
					key[0]="2"+key[0];
				if(key[1].equals("a") || key[1].equals("s"))
					key[0]="3"+key[0];
				if(key[1].equals("r"))
					key[0]="4"+key[0];
				if(synmap.containsKey(key[0])) {
					map.put(synmap.get(key[0]), data[1]);
					cs++;
				} else {
					//System.out.println("Not found: "+data[0]);
					cf++;
				}
			}
			System.out.println("Wordnet domain mapping: successfull: "+cs+" failed: "+cf);
		} catch (IOException e) {
			throw new ResourceLoaderException("Can't open WN-Domains datafile.",e);
		} catch (URISyntaxException e) {
			throw new ResourceLoaderException("Can't find WN-Domains gzip file.",e);
		}
		finally {
			try {
				br.close();
				zipstream.close();
			} catch (IOException e) {
				throw new ResourceLoaderException("Can't close gzip stream.",e);
			}
		}
		return map;
	}

	private HashMap<String,String> getWNMapData() throws ResourceLoaderException {
		HashMap<String,String> map = new HashMap<String,String>();
		GZIPInputStream  zipstream=null;
		BufferedReader br=null;
		try {
			URL synsetmap = getClass().getResource("/resource/WordNet_3/domains/sensemap.gz");
			File file = new File(synsetmap.toURI());
			zipstream = new GZIPInputStream(new FileInputStream(file));
			br = new BufferedReader(new InputStreamReader(zipstream));
			String line;
			while((line = br.readLine()) != null) {
				String[] data = line.split(";");
				map.put(data[1], data[0]);
			}
		} catch (IOException e) {
			throw new ResourceLoaderException("Can't open WN 2.0/3.0 sense mapping datafile.",e);
		} catch (URISyntaxException e) {
			throw new ResourceLoaderException("Can't find WN 2.0/3.0 sense mapping gzip file.",e);
		}
		finally {
			try {
				br.close();
				zipstream.close();
			} catch (IOException e) {
				throw new ResourceLoaderException("Can't close gzip stream.",e);
			}
		}
		return map;
	}

}
