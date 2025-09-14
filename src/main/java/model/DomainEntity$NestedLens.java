package model;

import lib.AbstractDomainLens;
import lib.Lens;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DomainEntity$NestedLens extends AbstractDomainLens<DomainEntity, Nested> {
    
    public DomainEntity$NestedLens(Function<DomainEntity, Nested> getter, BiFunction<DomainEntity, Nested, DomainEntity> setter) {
        super(getter, setter);
    }
    
    public Lens<DomainEntity, String> nestedValue() {
        return this.lens.andThen(Lens.of(
            Nested::nestedValue,
            (nested, newValue) -> new Nested(newValue, nested.moreNested())
        ));
    }
    
    public DomainEntity$MoreNestedLens moreNested() {
        return new DomainEntity$MoreNestedLens(
            entity -> lens.get(entity).moreNested(),
            (entity, newMoreNested) -> lens.set(entity, new Nested(lens.get(entity).nestedValue(), newMoreNested))
        );
    }
}
