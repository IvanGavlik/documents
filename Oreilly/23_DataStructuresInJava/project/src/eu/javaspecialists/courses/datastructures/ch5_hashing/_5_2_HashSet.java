package eu.javaspecialists.courses.datastructures.ch5_hashing;

import java.util.HashSet;
import java.util.Set;

/**
 * hashCode() vs identityHashCode()
 * Pixel and good hash code
 * Bucket collisions
 * Making keys implement Comparable
 */
public class _5_2_HashSet {
  private static class Pixel implements Comparable<Pixel> {
    private final int x, y;

    public Pixel(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Pixel pixel = (Pixel) o;

      if (x != pixel.x) return false;
      if (y != pixel.y) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return 4000 * x + y;
    }

    @Override
    public int compareTo(Pixel that) {
      int result = Integer.compare(this.x, that.x);
      if (result != 0) return result;
      return Integer.compare(this.y, that.y);
    }

  }

//  private record Pixel(int x, int y) implements Comparable<Pixel> {
//    @Override
//    public int hashCode() {
//      return 4000 * x + y;
//    }
//
//    @Override
//    public int compareTo(Pixel that) {
//      int result = Integer.compare(this.x, that.x);
//      if (result != 0) return result;
//      return Integer.compare(this.y, that.y);
//    }
//  }

  public static void main(String... args) {
    long time = System.nanoTime();
    try {
      Set<Pixel> pixels = new HashSet<>();
      for (int x = 0; x < 1024; x++) {
        for (int y = 0; y < 768; y++) {
          pixels.add(new Pixel(x, y));
        }
      }
    } finally {
      time = System.nanoTime() - time;
      System.out.printf("time = %dms%n", (time / 1_000_000));
    }
  }
}
