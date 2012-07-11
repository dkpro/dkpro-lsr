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
    @SuppressWarnings("unchecked")
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
