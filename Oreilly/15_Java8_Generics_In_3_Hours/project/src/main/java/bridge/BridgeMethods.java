package bridge;

class Base<E> {
  public void doWith(E arg) { // actual arg type is Object, yes?
    System.out.println("running Base.doWith with " + arg);
  }
}

class Sub extends Base<String> {
  @Override
  // this "isn't" really an override, it's an overload;
  // because it has different argument type!
  // but we *want* it to behave like an override...
  public void doWith(String arg) {
    System.out.println("In Sub.doWith with: " + arg);
    super.doWith(arg);
  }
}
public class BridgeMethods {
  public static void main(String[] args) {
    Base<String> b = new Sub();
    b.doWith("Hello"); // see normal overRIDE behavior...

    // Now, compile the class by hand
    // (probably: cd src/main/java ...)
    // javac -d output bridge/BridgeMethods.java
    // and disassemble the subclass...
    // javap -cp output bridge.Sub:
    /*
    er)
$ javap -cp output bridge.Sub
Compiled from "BridgeMethods.java"
class bridge.Sub extends bridge.Base<java.lang.String> {
  bridge.Sub();
  public void doWith(java.lang.String); <== IMPORTANT METHOD
  public void doWith(java.lang.Object); <== ORIGINAL METHOD "bridges" / delegates to above.
}

the bridge is an overload that delegates to the original.
     */
    // if we ask for more detail:
    // javap -cp output -c bridge.Sub
/*
  $javap -c -cp output bridge.Sub
Compiled from "BridgeMethods.java"
class bridge.Sub extends bridge.Base<java.lang.String> {
  bridge.Sub();
    Code: ELIDED
  public void doWith(java.lang.String);
    Code: REAL IMPLEMENTATION ELIDED

  public void doWith(java.lang.Object);
    Code:
       0: aload_0
       1: aload_1
       2: checkcast     #6                  // class java/lang/String
       5: invokevirtual #7                  // Method doWith:(Ljava/lang/String;)V
       8: return
}

NOTICE, line 5 delegates to the "important" version that takes a String :)

*/
  }
}
