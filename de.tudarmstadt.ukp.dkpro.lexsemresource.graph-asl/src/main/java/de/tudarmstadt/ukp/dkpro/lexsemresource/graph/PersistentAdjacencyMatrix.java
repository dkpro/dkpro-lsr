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
package de.tudarmstadt.ukp.dkpro.lexsemresource.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;

/**
 * A matrix holding the parenthood relations in the resource.
 * It uses the Matrix Toolkits for Java (MTJ) library for efficiency.
 * The Random Walks then read this matrix for finding and burning children of entities.
 * It supports persistence, i.e., when it first runs, it creates a file holding the essential
 * adjacency information, from which an adjacency matrix object will be quickly loaded on
 * the next runs
 *
 * @author garoufi
 *
 */
public class PersistentAdjacencyMatrix {
	private Log logger = LogFactory.getLog(getClass());
	public DirectedGraph<Entity,DefaultEdge> entityGraph;
	private LexicalSemanticResource lexSemResource;
	int resourceSize;
	String resourceName;
	FlexCompRowMatrix flexAdjMatrix;

	Map<Entity, Integer> entityIndex;
	Map<Integer, Entity> indexToEntity;

	public PersistentAdjacencyMatrix(LexicalSemanticResource resource)
	throws LexicalSemanticResourceException {

		this.lexSemResource = resource;
		this.resourceName = resource.getResourceName();

		// check if indexing of entities has already been done; if not, do it now
		File persistentEntIndex = new File("EntityIndexSer" + "_" + resourceName);
		File persistentIndexToEnt = new File("IndexToEntitySer" + "_" + resourceName);
		if (!persistentEntIndex.canRead() || !persistentIndexToEnt.canRead()) {
			indexEntities();
		}
		else {
			loadIndices();
		}

		this.resourceSize = entityIndex.size();
		logger.info("Resource size: " + resourceSize + "\n");

		// check if adjacency data have already been saved; if not, save them now
		File persistentAdj = new File("PersistentAdjacencies" + "_" + resourceName);
		if (!persistentAdj.canRead()) {
			saveAdjacencies();
		}

		// finally load the adjacency matrix as an object
		loadAdjMatrix();
	}

	// index entities
	// this has to be done as a separate step and cannot be performed on the fly,
	// since at the moment we have no other way to determine the size of the resource
	// (lexSemResource.getNumberOfEntities() is not reliable and gives wrong number for Wiktionary)
	private void indexEntities() throws LexicalSemanticResourceException {

		entityIndex = new HashMap<Entity, Integer>();
		indexToEntity = new HashMap<Integer, Entity>();

		logger.info("Indexing entities.");
		Iterator<Entity> entityIter = lexSemResource.getEntities().iterator();
		int index = -1;
		while (entityIter.hasNext()) {
			Entity entity = entityIter.next();

			// check whether the entity has already been indexed. if not, index it now.
			if (!entityIndex.keySet().contains(entity)) {
				index ++;
				entityIndex.put(entity, index);
				indexToEntity.put(index, entity);
			}

			// print progress:
			if (index % 10000 == 0) {
				logger.info("Index at " + index);
			}
		}
		logger.info("Indexing complete. Index runs from 0 to " + index);

		// now save the index maps:

		logger.info("Serializing the indices...");

		FileOutputStream fosEntIndex = null;
		ObjectOutputStream outEntIndex = null;

		FileOutputStream fosIndexEnt = null;
		ObjectOutputStream outIndexEnt = null;

		try
		{
			fosEntIndex = new FileOutputStream("EntityIndexSer" + "_" + resourceName);
			outEntIndex = new ObjectOutputStream(fosEntIndex);
			outEntIndex.writeObject(entityIndex);
			outEntIndex.close();

			fosIndexEnt = new FileOutputStream("IndexToEntitySer" + "_" + resourceName);
			outIndexEnt = new ObjectOutputStream(fosIndexEnt);
			outIndexEnt.writeObject(indexToEntity);
			outIndexEnt.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}

		logger.info("Indices serialized.");
	}

	/**
	 * Saves the adjacency data into a file.
	 * Each line contains the indices of the children of the entity whose index corresponds
	 * to the line's number (starting from 0)
	 */
	private void saveAdjacencies() {

		logger.info("Saving adjacencies...");

		try{
			// Create file
			FileWriter fstream = new FileWriter("PersistentAdjacencies" + "_" + resourceName);
			BufferedWriter out = new BufferedWriter(fstream);

			for (int row = 0; row < resourceSize; row ++) {

				// find which entity has the current index:
				Entity entity = indexToEntity.get(row);

				Set<Entity> children = lexSemResource.getChildren(entity);

				// if the entity has children, find their indices and write them down:
				if (children.size() != 0) {
					for (Entity child : children) {
						int childIndex = entityIndex.get(child);
						out.write(childIndex + " ");
					}
				}

				// the next entity will come in the next line:
				out.newLine();

				// print progress
				if (row % 10000 == 0) {
					logger.info("Progress: " + (100 * row / resourceSize) + "%");
				}
			}
			//Close the output stream
			out.close();
		}
		catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		logger.info("Adjacencies saved to file.\n");

	}

	/**
	  * Read the file with the adjacencies and load it into a sparse matrix object
	  */
	public FlexCompRowMatrix loadAdjMatrix() {

		logger.info("Loading the adjacency matrix...");

		// initialize adjacency matrix
		flexAdjMatrix = new FlexCompRowMatrix(resourceSize, resourceSize);
		int row = 0;

		try {
			// Open the file
			FileInputStream fstream = new FileInputStream("PersistentAdjacencies" + "_" + resourceName);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {

				// if the line is not empty, read it and fill the corresponding adjacency matrix entries:
				if (!strLine.equals("")) {
					String[] adjacencies = strLine.split(" ");
					for (String adjacency : adjacencies) {
						int col = Integer.parseInt(adjacency);
						flexAdjMatrix.set(row, col, 1);
					}
				}
				row ++;

				// print progress
				if (row % 10000 == 0) {
					logger.info("Progress: " + (100 * row / resourceSize) + "%");
				}
			}

			//Close the input stream
			in.close();

		//Catch exception if any
		}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}

		logger.info("Adjacency matrix loaded from file.\n");
		return flexAdjMatrix;
	}

	// deserialize the index maps:
	@SuppressWarnings("unchecked")
    public void loadIndices() {

		logger.info("Loading the indices...");

		FileInputStream finEntIndex = null;
		ObjectInputStream inEntIndex = null;

		FileInputStream finIndexEnt = null;
		ObjectInputStream inIndexEnt = null;

		try
		{
			finEntIndex = new FileInputStream("EntityIndexSer" + "_" + resourceName);
			inEntIndex = new ObjectInputStream(finEntIndex);
			entityIndex = (Map<Entity, Integer>) inEntIndex.readObject();
			inEntIndex.close();

			finIndexEnt = new FileInputStream("IndexToEntitySer" + "_" + resourceName);
			inIndexEnt = new ObjectInputStream(finIndexEnt);
			indexToEntity = (Map<Integer, Entity>) inIndexEnt.readObject();
			inIndexEnt.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		logger.info("Indices loaded.");

	}

	// read the matrix and find the children of the source:
	public Set<Entity> getAdjacencies(Entity source) {

		Set<Entity> childrenSet = new HashSet<Entity>();
		int sourceIndex = entityIndex.get(source);

		for (int col = 0; col < resourceSize; col ++) {
			if (flexAdjMatrix.get(sourceIndex, col) == 1) {
				Entity child = indexToEntity.get(col);
				childrenSet.add(child);
			}
		}
		return childrenSet;
	}


}


