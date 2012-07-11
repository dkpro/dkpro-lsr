package de.tudarmstadt.ukp.dkpro.lexsemresource.exception;

/**
 * Wraps other exceptions that are thrown inside the ResourceLoader.
 * 
 * @author Florian Schwager
 * 
 */
public class ResourceLoaderException extends Exception {

	private static final long serialVersionUID = -5960803645419721151L;

	public ResourceLoaderException() {
		super();
	}

	public ResourceLoaderException(String msg) {
		super(msg);
	}

	public ResourceLoaderException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ResourceLoaderException(Throwable cause) {
		super(cause);
	}
}
