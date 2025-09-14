package lib;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public final class Mutations<A> {

    private final List<UnaryOperator<A>> operations = new ArrayList<>();

    private Mutations() {}

    public static <A> Mutations<A> forType() { return new Mutations<>(); }

    public static <A> BoundMutations<A> forValue(A value) { return new BoundMutations<>(value); }

    public <B> Mutations<A> set(Lens<A, B> lens, B newValue) {
        operations.add(a -> lens.set(a, newValue));
        return this;
    }

    public <B> Mutations<A> set(LensProvider<A, B> provider, B newValue) {
        return set(provider.lens(), newValue);
    }

    public <B> Mutations<A> mod(Lens<A, B> lens, UnaryOperator<B> f) {
        operations.add(a -> lens.mod(a, f));
        return this;
    }

    public <B> Mutations<A> mod(LensProvider<A, B> provider, UnaryOperator<B> f) {
        return mod(provider.lens(), f);
    }

    public interface LensProvider<A, B> {
        Lens<A, B> lens();
    }

    public static final class BoundMutations<A> {
        private A current;
        private final List<UnaryOperator<A>> operations = new ArrayList<>();

        private BoundMutations(A start) { this.current = start; }

        public <B> BoundMutations<A> set(Lens<A, B> lens, B newValue) {
            operations.add(a -> lens.set(a, newValue));
            return this;
        }

        public <B> BoundMutations<A> set(LensProvider<A, B> provider, B newValue) {
            return set(provider.lens(), newValue);
        }

        public <B> BoundMutations<A> mod(Lens<A, B> lens, UnaryOperator<B> f) {
            operations.add(a -> lens.mod(a, f));
            return this;
        }

        public <B> BoundMutations<A> mod(LensProvider<A, B> provider, UnaryOperator<B> f) {
            return mod(provider.lens(), f);
        }

        public A apply() {
            for (UnaryOperator<A> op : operations) current = op.apply(current);
            return current;
        }
    }
}


