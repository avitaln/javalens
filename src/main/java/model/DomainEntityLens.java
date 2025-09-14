package model;

import lib.Lens;
import lib.ListLens;
import lib.ListLensWrapper;
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

    // String primitive lens as function
    public static Lens<DomainEntity, String> stringValue() {
        return Lens.of(
            DomainEntity::stringValue,
            (entity, newValue) -> new DomainEntity(
                newValue, entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested(), entity.recursiveNested()
            )
        );
    }

    // Optional String lens as function
    public static Lens<DomainEntity, Optional<String>> optionalString() {
        return Lens.of(
            DomainEntity::optionalString,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), newValue, entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested(), entity.recursiveNested()
            )
        );
    }

    // String List lens with direct element access as function
    public static ListLensWrapper<DomainEntity, String> stringList() {
        return new ListLensWrapper<>(
            DomainEntity::stringList,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), newValue, entity.stringMap(), entity.nested(), entity.optionalNested(), entity.recursiveNested()
            )
        );
    }

    // String Map lens with direct key access as function
    public static MapLensWrapper<DomainEntity, String, String> stringMap() {
        return new MapLensWrapper<>(
            DomainEntity::stringMap,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), newValue, entity.nested(), entity.optionalNested(), entity.recursiveNested()
            )
        );
    }
    

    // Nested lens with direct property access as function
    public static DomainEntity$NestedLens nested() {
        return new DomainEntity$NestedLens(
            DomainEntity::nested,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), newValue, entity.optionalNested(), entity.recursiveNested()
            )
        );
    }

    // Optional Nested lens as function
    public static Lens<DomainEntity, Optional<Nested>> optionalNested() {
        return Lens.of(
            DomainEntity::optionalNested,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), newValue, entity.recursiveNested()
            )
        );
    }

    // Recursive Nested lens with function-based access as function
    public static DomainEntity$RecursiveNestedLens recursiveNested() {
        return DomainEntity$RecursiveNestedLens.fromRequired(
            DomainEntity::recursiveNested,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested(), newValue
            )
        );
    }
}