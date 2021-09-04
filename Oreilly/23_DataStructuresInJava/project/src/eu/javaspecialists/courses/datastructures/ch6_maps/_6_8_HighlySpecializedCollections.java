package eu.javaspecialists.courses.datastructures.ch6_maps;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 *  EnumSet
 *  EnumMap
 *  IdentityHashMap
 *  Properties
 *  WeakHashMap
 */
public class _6_8_HighlySpecializedCollections {
  public static void main(String... args) {

    Map<String, String> names= new IdentityHashMap<>();
    names.put("Heinz", "Kabutz");
    names.put(new String("Heinz"), "Ketchup");
    System.out.println(names);
  }
}
