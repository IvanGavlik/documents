package eu.javaspecialists.courses.datastructures.ch6_maps;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Creating maps of numbers
 * computeIfAbsent() for List of values
 */
public class _6_3_MapsOfNumbers {
  public static void main(String... args) {
    int[] numbers = ThreadLocalRandom.current().ints(1000, 0, 20).toArray();

    System.out.println("getOrDefault and put");
    Map<Integer, Long> map1 = new HashMap<>();
    for (int number : numbers) {
      long count = map1.getOrDefault(number, 0L);
      map1.put(number, ++count);
    }
    map1.forEach((number, count) -> System.out.println(number + " -> " + count));

    System.out.println("compute()");
    Map<Integer, Long> map2 = new HashMap<>();
    for (int number : numbers) {
      map2.compute(number, (key, count) -> {
        if (count == null) return 0L;
        return count + 1;
      });
    }
    map2.forEach((number, count) -> System.out.println(number + " -> " + count));

    System.out.println("stream()");
    Map<Integer, Long> map3 = IntStream.of(numbers)
        .mapToObj(Integer::valueOf)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    map3.forEach((number, count) -> System.out.println(number + " -> " + count));


  }
}
