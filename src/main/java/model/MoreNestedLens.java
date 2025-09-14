package model;

import lib.Lens;
import lib.Mutations;

public final class MoreNestedLens {

    public static Mutations.BoundMutations<MoreNested> on(MoreNested entity) { 
        return Mutations.forValue(entity); 
    }

    // String primitive lens
    public static final Lens<MoreNested, String> moreNestedValue = Lens.of(
            MoreNested::moreNestedValue,
            (entity, newValue) -> new MoreNested(newValue)
    );
}
