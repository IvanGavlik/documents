package hello;

import java.util.function.Consumer;

public class Hello {
  public static void main(String[] args) {
    ((Consumer<String>)(@Deprecated var s) -> System.out.println(s)).accept("Hello World");
  }
}
