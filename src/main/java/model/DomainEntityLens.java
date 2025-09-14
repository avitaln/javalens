package model;

import lib.Lens;
import lib.ListLens;
import lib.MapLens;
import lib.Mutations;
import lib.MapLensWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DomainEntityLens {

    public static Mutations.BoundMutations<DomainEntity> on(DomainEntity entity) { 
        return Mutations.forValue(entity); 
    }

    // String primitive lens
    public static final Lens<DomainEntity, String> stringValue = Lens.of(
            DomainEntity::stringValue,
            (entity, newValue) -> new DomainEntity(
                newValue, entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested()
            )
    );

    // Optional String lens
    public static final Lens<DomainEntity, Optional<String>> optionalString = Lens.of(
            DomainEntity::optionalString,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), newValue, entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested()
            )
    );

    // String List lens
    public static final Lens<DomainEntity, List<String>> stringList = Lens.of(
        DomainEntity::stringList,
        (entity, newValue) -> new DomainEntity(
            entity.stringValue(), entity.optionalString(), newValue, entity.stringMap(), entity.nested(), entity.optionalNested()
        )
    );
    
    // String List element access
    public static Lens<DomainEntity, String> stringListGet(int index) {
        return stringList.andThen(ListLens.index(index));
    }

    // String Map lens with direct key access
    public static final MapLensWrapper<DomainEntity, String, String> stringMap = new MapLensWrapper<>(
        DomainEntity::stringMap,
        (entity, newValue) -> new DomainEntity(
            entity.stringValue(), entity.optionalString(), entity.stringList(), newValue, entity.nested(), entity.optionalNested()
        )
    );
    

    // Nested lens with direct property access
    public static final DomainEntity$NestedLens nested = new DomainEntity$NestedLens(
        DomainEntity::nested,
        (entity, newValue) -> new DomainEntity(
            entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), newValue, entity.optionalNested()
        )
    );

    // Optional Nested lens
    public static final Lens<DomainEntity, Optional<Nested>> optionalNested = Lens.of(
        DomainEntity::optionalNested,
        (entity, newValue) -> new DomainEntity(
            entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), newValue
        )
    );
}