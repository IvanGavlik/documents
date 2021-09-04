package eu.javaspecialists.courses.datastructures.ch4_sets;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.IntStream;

/**
 * Thread-safe sorted set
 */
public class _4_3_ConcurrentSkipListSet {
  public static void main(String... args) {
    long time = System.nanoTime();
    try {
      Set<Integer> set = new ConcurrentSkipListSet<>();
          //Collections.synchronizedSet(new TreeSet<>());
      IntStream.range(0, 1_000_000)
          .parallel()
          .forEach(set::add); // exception or worse
      System.out.println(set.size());
    } finally {
      time = System.nanoTime() - time;
      System.out.printf("time = %dms%n", (time / 1_000_000));
    }
  }
}
