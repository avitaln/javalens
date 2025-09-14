package model;

import lib.AbstractDomainLens;
import lib.Lens;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DomainEntity$OptionalNestedLens extends AbstractDomainLens<DomainEntity, Optional<Nested>> {
    
    public DomainEntity$OptionalNestedLens(Function<DomainEntity, Optional<Nested>> getter, BiFunction<DomainEntity, Optional<Nested>, DomainEntity> setter) {
        super(getter, setter);
    }
    
    public Lens<DomainEntity, String> nestedValue() {
        return this.lens.andThen(Lens.of(
            opt -> opt.map(Nested::nestedValue).orElse(""),
            (opt, newValue) -> opt.map(nested -> new Nested(newValue, nested.moreNested()))
        ));
    }
    
    public DomainEntity$OptionalMoreNestedLens moreNested() {
        return new DomainEntity$OptionalMoreNestedLens(
            entity -> lens.get(entity).map(Nested::moreNested),
            (entity, newMoreNested) -> lens.set(entity, 
                lens.get(entity).map(nested -> new Nested(nested.nestedValue(), newMoreNested.orElse(nested.moreNested()))))
        );
    }
}
