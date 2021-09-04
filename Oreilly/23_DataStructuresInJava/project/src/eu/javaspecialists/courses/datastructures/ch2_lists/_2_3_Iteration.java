package eu.javaspecialists.courses.datastructures.ch2_lists;

import java.util.*;

/**
 * Enumeration bugs
 * Fail fast collection
 * forEach()
 */
public class _2_3_Iteration {
  public static void main(String... args) {
    Vector<String> beachToys = new Vector<>();
    Collections.addAll(beachToys, "floatie", "sunblock", "rage", "razor",
        "umbrella", "bucket", "raki"); // um - starting with "ra" - are suspect
    beachToys.removeIf(toy -> toy.startsWith("ra")); // O(n)
    System.out.println(beachToys);
  }
}
