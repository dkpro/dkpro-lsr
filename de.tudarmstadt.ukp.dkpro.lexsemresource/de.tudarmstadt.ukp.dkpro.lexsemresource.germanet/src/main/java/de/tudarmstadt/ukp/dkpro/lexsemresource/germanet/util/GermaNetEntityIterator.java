package de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util;

import java.util.Iterator;

import org.tud.sir.gn.Synset;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterator;

/**
 * @author Anouar
 *
 */
public class GermaNetEntityIterator extends LexicalSemanticEntityIterator {

	private Iterator synsets;
	
	public GermaNetEntityIterator(Iterator synsets){
		this.synsets = synsets;
	}

    @Override
	public boolean hasNext() {
		return synsets.hasNext();
	}

	@Override
	public Entity next() {
        Synset synset = (Synset) synsets.next();
        return GermaNetUtils.synsetToEntity(synset);
	}

}
