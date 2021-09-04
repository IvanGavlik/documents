package students;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SuperIterable<E> implements Iterable<E> {
    private Iterable<E> self;

    public SuperIterable(Iterable<E> self) {
        this.self = self;
    }

    public SuperIterable<E> filter(Predicate<E> test) {
        List<E> rv = new ArrayList<>();
        for (E s : self) {
            if (test.test(s)) {
                rv.add(s);
            }
        }
        return new SuperIterable<>(rv);
    }

    // "Bucket o'data" that performs a map, resulting in 1:1 output is called "Functor"
    public <F> SuperIterable<F> map(Function<E, F> op) {
        List<F> rv = new ArrayList<>();
        for (E e : self) {
            F f = op.apply(e);
            rv.add(f);
        }
        return new SuperIterable<>(rv);
    }

    // "Bucket o'data" that performs an operation that results in a new "Bucket o'data" and
    // unwraps the results into our final "Bucket o'data" is called "Monad"
    public <F> SuperIterable<F> flatMap(Function<E, SuperIterable<F>> op) {
        List<F> rv = new ArrayList<>();
        for (E e : self) {
            SuperIterable<F> fs = op.apply(e);
            for (F f : fs) {
                rv.add(f);
            }
        }
        return new SuperIterable<>(rv);
    }

    public void forEvery(Consumer<E> op) {
        for (E e : self) {
            op.accept(e);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return self.iterator();
    }
}
