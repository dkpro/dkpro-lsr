package de.tudarmstadt.ukp.dkpro.lexsemresource.germanet.util;

import java.util.Iterator;
import java.util.Set;

import org.tud.sir.gn.GermaNetObject;
import org.tud.sir.util.Constant;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;


/**
 * @author Anouar
 *
 */
public class GermaNetEntityIterable extends LexicalSemanticEntityIterable{


	private Set synsets;
	
	@SuppressWarnings("unchecked")
    public GermaNetEntityIterable(GermaNetObject gnObject){
		this.synsets = gnObject.getAllSynsets(Constant.ADJECTIVE);
		this.synsets.addAll(gnObject.getAllSynsets(Constant.VERB));
		this.synsets.addAll(gnObject.getAllSynsets(Constant.NOUN));
	}
	
	@Override
	public Iterator<Entity> iterator() {
		return new GermaNetEntityIterator(synsets.iterator());
	}

}
