package de.tudarmstadt.ukp.dkpro.lexsemresource;

import java.util.Iterator;

public abstract class LexicalSemanticEntityIterable implements Iterable<Entity> {
        public abstract Iterator<Entity> iterator();
}
