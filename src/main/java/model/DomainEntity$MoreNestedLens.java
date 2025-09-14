package model;

import lib.AbstractDomainLens;
import lib.Lens;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DomainEntity$MoreNestedLens extends AbstractDomainLens<DomainEntity, MoreNested> {
    
    public DomainEntity$MoreNestedLens(Function<DomainEntity, MoreNested> getter, BiFunction<DomainEntity, MoreNested, DomainEntity> setter) {
        super(getter, setter);
    }
    
    public Lens<DomainEntity, String> moreNestedValue() {
        return this.lens.andThen(Lens.of(
            MoreNested::moreNestedValue,
            (moreNested, newValue) -> new MoreNested(newValue)
        ));
    }
}
