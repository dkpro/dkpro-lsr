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
package de.tudarmstadt.ukp.dkpro.lexsemresource.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public final class FileUtils
{
	private static Map<URL, File> urlFileCache;

	static {
		urlFileCache = new HashMap<URL, File>();
	}

	private FileUtils()
	{
		// No instance
	}

	/**
	 * Makes the given stream available as a file. The created file is temporary
	 * and deleted upon normal termination of the JVM. Still the file should be
	 * deleted as soon as possible if it is no longer required. In case the JVM
	 * crashes the file would not be deleted. The source stream is closed by
	 * this operation in all cases.
	 *
	 * @param is
	 *            the source.
	 * @return the file.
	 * @throws IOException
	 *             in case of read or write problems.
	 */
	public static File getStreamAsFile(final InputStream is)
		throws IOException
	{
		OutputStream os = null;
		try {
			final File f = Files.createTempFile("dkpro_stream", "tmp").toFile();
			f.deleteOnExit();
			os = new FileOutputStream(f);
			IOUtils.copy(is, os);
			return f;
		}
		finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Make the given URL available as a file. A temporary file is created and
	 * deleted upon a regular shutdown of the JVM. If the parameter {@code
	 * aCache} is {@code true}, the temporary file is remembered in a cache and
	 * if a file is requested for the same URL at a later time, the same file is
	 * returned again. If the previously created file has been deleted
	 * meanwhile, it is recreated from the URL.
	 *
	 * @param aUrl
	 *            the URL.
	 * @param aCache
	 *            use the cache or not.
	 * @return a file created from the given URL.
	 * @throws IOException
	 *             if the URL cannot be accessed to (re)create the file.
	 */
	public static synchronized File getUrlAsFile(URL aUrl, boolean aCache)
		throws IOException
	{
		// If the URL already points to a file, there is not really much to do.
		if ("file".equals(aUrl.getProtocol())) {
			return new File(aUrl.getPath());
		}

		// Lets see if we already have a file for this URL in our cache. Maybe
		// the file has been deleted meanwhile, so we also check if the file
		// actually still exists on disk.
		File file = urlFileCache.get(aUrl);
		if (!aCache || (file == null) || !file.exists()) {
			// Create a temporary file and try to preserve the file extension
			String suffix = ".temp";
			String name = new File(aUrl.getPath()).getName();
			int suffixSep = name.indexOf(".");
			if (suffixSep != -1) {
				suffix = name.substring(suffixSep + 1);
				name = name.substring(0, suffixSep);
			}

			// Get a temporary file which will be deleted when the JVM shuts
			// down.
			file = Files.createTempFile(name, suffix).toFile();
			file.deleteOnExit();

			// Now copy the file from the URL to the file.
			InputStream is = null;
			OutputStream os = null;
			try {
				is = aUrl.openStream();
				os = new FileOutputStream(file);
				IOUtils.copy(is, os);
			}
			finally {
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(os);
			}

			// Remember the file
			if (aCache) {
				urlFileCache.put(aUrl, file);
			}
		}

		return file;
	}
}
