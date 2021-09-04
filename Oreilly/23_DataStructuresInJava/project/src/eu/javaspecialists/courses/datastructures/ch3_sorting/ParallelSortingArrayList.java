package eu.javaspecialists.courses.datastructures.ch3_sorting;

import java.util.*;

public class ParallelSortingArrayList<E> extends ArrayList<E> {
  public ParallelSortingArrayList(int initialCapacity) {
    super(initialCapacity);
  }

  public ParallelSortingArrayList() {
  }

  public ParallelSortingArrayList(Collection<? extends E> c) {
    super(c);
  }

  public void parallelSort(Comparator<? super E> c) {
    Object[] a = this.toArray();
    Arrays.parallelSort(a, (Comparator) c);
    ListIterator<E> i = this.listIterator();
    for (Object e : a) {
      i.next();
      i.set((E) e);
    }
  }
}
