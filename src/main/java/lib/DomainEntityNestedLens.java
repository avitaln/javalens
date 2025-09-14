package lib;

import model.DomainEntity;
import model.Nested;
import model.MoreNested;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A wrapper around a Lens<DomainEntity, Nested> that provides direct access to nested properties.
 * This allows for syntax like: DomainEntityLens.nested.nestedValue instead of 
 * DomainEntityLens.nested.andThen(NestedLens.nestedValue)
 */
public class DomainEntityNestedLens implements Mutations.LensProvider<DomainEntity, Nested> {

    private final Lens<DomainEntity, Nested> lens;
    
    // Direct access to nestedValue property
    public final Lens<DomainEntity, String> nestedValue;
    
    // Direct access to moreNested property  
    public final DomainEntity$MoreNestedLens moreNested;

    public DomainEntityNestedLens(Function<DomainEntity, Nested> getter, BiFunction<DomainEntity, Nested, DomainEntity> setter) {
        this.lens = Lens.of(getter, setter);
        
        // Create direct property access lenses by composing with this lens
        this.nestedValue = this.lens.andThen(Lens.of(
            Nested::nestedValue,
            (nested, newValue) -> new Nested(newValue, nested.moreNested())
        ));
        
        this.moreNested = new DomainEntity$MoreNestedLens(
            entity -> lens.get(entity).moreNested(),
            (entity, newMoreNested) -> lens.set(entity, new Nested(lens.get(entity).nestedValue(), newMoreNested))
        );
    }
    
    // Delegate lens methods to the wrapped lens
    public Nested get(DomainEntity entity) {
        return lens.get(entity);
    }
    
    public DomainEntity set(DomainEntity entity, Nested newValue) {
        return lens.set(entity, newValue);
    }
    
    public <C> Lens<DomainEntity, C> andThen(Lens<Nested, C> that) {
        return lens.andThen(that);
    }
    
    // Implement LensProvider interface
    @Override
    public Lens<DomainEntity, Nested> lens() {
        return lens;
    }
}
