package eu.javaspecialists.courses.datastructures.ch3_sorting;

import java.util.Objects;
import java.util.concurrent.CompletionService;

public record Student(String name, int iq) implements Comparable<Student> {
  public Student {
    Objects.requireNonNull(name);
  }

  @Override
  public int compareTo(Student that) {
    return this.name.compareTo(that.name);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Student student)) return false;
    return this.name.equals(student.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
