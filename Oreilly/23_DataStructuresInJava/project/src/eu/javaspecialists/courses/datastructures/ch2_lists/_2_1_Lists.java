package eu.javaspecialists.courses.datastructures.ch2_lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Arrays.asList()
 * Quick look at the List methods
 * Optional methods
 * asList() vs List.of()
 * RandomAccess
 */
public class _2_1_Lists {
  public static void main(String... args) {
    List<String> names = Arrays.asList("John", "Anton", "Heinz");
    System.out.println("names.getClass().getSimpleName() = " +
        names.getClass().getName());
    System.out.println("names = " + names);
    names.set(0, "Zach");
    System.out.println("names = " + names);
//    names.add("Dirk"); // exception

    List<String> names2 = List.of("John", "Anton", "Heinz");
    System.out.println("names2.getClass().getSimpleName() = " +
        names2.getClass().getName());
//    names2.set(0, "Zach"); // exception

    List<String> names3 = new ArrayList<>();
    names3.add("AK");

    List<Integer> l0 = List.of();
    List<Integer> l1 = List.of(1);
    List<Integer> l2 = List.of(1, 2);
    List<Integer> l3 = List.of(1, 2, 3);

    System.out.println("l0.getClass() = " + l0.getClass());
    System.out.println("l1.getClass() = " + l1.getClass());
    System.out.println("l2.getClass() = " + l2.getClass());
    System.out.println("l3.getClass() = " + l3.getClass());

    List<String> copyOfNames = List.copyOf(names);
  }
}
