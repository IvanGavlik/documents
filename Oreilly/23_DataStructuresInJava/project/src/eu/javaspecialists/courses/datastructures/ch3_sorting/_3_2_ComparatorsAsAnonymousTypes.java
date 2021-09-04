package eu.javaspecialists.courses.datastructures.ch3_sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Writing Comparators as anonymous classes
 */
public class _3_2_ComparatorsAsAnonymousTypes {
  public static void main(String... args) {
    List<Student> students = new ArrayList<>();
    Collections.addAll(students,
        new Student("John", 180),
        new Student("Peter", 130),
        new Student("Manfred", 195),
        new Student("Maxi", 120),
        new Student("Heinz", 120),
        new Student("Anton", 150));
    students.sort(null); // natural order
    students.forEach(System.out::println);

    System.out.println("Sorted IQ, name");
    students.sort(new Comparator<Student>() {
      @Override
      public int compare(Student s1, Student s2) {
        int result = -Integer.compare(s1.iq(), s2.iq());
        if (result != 0) return result;
        return s1.name().compareTo(s2.name());
      }
    });
    students.forEach(System.out::println);
  }
}
