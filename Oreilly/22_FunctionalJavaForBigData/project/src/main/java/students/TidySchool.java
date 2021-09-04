package students;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TidySchool {
    public static <E> Predicate<E> and(Predicate<E> first, Predicate<E> second) {
        return e -> first.test(e) && second.test(e);
    }

    public static <E> Predicate<E> negate(Predicate<E> test) {
        return e -> !test.test(e);
    }

    public static <E> void show(List<E> ls) {
        for (E s : ls) {
            System.out.println(s);
        }
        System.out.println("-----------------");
    }

    public static <E> List<E> filter(Iterable<E> ls, Predicate<E> sst) {
        List<E> rv = new ArrayList<>();
        for (E s : ls) {
            if (sst.test(s)) {
                rv.add(s);
            }
        }
        return rv;
    }

    public static void main(String[] args) {
        List<Student> roster = List.of(
                Student.of("Fred", 75, "Math", "Physics"),
                Student.of("Jim", 58, "Art"),
                Student.of("Sheila", 92, "Math", "Physics", "Astrophysics", "Quantum Mechanics")
        );

//        show(filter(roster, s -> s.getGrade() > 70));
        Predicate<Student> smartPredicate = Student.getSmartnessPredicate(70);
        Predicate<Student> enthusiasticPredicate = Student.getEnthusiasmPredicate(3);

        show(filter(roster, smartPredicate));
        show(filter(roster, Student.getSmartnessPredicate(50)));
        show(filter(roster, enthusiasticPredicate));

        List<String> names = List.of("Fred", "Jim", "Sheila");
        show(filter(names, s -> s.length() < 5 ));

        show(filter(roster, and(smartPredicate, enthusiasticPredicate)));
        show(filter(roster, and(smartPredicate, negate(enthusiasticPredicate))));
        show(filter(roster, smartPredicate.and(enthusiasticPredicate.negate())));
    }
}
