package eu.javaspecialists.courses.datastructures.ch2_lists;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Safe iteration
 */
public class _2_4_CopyOnWriteArrayList {
  public static void main(String... args) {
    List<String> beachToys = new CopyOnWriteArrayList<>();
    Collections.addAll(beachToys, "floatie", "sunblock", "rage", "razor",
        "umbrella", "bucket", "raki"); // um - starting with "ra" - are suspect

    Iterator<String> it = beachToys.iterator();
    while(it.hasNext()) {
      String toy = it.next();
      if (toy.startsWith("ra")) beachToys.remove(toy);
    }
    System.out.println(beachToys);
  }
}
