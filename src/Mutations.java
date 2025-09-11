import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public final class Mutations<A> {

    private final List<UnaryOperator<A>> operations = new ArrayList<>();

    private Mutations() {}

    public static <A> Mutations<A> forType() { return new Mutations<>(); }

    public <B> Mutations<A> set(Lens<A, B> lens, B newValue) {
        operations.add(a -> lens.set(a, newValue));
        return this;
    }

    public <B> Mutations<A> mod(Lens<A, B> lens, UnaryOperator<B> f) {
        operations.add(a -> lens.mod(a, f));
        return this;
    }

    public A apply(A a) {
        for (UnaryOperator<A> op : operations) a = op.apply(a);
        return a;
    }
}


