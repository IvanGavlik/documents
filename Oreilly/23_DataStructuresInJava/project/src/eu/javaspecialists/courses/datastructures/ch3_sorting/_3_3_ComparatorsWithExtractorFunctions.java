package eu.javaspecialists.courses.datastructures.ch3_sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Comparators with extractor functions
 * Type witnesses
 * Declared lambda parameters
 * Method references
 */
public class _3_3_ComparatorsWithExtractorFunctions {
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
    students.sort(
        Comparator.comparingInt(Student::iq)
            .reversed()
            .thenComparing(Student::name));
    students.forEach(System.out::println);

  }
}
