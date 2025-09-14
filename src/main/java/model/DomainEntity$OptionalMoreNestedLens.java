package model;

import lib.AbstractDomainLens;
import lib.Lens;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DomainEntity$OptionalMoreNestedLens extends AbstractDomainLens<DomainEntity, Optional<MoreNested>> {
    
    public DomainEntity$OptionalMoreNestedLens(Function<DomainEntity, Optional<MoreNested>> getter, BiFunction<DomainEntity, Optional<MoreNested>, DomainEntity> setter) {
        super(getter, setter);
    }
    
    public Lens<DomainEntity, String> moreNestedValue() {
        return this.lens.andThen(Lens.of(
            opt -> opt.map(MoreNested::moreNestedValue).orElse(""),
            (opt, newValue) -> opt.map(moreNested -> new MoreNested(newValue))
        ));
    }
}
