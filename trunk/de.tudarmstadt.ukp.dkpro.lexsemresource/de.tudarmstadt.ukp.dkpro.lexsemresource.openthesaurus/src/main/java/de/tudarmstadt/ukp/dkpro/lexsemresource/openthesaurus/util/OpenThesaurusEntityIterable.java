package de.tudarmstadt.ukp.dkpro.lexsemresource.openthesaurus.util;

import java.util.Iterator;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticEntityIterable;
import de.tudarmstadt.ukp.openthesaurus.api.OpenThesaurus;
import de.tudarmstadt.ukp.openthesaurus.api.Synset;
import de.tudarmstadt.ukp.openthesaurus.exception.OpenThesaurusException;

/**
 * 
 * @author chebotar
 *
 */
public class OpenThesaurusEntityIterable extends LexicalSemanticEntityIterable {
	
	private Set<Synset> synsets;
	
    public OpenThesaurusEntityIterable(OpenThesaurus openThesaurus) throws OpenThesaurusException{
		this.synsets = openThesaurus.getAllSynsets();		
	}
    
	@Override
	public Iterator<Entity> iterator() {
		return new OpenThesaurusEntityIterator(synsets.iterator());		
	}

}
