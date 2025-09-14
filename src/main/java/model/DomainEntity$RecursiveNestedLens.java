package model;

import lib.Lens;
import lib.Mutations;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A unified wrapper for recursive nested structures that handles both RecursiveNested 
 * and Optional<RecursiveNested> cases. This prevents infinite loops during initialization 
 * while maintaining friendly syntax: entity.recursiveNested().value() and 
 * entity.recursiveNested().child().child().value()
 */
public class DomainEntity$RecursiveNestedLens implements Mutations.LensProvider<DomainEntity, Optional<RecursiveNested>> {

    private final Lens<DomainEntity, Optional<RecursiveNested>> lens;

    // Private constructor - use factory methods instead
    private DomainEntity$RecursiveNestedLens(Lens<DomainEntity, Optional<RecursiveNested>> lens) {
        this.lens = lens;
    }
    
    // Factory method for non-optional RecursiveNested (root level)
    public static DomainEntity$RecursiveNestedLens fromRequired(Function<DomainEntity, RecursiveNested> getter, BiFunction<DomainEntity, RecursiveNested, DomainEntity> setter) {
        return new DomainEntity$RecursiveNestedLens(Lens.of(
            entity -> Optional.of(getter.apply(entity)),
            (entity, optValue) -> optValue.map(value -> setter.apply(entity, value)).orElse(entity)
        ));
    }
    
    // Factory method for Optional<RecursiveNested> (child levels)
    public static DomainEntity$RecursiveNestedLens fromOptional(Function<DomainEntity, Optional<RecursiveNested>> getter, BiFunction<DomainEntity, Optional<RecursiveNested>, DomainEntity> setter) {
        return new DomainEntity$RecursiveNestedLens(Lens.of(getter, setter));
    }
    
    // Direct access to value property - handles Optional internally
    public Lens<DomainEntity, String> value() {
        return this.lens.andThen(Lens.of(
            opt -> opt.map(RecursiveNested::value).orElse(""),
            (opt, newValue) -> opt.map(nested -> new RecursiveNested(newValue, nested.child()))
        ));
    }
    
    // Recursive access to child property - returns another RecursiveNestedLens for chaining
    public DomainEntity$RecursiveNestedLens child() {
        return DomainEntity$RecursiveNestedLens.fromOptional(
            entity -> lens.get(entity).flatMap(RecursiveNested::child),
            (entity, newChild) -> lens.set(entity, 
                lens.get(entity).map(nested -> new RecursiveNested(nested.value(), newChild)))
        );
    }
    
    // Delegate lens methods to the wrapped lens
    public Optional<RecursiveNested> get(DomainEntity entity) {
        return lens.get(entity);
    }
    
    public DomainEntity set(DomainEntity entity, Optional<RecursiveNested> newValue) {
        return lens.set(entity, newValue);
    }
    
    public <C> Lens<DomainEntity, C> andThen(Lens<Optional<RecursiveNested>, C> that) {
        return lens.andThen(that);
    }
    
    // Implement LensProvider interface
    @Override
    public Lens<DomainEntity, Optional<RecursiveNested>> lens() {
        return lens;
    }
}
