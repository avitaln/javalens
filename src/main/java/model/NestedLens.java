package model;

import lib.Lens;
import lib.Mutations;

public final class NestedLens {

    public static Mutations.BoundMutations<Nested> on(Nested entity) { 
        return Mutations.forValue(entity); 
    }

    // String primitive lens
    public static final Lens<Nested, String> nestedValue = Lens.of(
            Nested::nestedValue,
            (entity, newValue) -> new Nested(newValue, entity.moreNested())
    );

    // MoreNested lens
    public static final Lens<Nested, MoreNested> moreNested = Lens.of(
            Nested::moreNested,
            (entity, newValue) -> new Nested(entity.nestedValue(), newValue)
    );
}
