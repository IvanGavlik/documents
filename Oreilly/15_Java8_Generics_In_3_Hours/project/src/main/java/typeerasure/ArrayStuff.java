package typeerasure;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class ArrayStuff {
  // This fails because there is no type E at runtime,
  // and arrays need to know their base type
//  public static <E> E[] getAsArray(List<E> le) {
//    E[] result = new E[le.size()];
//  }

//  public static <E> E[] getAsArray(List<E> le) {
    // This fails because array of object is NOT array of E
//    E[] result = new Object[le.size()];
    // The cast "works" but typically crashes at runtime when you try to use
    // the array, since it's not actually what the compiler thinks
//    E[] result = (E[])new Object[le.size()];

  public static <E> E[] simpleGetAsArray(List<E> le, Class<E> cl) {
    // This requires the cast for backward compatibiliity
    // Array.newInstance existed before generics so returns Object :(
    E[] result = (E[])Array.newInstance(cl, le.size());
    for (int idx = 0; idx < result.length; idx++) {
      result[idx] = le.get(idx);
    }
    return result;
  }

  // BUT, what if I want to add strings to an array of CharSequence??
  // What matters is that the type I request for the array *accepts*
  // assignment from the type of the list element (sounds like contravariance, yes?):
  // Sadly, this is not allowed in Java
//  public static <E, F super e> F[] getAsArray(List<E> le, Class<F> cl) {
  // But this IS :)
  public static <F, E extends F> F[] getAsArray(List<E> le, Class<F> cl) {
    F[] result = (F[])Array.newInstance(cl, le.size());
    for (int i = 0; i < result.length; i++) {
      result[i] = le.get(i);
    }
    return result;
  }

  public static void main(String[] args) {
    List<String> ls = Arrays.asList("Fred", "Jim", "Sheila");
    String[] sa = simpleGetAsArray(ls, String.class);
    // Note the array *knows* that it's an array **OF STRING**
    // that's the [Ljava.lang.String part of the typename
    System.out.println("Type of array is: " + sa.getClass().getName());

    CharSequence[] cs = getAsArray(ls, CharSequence.class);
    System.out.println("Type of array is: " + cs.getClass().getName());
  }
}
