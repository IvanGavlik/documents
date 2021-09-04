package eu.javaspecialists.courses.datastructures.ch3_sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sorting list of Strings
 * Sorting custom classes like Student
 * Comparing ints and longs
 */
public class _3_1_SortingLists {
  public static void main(String... args) {
    List<String> initials = new ArrayList<>();
    Collections.addAll(initials, "AK", "AA", "SV", "GR", "VA");
    initials.sort(null);
  }
}
