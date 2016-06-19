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
package de.tudarmstadt.ukp.dkpro.lexsemresource.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.ResourceLoaderException;

/**
 * Class for loading diverse resources, implementing the LexicalSemanticResource
 * interface, with fewer parameters.
 *
 * @author Richard Eckart de Castilho
 */
public class ResourceFactory
{
	public static final String ENV_DKPRO_HOME = "DKPRO_HOME";
	public final static String CONFIG_FILE = "resources.xml";

	private static ResourceFactory loader;

	private FileSystemXmlApplicationContext context;

	public static synchronized ResourceFactory getInstance()
		throws ResourceLoaderException
	{
		if (loader == null) {
			List<String> locs = new ArrayList<String>();
			URL resourceXmlUrl = null;

			// Check in workspace
			try {
				File f = new File(getWorkspace(), CONFIG_FILE);
				if (f.isFile()) {
					try {
						resourceXmlUrl = f.toURI().toURL();
					}
					catch (MalformedURLException e) {
						throw new ResourceLoaderException(e);
					}
				}
				locs.add(f.getAbsolutePath());
			}
			catch (IOException e) {
				locs.add("DKPro workspace not available");
			}

			// Check in classpath
			if (resourceXmlUrl == null) {
				resourceXmlUrl = ResourceFactory.class
						.getResource(CONFIG_FILE);
				locs.add("Classpath: " + CONFIG_FILE);
			}

			// Check in default file system location
			if (resourceXmlUrl == null && new File(CONFIG_FILE).isFile()) {
				try {
					resourceXmlUrl = new File(CONFIG_FILE).toURI().toURL();
				}
				catch (MalformedURLException e) {
					throw new ResourceLoaderException(e);
				}
				locs.add(new File(CONFIG_FILE).getAbsolutePath());
			}

			// Bail out if still not found
			if (resourceXmlUrl == null) {
				throw new ResourceLoaderException(
						"Unable to locate configuration file [" + CONFIG_FILE
								+ "] in " + locs.toString());
			}

			loader = new ResourceFactory(resourceXmlUrl.toString());
		}
		return loader;
	}

	/**
	 * Constructor parameterized by the path to the configuration file.
	 *
	 * @param location location of the configuration file.
	 */
	public ResourceFactory(String location)
	{
		context = new FileSystemXmlApplicationContext(location);
	}

	/**
	 * Standard function to get a resource object specified by name and
	 * language.
	 *
	 * @param name
	 *            String
	 * @param lang
	 *            String
	 * @return LexicalSemanticResource
	 * @throws ResourceLoaderException
	 */
	public LexicalSemanticResource get(String name, String lang)
		throws ResourceLoaderException
	{
		return (LexicalSemanticResource) context.getBean(name + "-" + lang,
				LexicalSemanticResource.class);
	}

	/**
	 * @return All registered resources. ResourceLoaderExceptions are catched
	 *         and ignored to all for easy iteration over all resources runnalbe
	 *         on the current system.
	 */
	public Collection<LexicalSemanticResource> getAll()
	{
		return context.getBeansOfType(LexicalSemanticResource.class).values();
	}

	/**
	 * Get the workspace directory.
	 *
	 * @return the workspace directory.
	 * @throws IOException if the workspace cannot be obtained
	 */
	private static
	File getWorkspace()
	throws IOException
	{
		if (System.getenv(ENV_DKPRO_HOME) != null) {
			File f = new File(System.getenv(ENV_DKPRO_HOME));
			return new File(f, ResourceFactory.class.getName());
		}

		throw new IOException("Environment variable ["+ENV_DKPRO_HOME+"] not set");
	}
}
