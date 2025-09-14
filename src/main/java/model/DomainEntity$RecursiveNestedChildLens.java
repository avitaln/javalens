package model;

import lib.Lens;
import lib.Mutations;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A lens for accessing child properties of RecursiveNested within DomainEntity.
 * This enables chaining like: recursiveNested.child().child().value()
 */
public class DomainEntity$RecursiveNestedChildLens implements Mutations.LensProvider<DomainEntity, Optional<RecursiveNested>> {

    private final Lens<DomainEntity, Optional<RecursiveNested>> lens;

    public DomainEntity$RecursiveNestedChildLens(Function<DomainEntity, Optional<RecursiveNested>> getter, BiFunction<DomainEntity, Optional<RecursiveNested>, DomainEntity> setter) {
        this.lens = Lens.of(getter, setter);
    }
    
    // Direct access to value property of the child
    public Lens<DomainEntity, String> value() {
        return this.lens.andThen(Lens.of(
            opt -> opt.map(RecursiveNested::value).orElse(""),
            (opt, newValue) -> opt.map(nested -> new RecursiveNested(newValue, nested.child()))
        ));
    }
    
    // Direct access to child property for further chaining
    public DomainEntity$RecursiveNestedChildLens child() {
        return new DomainEntity$RecursiveNestedChildLens(
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
