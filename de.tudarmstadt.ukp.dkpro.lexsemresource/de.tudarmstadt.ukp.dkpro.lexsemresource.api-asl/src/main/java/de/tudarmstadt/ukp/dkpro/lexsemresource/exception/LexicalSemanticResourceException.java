package de.tudarmstadt.ukp.dkpro.lexsemresource.exception;

/**
 * Wraps other exceptions that are thrown inside a resource.
 * 
 * @author zesch
 *
 */
public class LexicalSemanticResourceException extends Exception {

    static final long serialVersionUID = 1L;

    public LexicalSemanticResourceException() {
        super();
    }
    
    public LexicalSemanticResourceException(String txt) {
        super(txt);
    }
    
    public LexicalSemanticResourceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public LexicalSemanticResourceException(Throwable cause) {
        super(cause);
    }

}