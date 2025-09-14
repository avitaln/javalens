package model;

import lib.Lens;
import lib.ListLens;
import lib.MapLens;
import lib.Mutations;

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
                newValue, entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested()
            )
    );

    // Optional String lens
    public static final Lens<DomainEntity, Optional<String>> optionalString = Lens.of(
            DomainEntity::optionalString,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), newValue, entity.stringList(), entity.stringMap(), entity.nested()
            )
    );

    // String List lens
    public static final Lens<DomainEntity, List<String>> stringList = Lens.of(
        DomainEntity::stringList,
        (entity, newValue) -> new DomainEntity(
            entity.stringValue(), entity.optionalString(), newValue, entity.stringMap(), entity.nested()
        )
    );
    
    // String List element access
    public static Lens<DomainEntity, String> stringListGet(int index) {
        return stringList.andThen(ListLens.index(index));
    }

    // String Map lens
    public static final Lens<DomainEntity, Map<String, String>> stringMap = Lens.of(
        DomainEntity::stringMap,
        (entity, newValue) -> new DomainEntity(
            entity.stringValue(), entity.optionalString(), entity.stringList(), newValue, entity.nested()
        )
    );
    
    // String Map key access
    public static Lens<DomainEntity, String> stringMapKey(String key) {
        return stringMap.andThen(MapLens.key(key));
    }

    // Nested lens
    public static final Lens<DomainEntity, Nested> nested = Lens.of(
        DomainEntity::nested,
        (entity, newValue) -> new DomainEntity(
            entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), newValue
        )
    );
}