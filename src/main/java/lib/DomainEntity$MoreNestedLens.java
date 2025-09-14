package lib;

import model.DomainEntity;
import model.MoreNested;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A wrapper around a Lens<DomainEntity, MoreNested> that provides direct access to nested properties.
 * This allows for syntax like: DomainEntityLens.nested.moreNested.moreNestedValue
 */
public class DomainEntity$MoreNestedLens implements Mutations.LensProvider<DomainEntity, MoreNested> {

    private final Lens<DomainEntity, MoreNested> lens;
    
    // Direct access to moreNestedValue property
    public final Lens<DomainEntity, String> moreNestedValue;

    public DomainEntity$MoreNestedLens(Function<DomainEntity, MoreNested> getter, BiFunction<DomainEntity, MoreNested, DomainEntity> setter) {
        this.lens = Lens.of(getter, setter);
        
        // Create direct property access lens by composing with this lens
        this.moreNestedValue = this.lens.andThen(Lens.of(
            MoreNested::moreNestedValue,
            (moreNested, newValue) -> new MoreNested(newValue)
        ));
    }
    
    // Delegate lens methods to the wrapped lens
    public MoreNested get(DomainEntity entity) {
        return lens.get(entity);
    }
    
    public DomainEntity set(DomainEntity entity, MoreNested newValue) {
        return lens.set(entity, newValue);
    }
    
    public <C> Lens<DomainEntity, C> andThen(Lens<MoreNested, C> that) {
        return lens.andThen(that);
    }
    
    // Implement LensProvider interface
    @Override
    public Lens<DomainEntity, MoreNested> lens() {
        return lens;
    }
}
