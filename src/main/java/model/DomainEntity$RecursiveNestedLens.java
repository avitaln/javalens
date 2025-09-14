package model;

import lib.AbstractDomainLens;
import lib.Lens;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DomainEntity$RecursiveNestedLens extends AbstractDomainLens<DomainEntity, Optional<RecursiveNested>> {

    private DomainEntity$RecursiveNestedLens(Lens<DomainEntity, Optional<RecursiveNested>> lens) {
        super(lens);
    }
    
    public static DomainEntity$RecursiveNestedLens fromRequired(Function<DomainEntity, RecursiveNested> getter, BiFunction<DomainEntity, RecursiveNested, DomainEntity> setter) {
        return new DomainEntity$RecursiveNestedLens(Lens.of(
            entity -> Optional.of(getter.apply(entity)),
            (entity, optValue) -> optValue.map(value -> setter.apply(entity, value)).orElse(entity)
        ));
    }
    
    public static DomainEntity$RecursiveNestedLens fromOptional(Function<DomainEntity, Optional<RecursiveNested>> getter, BiFunction<DomainEntity, Optional<RecursiveNested>, DomainEntity> setter) {
        return new DomainEntity$RecursiveNestedLens(Lens.of(getter, setter));
    }
    
    public Lens<DomainEntity, String> value() {
        return this.lens.andThen(Lens.of(
            opt -> opt.map(RecursiveNested::value).orElse(""),
            (opt, newValue) -> opt.map(nested -> new RecursiveNested(newValue, nested.child()))
        ));
    }
    
    public DomainEntity$RecursiveNestedLens child() {
        return DomainEntity$RecursiveNestedLens.fromOptional(
            entity -> lens.get(entity).flatMap(RecursiveNested::child),
            (entity, newChild) -> lens.set(entity, 
                lens.get(entity).map(nested -> new RecursiveNested(nested.value(), newChild)))
        );
    }
}
