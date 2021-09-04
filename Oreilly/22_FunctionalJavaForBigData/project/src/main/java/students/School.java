package students;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
interface Test<E> {
    default void doDefaultStuff(){}
    boolean test(E s);
//    void doStuff();
}

class SmartStudentTest implements Test<Student> {
    public boolean test(Student s) {
        return s.getGrade() > 70;
    }
}

//class ShortStringTest implements Test<String> {
//    @Override
//    public boolean test(String s) {
//        return s.length() < 5;
//    }
//}

class EnthusiasticStudentTest implements Test<Student> {
    @Override
    public boolean test(Student s) {
        return s.getCourses().size() > 3;
    }
}
public class School {
    public static <E> void show(List<E> ls) {
        for (E s : ls) {
            System.out.println(s);
        }
        System.out.println("-----------------");
    }

    // Use "pointer to an object" for the behavior that object contains...
    // e.g. strategy, and this one, which is called "command"
    // In "functional programming" passing a "behavior" (i.e. a function :) is called "higher order function"
    public static <E> List<E> filter(List<E> ls, Test<E> sst) {
        List<E> rv = new ArrayList<>();
        for (E s : ls) {
            if (sst.test(s)) { // NOT sst(s)
                rv.add(s);
            }
        }
        return rv;
    }

//     public static List<Student> getSmartStudents(List<Student> ls, int threshold) {
//        List<Student> rv = new ArrayList<>();
//        for (Student s : ls) {
//            if (s.getGrade() > threshold) {
//                rv.add(s);
//            }
//        }
//        return rv;
//    }
//
//    public static List<Student> getEnthusiastcStudents(List<Student> ls, int threshold) {
//        List<Student> rv = new ArrayList<>();
//        for (Student s : ls) {
//            if (s.getCourses().size() > threshold) {
//                rv.add(s);
//            }
//        }
//        return rv;
//    }

    public static void main(String[] args) {
        List<Student> roster = List.of(
                Student.of("Fred", 75, "Math", "Physics"),
                Student.of("Jim", 58, "Art"),
                Student.of("Sheila", 92, "Math", "Physics", "Astrophysics", "Quantum Mechanics")
        );

        show(filter(roster, new SmartStudentTest()));
//        showStudents(getSmartStudents(roster, 70));
        show(filter(roster, new EnthusiasticStudentTest()));

        List<String> names = List.of("Fred", "Jim", "Sheila");
//        show(filter(names, new ShortStringTest()));

//        show(filter(names, new
//                // "Anonymous inner class"
//        /*class ShortStringTest implements */Test<String>() {
//            @Override
//            public boolean test(String s) {
//                return s.length() < 5;
//            }
//        }
//                ));

        // "Traditional" compiler mode -- tell what you must give, then verify I gave the right thing...
        // "New" mode, "here are parts of what's needed... work out what is fully needed, and make it from these parts..
        // which parts are needed???
        // compiler knows it needs an object--no value in saying "new"
        // knows it must be a Test<String>
        // In THIS situation, the *inteface* defines EXACTLY ONE abstract method, AND we only want to implement
        // that ONE abstract method...
        // don't need "grouping"
        // Compiler knows the ONLY method we're implementing is ... test(String s)
        // needs "permission" to use "new syntax" and behavior ... use an arrow
//        show(filter(names, /*new Test<String>() { */
//           /* @Override
//            public boolean test*/(String s) -> {
//                return s.length() < 5;
//            }
//        /*}*/
//                ));
//        show(filter(names, (String s) -> { return s.length() < 5; }));
        // if argument types are ALL unambiguous, can leave them ALL out
//        show(filter(names, (s) -> { return s.length() < 5; }));
        // java 11 allows var for arg types (ALL or none)
//        show(filter(names, (var s) -> { return s.length() < 5; }));
//        show(filter(names, (@Deprecated var s) -> { return s.length() < 5; }));
        // IFF exactly ONE argument, without type of any kind, can leave off parens
        // zero args MUST have parens e.g. () -> {}
//        show(filter(names, s -> { return s.length() < 5; }));
        // IFF function body is purely a single return statement, replace the body with the expression to be returned
        show(filter(names, s -> s.length() < 5 ));

        Test<String> obj = s -> s.length() < 5;
        System.out.println("Class of the lambda is " + obj.getClass().getName());
        System.out.println("lambda implements Test? " + (obj instanceof Test));
        Method[] methods = obj.getClass().getMethods();
        for (Method m : methods) {
            System.out.println(m);
        }
    }
}
