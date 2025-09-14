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
                newValue, entity.optionalString(), entity.stringList(), entity.stringMap()
            )
    );

    // Optional String lens
    public static final Lens<DomainEntity, Optional<String>> optionalString = Lens.of(
            DomainEntity::optionalString,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), newValue, entity.stringList(), entity.stringMap()
            )
    );

    // String List lens
    public static final class StringListLens {
        public final Lens<DomainEntity, List<String>> lens = Lens.of(
            DomainEntity::stringList,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), newValue, entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, String> get(int index) {
            return lens.andThen(ListLens.index(index));
        }
    }
    
    public static final StringListLens stringList = new StringListLens();

    // String Map lens
    public static final class StringMapLens {
        public final Lens<DomainEntity, Map<String, String>> lens = Lens.of(
            DomainEntity::stringMap,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), newValue
            )
        );
        
        public Lens<DomainEntity, String> key(String key) {
            return lens.andThen(MapLens.key(key));
        }
    }
    
    public static final StringMapLens stringMap = new StringMapLens();
}