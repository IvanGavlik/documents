package eu.javaspecialists.courses.datastructures.ch1_introduction;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Computational Time and Space Complexity
 * O(1), O(n), O(n^2), O(log n), O(n * log n)
 */
public class _1_1_ComputationalTimeComplexity {
  public static void main(String... args) {

    // O(1) - constant time - hash map lookup
    // O(log n) - logarithmic - binary search
    // O(n) - linear - List.removeIf()
    // O(n * log n) - quasilinear - quicksort, mergesort
    ////////
    // O(n^2) - quadratic - removing from ArrayList with iterator, bubble sort
    //...

    ArrayList<Integer> al = new ArrayList<>();
    for (int i = 0; i < 100_000; i++) {
      al.add(i);
    }
    long time = System.nanoTime();
    try {
      for (Iterator<Integer> it = al.iterator(); it.hasNext(); ) {
        int next = it.next();
        if (next % 2 == 1) it.remove();
      }
    } finally {
      time = System.nanoTime() - time;
      System.out.printf("time = %dms%n", (time / 1_000_000));
    }
  }
}
