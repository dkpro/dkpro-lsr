/*******************************************************************************
 * Copyright 2016
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
