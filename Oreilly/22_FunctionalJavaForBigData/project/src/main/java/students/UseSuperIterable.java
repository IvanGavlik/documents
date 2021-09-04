package students;

import java.util.List;

public class UseSuperIterable {
    public static void main(String[] args) {
        SuperIterable<Student> roster = new SuperIterable<>(List.of(
                Student.of("Fred", 75, "Math", "Physics"),
                Student.of("Jim", 58, "Art"),
                Student.of("Sheila", 92, "Math", "Physics", "Astrophysics", "Quantum Mechanics")
        ));

        /*var smart = */roster
                .filter(s -> s.getGrade() > 70) // "Intermediate" or "non-terminal" operations
                .map(s -> "Student called " + s.getName() + " is smart")
                .forEach(s -> System.out.println(s)); // any operation that does not return [SuperIterable] is "terminal"
//                .forEvery(s -> System.out.println(s));


//                .map(s -> {System.out.println(s); return null;});
//        for (String s : smart) {
//            System.out.println("> " + s);
//        }

        System.out.println("---------------------");
        roster
//                .filter(__ -> __.getGrade() > 70)
                .filter(s -> s.getGrade() > 70)
                .flatMap(s -> new SuperIterable<>(s.getCourses()))
                .forEach(s -> System.out.println(s));
    }
}
