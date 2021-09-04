package eu.javaspecialists.courses.datastructures.ch3_sorting;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Sorting performance ArrayList vs LinkedList
 * Parallel sorting of ArrayList
 */
public class _3_4_ParallelSorting {
  public static void main(String... args) {
    int[] random = ThreadLocalRandom.current()
        .ints(100_000_000).parallel().toArray();

    int[] sorted = random.clone();
    Arrays.parallelSort(sorted);

    for (int i = 0; i < 10; i++) {
      test("sequential unsorted   ", random, Arrays::sort, int[]::clone);
      test("parallel unsorted     ", random, Arrays::parallelSort, int[]::clone);
      test("sequential sorted     ", sorted, Arrays::sort, data -> data);
      test("parallel sorted       ", sorted, Arrays::parallelSort, data -> data);
    }
  }

  public static void test(String description,
                          int[] data,
                          Consumer<int[]> sortFunction,
                          UnaryOperator<int[]> cloneFunction) {
    System.out.print(description);
    data = cloneFunction.apply(data);
    long time = System.nanoTime();
    try {
      sortFunction.accept(data);
    } finally {
      time = System.nanoTime() - time;
      System.out.printf("%dms%n", (time / 1_000_000));
    }
    data = null;
    for (int i = 0; i < 3; i++) {
      System.gc();
    }
  }
}
