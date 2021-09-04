package eu.javaspecialists.courses.datastructures.ch2_lists;

import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;

public class ListAccess {
  public static void main(String... args) {
    RandomAccess ra;

    List<Long> numbers = new LinkedList<>();
    for (int i = 0; i < 1_000_000; i++) {
      numbers.add((long)i);
    }
    long total = 0;
    long time = System.nanoTime();
    try {
      for (int i = 0; i < 10_000; i++) {
        total += numbers.get(numbers.size() / 2);
      }
    } finally {
      time = System.nanoTime() - time;
      System.out.printf("time = %dms%n", (time / 1_000_000));
    }
    System.out.println("total = " + total);
  }
}
