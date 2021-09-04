package eu.javaspecialists.courses.datastructures.ch2_lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enumeration bugs
 * Fail fast collection
 * forEach()
 */
public class RemoveIfVsStream {
  public static void main(String... args) {
    String[] initial = {"floatie", "sunblock", "rage", "razor",
        "umbrella", "bucket", "raki"};

    List<String> beachToys = new ArrayList<>();
    Collections.addAll(beachToys, initial);

    List<String> filteredBeachToys = beachToys.stream()
        .filter(toy -> !toy.startsWith("ra"))
        .collect(Collectors.toUnmodifiableList());

    System.out.println("beachToys = " + beachToys);
    System.out.println("filteredBeachToys = " + filteredBeachToys);

    beachToys.removeIf(toy -> toy.startsWith("ra")); // O(n)
    System.out.println("After removeIf()");
    System.out.println("beachToys = " + beachToys);
  }
}
