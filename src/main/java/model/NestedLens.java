package model;

import lib.Lens;
import lib.Mutations;

public final class NestedLens {

    public static Mutations.BoundMutations<Nested> on(Nested entity) { 
        return Mutations.forValue(entity); 
    }

    // String primitive lens as function
    public static Lens<Nested, String> nestedValue() {
        return Lens.of(
            Nested::nestedValue,
            (entity, newValue) -> new Nested(newValue, entity.moreNested())
        );
    }

    // MoreNested lens as function
    public static Lens<Nested, MoreNested> moreNested() {
        return Lens.of(
            Nested::moreNested,
            (entity, newValue) -> new Nested(entity.nestedValue(), newValue)
        );
    }
}
