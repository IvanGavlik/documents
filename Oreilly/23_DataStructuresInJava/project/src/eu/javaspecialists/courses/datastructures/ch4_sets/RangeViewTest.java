package eu.javaspecialists.courses.datastructures.ch4_sets;

import java.util.SortedSet;
import java.util.TreeSet;

public class RangeViewTest {
  public static void main(String... args) {
    TreeSet<Integer> numbers = new TreeSet<>();
    for (int i = 0; i < 10; i++) {
      numbers.add(i);
    }
    SortedSet<Integer> head = numbers.headSet(4);
    SortedSet<Integer> tail = numbers.tailSet(6);
    SortedSet<Integer> subSet = numbers.subSet(3, 7);

    System.out.println("head = " + head);
    System.out.println("tail = " + tail);
    System.out.println("subSet = " + subSet);

    numbers.add(10);
    numbers.remove(3);
    numbers.remove(6);

    System.out.println("head = " + head);
    System.out.println("tail = " + tail);
    System.out.println("subSet = " + subSet);
  }
}
