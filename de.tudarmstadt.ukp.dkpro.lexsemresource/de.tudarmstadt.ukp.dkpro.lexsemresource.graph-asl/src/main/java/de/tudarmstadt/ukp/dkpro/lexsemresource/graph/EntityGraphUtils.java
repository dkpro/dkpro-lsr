/*******************************************************************************
 * Copyright 2012
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class EntityGraphUtils {

    /**
     * Serialize a Map.
     *
     * @param map The map to serialize.
     * @param file The file for saving the map.
     */
    public static void serializeMap(Map<?,?> map, File file) {
        try {
            ObjectOutputStream os = new ObjectOutputStream(
                    new FileOutputStream(file));
            os.writeObject(map);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deserialize a map
     * @param file The file with the map.
     */
    public static Map deserializeMap(File file) {
        Map<?,?> map;
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
            map = (Map<?,?>) is.readObject();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }
}
