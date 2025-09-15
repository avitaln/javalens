package lib;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ObjectOptionalLensWrapper<T, U, V extends AbstractDomainLens<T, U>> extends OptionalLensWrapper<T, U> {
    private final Function<Lens<T, U>, V> lensCreator;

    public ObjectOptionalLensWrapper(
            final Function<T, Optional<U>> getter,
            final BiFunction<T, Optional<U>, T> setter,
            final Function<Lens<T, U>, V> lensCreator) {
        super(getter, setter);
        this.lensCreator = lensCreator;
    }

    protected <R> Lens<T, R> createPropertyLens(
            final Function<U, R> getter,
            final BiFunction<U, R, U> setter,
            final R defaultValue) {
        return this.lens.andThen(Lens.of(
                optional -> optional.map(getter).orElseThrow(() -> new IllegalStateException("Cannot access property of empty optional")),
                (optional, newValue) -> optional.map(value -> setter.apply(value, newValue))
                        .or(() -> { throw new IllegalStateException("Cannot set property of empty optional"); })
        ));
    }

    protected <W extends AbstractDomainLens<T, S>, S> W createNestedLens(
            final Function<U, S> getter,
            final BiFunction<U, S, U> setter,
            final Function<Lens<T, S>, W> lensCreator) {
        final Lens<T, S> nestedLens = this.lens.andThen(Lens.of(
                optional -> optional.map(getter).orElseThrow(() -> new IllegalStateException("Cannot access property of empty optional")),
                (optional, newSubValue) -> optional.map(value -> setter.apply(value, newSubValue))
                        .or(() -> { throw new IllegalStateException("Cannot set property of empty optional"); })
        ));
        return lensCreator.apply(nestedLens);
    }
}