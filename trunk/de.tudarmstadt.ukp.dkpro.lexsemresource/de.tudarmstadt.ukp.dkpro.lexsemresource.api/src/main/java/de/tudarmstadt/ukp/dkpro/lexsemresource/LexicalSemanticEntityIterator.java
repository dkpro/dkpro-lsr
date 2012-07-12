package de.tudarmstadt.ukp.dkpro.lexsemresource;

import java.util.Iterator;

public abstract class LexicalSemanticEntityIterator implements Iterator<Entity> {

    public abstract boolean hasNext();

    public abstract Entity next();

    public void remove() {
        throw new UnsupportedOperationException();
    }
}