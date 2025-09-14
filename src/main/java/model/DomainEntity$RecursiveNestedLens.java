package model;

import lib.Lens;
import lib.Mutations;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A wrapper for recursive nested structures using functions instead of properties.
 * This prevents infinite loops during initialization while maintaining friendly syntax:
 * entity.recursiveNested.value() instead of entity.recursiveNested.value
 * entity.recursiveNested.child().value() instead of entity.recursiveNested.child.getValue()
 */
public class DomainEntity$RecursiveNestedLens implements Mutations.LensProvider<DomainEntity, RecursiveNested> {

    private final Lens<DomainEntity, RecursiveNested> lens;

    public DomainEntity$RecursiveNestedLens(Function<DomainEntity, RecursiveNested> getter, BiFunction<DomainEntity, RecursiveNested, DomainEntity> setter) {
        this.lens = Lens.of(getter, setter);
    }
    
    // Direct access to value property as function
    public Lens<DomainEntity, String> value() {
        return this.lens.andThen(Lens.of(
            RecursiveNested::value,
            (nested, newValue) -> new RecursiveNested(newValue, nested.child())
        ));
    }
    
    // Recursive access to child property as function - supports chaining!
    public DomainEntity$RecursiveNestedChildLens child() {
        return new DomainEntity$RecursiveNestedChildLens(
            entity -> lens.get(entity).child(),
            (entity, newChild) -> lens.set(entity, new RecursiveNested(lens.get(entity).value(), newChild))
        );
    }
    
    // Delegate lens methods to the wrapped lens
    public RecursiveNested get(DomainEntity entity) {
        return lens.get(entity);
    }
    
    public DomainEntity set(DomainEntity entity, RecursiveNested newValue) {
        return lens.set(entity, newValue);
    }
    
    public <C> Lens<DomainEntity, C> andThen(Lens<RecursiveNested, C> that) {
        return lens.andThen(that);
    }
    
    // Implement LensProvider interface
    @Override
    public Lens<DomainEntity, RecursiveNested> lens() {
        return lens;
    }
}
