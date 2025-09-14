package model;

import lib.Lens;
import lib.Mutations;

public final class MoreNestedLens {

    public static Mutations.BoundMutations<MoreNested> on(MoreNested entity) { 
        return Mutations.forValue(entity); 
    }

    // String primitive lens as function
    public static Lens<MoreNested, String> moreNestedValue() {
        return Lens.of(
            MoreNested::moreNestedValue,
            (entity, newValue) -> new MoreNested(newValue)
        );
    }
}
