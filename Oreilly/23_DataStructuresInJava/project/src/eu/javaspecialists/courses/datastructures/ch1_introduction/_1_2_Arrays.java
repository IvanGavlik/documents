package eu.javaspecialists.courses.datastructures.ch1_introduction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Primitive vs object arrays
 * Multi-dimensional arrays
 */
public class _1_2_Arrays {
  public static void main(String... args) {
    // 1_000_000 * 4 + 12 (object header) + 4 (array length)
    // 4_000_016 bytes
    int[] numbers = new int[1_000_000];

    List<Integer> al = new ArrayList<>(); // 1215487
    // 1215487 * 4 + 12 + 4
    // 64 // ArrayList object
    // 1000000 * 16 = Integer Objects
    // total of 20_862_028
    for (int i = 0; i < 1_000_000; i++) {
      al.add(new Integer(i));
    }

    List<Integer> ll = new LinkedList<>();
    // 1000000 * 24
    // 64 // LinkedList object
    // 1000000 * 16 = Integer Objects
    // total of 40_000_064

    for (int i = 0; i < 1_000_000; i++) {
      ll.add(new Integer(i));
    }

    al.clear();
    System.out.println("al.size() = " + al.size());
  }

  // each node is 24 bytes
  // 12 byte object
  private static class Node<E> {
    E item; // 4 bytes
    Node<E> next; // 4 bytes
    Node<E> prev; // 4 bytes

    Node(Node<E> prev, E element, Node<E> next) {
      this.item = element;
      this.next = next;
      this.prev = prev;
    }
  }

  // each node is 20 bytes ≈ 24 bytes
  // 12 byte object
  private static class Node2<E> {
    E item; // 4 bytes
    Node2<E> next; // 4 bytes

    Node2( E element, Node2<E> next) {
      this.item = element;
      this.next = next;
    }
  }


}
