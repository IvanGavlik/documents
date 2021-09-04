package eu.javaspecialists.courses.datastructures.ch4_sets;

import java.util.Set;

/**
 * Set.of()
 * union with addAll()
 * intersection with retainAll() or stream/filter
 */
public class _4_1_Sets {
  public static void main(String... args) {
    Set<Integer> digits = Set.of(3, 1, 4, 8, 5, 9);
    System.out.println("digits = " + digits);
  }
}
