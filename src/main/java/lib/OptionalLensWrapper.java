package lib;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Generic wrapper for Optional domain objects that provides the same API as the wrapped domain lens.
 * This allows reusing existing domain lens classes for their Optional counterparts while maintaining
 * the same method names and behavior.
 * 
 * @param <A> the root entity type
 * @param <T> the wrapped domain object type (e.g., Nested)
 */
public class OptionalLensWrapper<A, T> extends AbstractDomainLens<A, Optional<T>> {

    public OptionalLensWrapper(Function<A, Optional<T>> getter, BiFunction<A, Optional<T>, A> setter) {
        super(getter, setter);
    }

    /**
     * Helper method to create lenses that operate on nested properties of the Optional value.
     * This handles the Optional logic (mapping over present values, providing defaults for empty).
     * 
     * @param propertyGetter function to extract a property from the wrapped type T
     * @param propertyUpdater function to update the wrapped type T with a new property value
     * @param defaultValue default value to return when Optional is empty
     * @return a lens that operates on the nested property
     */
    protected <P> Lens<A, P> createPropertyLens(Function<T, P> propertyGetter, 
                                               BiFunction<T, P, T> propertyUpdater,
                                               P defaultValue) {
        return lens.andThen(Lens.of(
            opt -> opt.map(propertyGetter).orElse(defaultValue),
            (opt, newValue) -> {
                if (opt.isEmpty()) {
                    throw new IllegalStateException("Cannot update property on empty Optional");
                }
                return opt.map(value -> propertyUpdater.apply(value, newValue));
            }
        ));
    }

    /**
     * Helper method to create nested lenses with fail-fast behavior for empty optionals.
     * This can create any type of lens (regular or optional) for nested properties.
     * 
     * @param nestedGetter function to extract a nested property from the wrapped type T
     * @param nestedUpdater function to update the wrapped type T with a new nested property value
     * @param lensFactory function to create the appropriate lens type from getter and setter functions
     * @return a lens for the nested property
     */
    protected <N, L> L createNestedLens(Function<T, N> nestedGetter,
                                       BiFunction<T, N, T> nestedUpdater,
                                       BiFunction<Function<A, N>, BiFunction<A, N, A>, L> lensFactory) {
        return lensFactory.apply(
            // Getter: extract nested property, fail if Optional is empty
            entity -> lens.get(entity)
                .map(nestedGetter)
                .orElseThrow(() -> new IllegalStateException("Cannot access nested property on empty Optional")),
            // Setter: update nested property, fail if Optional is empty
            (entity, newNestedValue) -> {
                Optional<T> current = lens.get(entity);
                if (current.isEmpty()) {
                    throw new IllegalStateException("Cannot update nested property on empty Optional");
                }
                return lens.set(entity, current.map(value -> nestedUpdater.apply(value, newNestedValue)));
            }
        );
    }

}
