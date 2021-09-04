package eu.javaspecialists.courses.datastructures.ch4_sets;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Sorted by natural order
 * Red-black tree
 * Unbalanced tree O(n) vs O(log n)
 * Counting maximum tree depth
 */
public class _4_2_TreeSet {
  public static void main(String... args) {
    // 0 1 2 3 4 5 6 7 8 9
    // bad implementation
    // 0
    //   1
    //     2
    //       3
    //         4
    //           5
    //             6
    //               7
    //                 8
    //                   9

    // red-black tree
    //             3
    //      1             5
    //    0   2        4     7
    //                     6   8
    //                           9

    // hand-balanced tree
    //             3
    //      1             7
    //    0   2        5     8
    //                4  6     9

    Set<Integer> tree = new TreeSet<>();
    // max 5 levels
    Collections.addAll(tree, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    System.out.println(tree);
  }
}
