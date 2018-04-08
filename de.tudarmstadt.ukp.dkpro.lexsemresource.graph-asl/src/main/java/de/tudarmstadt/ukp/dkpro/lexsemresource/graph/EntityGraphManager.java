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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;

/**
 * The entity graph manager implements real singletons for entity graphs. There should be no way to
 * construct an entity graph that circumvents the manager.
 *
 * @author zesch
 *
 */
public class EntityGraphManager
{

	private static Map<String, EntityGraph> entityGraphMap = new HashMap<String, EntityGraph>();

	public enum EntityGraphType
	{
		JGraphT, JUNG
	}

    public static EntityGraph getEntityGraph(LexicalSemanticResource lsr, EntityGraphType type)
        throws LexicalSemanticResourceException
    {
        return getEntityGraph(lsr, type, null);
    }

    public static EntityGraph getEntityGraph(LexicalSemanticResource lsr, EntityGraphType type,
            File aGraphDirectory)
        throws LexicalSemanticResourceException
    {
		String graphID = getGraphID(lsr, "");
		if (!entityGraphMap.containsKey(graphID)) {
			EntityGraph entityGraph = null;
			if (type.equals(EntityGraphType.JGraphT)) {
				EntityGraphJGraphT entityGraphJGraphT = new EntityGraphJGraphT(aGraphDirectory);
				entityGraph = entityGraphJGraphT.getEntityGraphJGraphT(lsr, lsr.getEntities(),
						"", lsr.getNumberOfEntities());
			}
			else if (type.equals(EntityGraphType.JUNG)) {
				// TODO JUNG graphes have different factory methods - that should not be the case -
				// JUNG factoy methods should be modelled after JGraphT
				EntityGraphJUNG entityGraphJung = new EntityGraphJUNG();
				entityGraph = entityGraphJung.getEntityGraphJUNG(lsr);
			}

			entityGraphMap.put(graphID, entityGraph);
		}

		return entityGraphMap.get(graphID);
	}

	public static EntityGraph getEntityGraph(LexicalSemanticResource lsr,
			Iterable<Entity> nodesToConsider, String nameSuffix, EntityGraphType type)
		throws LexicalSemanticResourceException
	{
		String graphID = getGraphID(lsr, nameSuffix);
		if (!entityGraphMap.containsKey(graphID)) {
			EntityGraph entityGraph = null;
			if (type.equals(EntityGraphType.JGraphT)) {
				EntityGraphJGraphT entityGraphJGraphT = new EntityGraphJGraphT();
				entityGraph = entityGraphJGraphT.getEntityGraphJGraphT(lsr, nodesToConsider,
						nameSuffix, -1);
			}
			else if (type.equals(EntityGraphType.JUNG)) {
				// TODO JUNG graphes have different factory methods - that should not be the case -
				// JUNG factoy methods should be modelled after JGraphT
				EntityGraphJUNG entityGraphJung = new EntityGraphJUNG();
				entityGraph = entityGraphJung.getEntityGraphJUNG(lsr);
			}

			entityGraphMap.put(graphID, entityGraph);
		}

		return entityGraphMap.get(graphID);
	}

	private static String getGraphID(LexicalSemanticResource lsr, String nameSuffix)
	{
		return lsr.getResourceName() + lsr.getResourceVersion() + nameSuffix;
	}
}
