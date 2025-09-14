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

    // Primitive lenses
    public static final Lens<DomainEntity, Integer> intValue = Lens.of(
            DomainEntity::intValue,
            (entity, newValue) -> new DomainEntity(
                newValue, entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    public static final Lens<DomainEntity, Long> longValue = Lens.of(
            DomainEntity::longValue,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), newValue, entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    public static final Lens<DomainEntity, Double> doubleValue = Lens.of(
            DomainEntity::doubleValue,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), newValue, entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    public static final Lens<DomainEntity, Boolean> booleanValue = Lens.of(
            DomainEntity::booleanValue,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), newValue, entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    public static final Lens<DomainEntity, String> stringValue = Lens.of(
            DomainEntity::stringValue,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), newValue,
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    // Optional lenses
    public static final Lens<DomainEntity, Optional<Integer>> optionalInt = Lens.of(
            DomainEntity::optionalInt,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                newValue, entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    public static final Lens<DomainEntity, Optional<Long>> optionalLong = Lens.of(
            DomainEntity::optionalLong,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), newValue, entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    public static final Lens<DomainEntity, Optional<Double>> optionalDouble = Lens.of(
            DomainEntity::optionalDouble,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), newValue, entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    public static final Lens<DomainEntity, Optional<Boolean>> optionalBoolean = Lens.of(
            DomainEntity::optionalBoolean,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), newValue, entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    public static final Lens<DomainEntity, Optional<String>> optionalString = Lens.of(
            DomainEntity::optionalString,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), newValue,
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
    );

    // List lenses
    public static final class IntListLens {
        public final Lens<DomainEntity, List<Integer>> lens = Lens.of(
            DomainEntity::intList,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                newValue, entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, Integer> get(int index) {
            return lens.andThen(ListLens.index(index));
        }
    }
    
    public static final class LongListLens {
        public final Lens<DomainEntity, List<Long>> lens = Lens.of(
            DomainEntity::longList,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), newValue, entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, Long> get(int index) {
            return lens.andThen(ListLens.index(index));
        }
    }
    
    public static final class DoubleListLens {
        public final Lens<DomainEntity, List<Double>> lens = Lens.of(
            DomainEntity::doubleList,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), newValue, entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, Double> get(int index) {
            return lens.andThen(ListLens.index(index));
        }
    }
    
    public static final class BooleanListLens {
        public final Lens<DomainEntity, List<Boolean>> lens = Lens.of(
            DomainEntity::booleanList,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), newValue, entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, Boolean> get(int index) {
            return lens.andThen(ListLens.index(index));
        }
    }
    
    public static final class StringListLens {
        public final Lens<DomainEntity, List<String>> lens = Lens.of(
            DomainEntity::stringList,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), newValue,
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, String> get(int index) {
            return lens.andThen(ListLens.index(index));
        }
    }
    
    public static final IntListLens intList = new IntListLens();
    public static final LongListLens longList = new LongListLens();
    public static final DoubleListLens doubleList = new DoubleListLens();
    public static final BooleanListLens booleanList = new BooleanListLens();
    public static final StringListLens stringList = new StringListLens();

    // Map lenses
    public static final class IntMapLens {
        public final Lens<DomainEntity, Map<String, Integer>> lens = Lens.of(
            DomainEntity::intMap,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                newValue, entity.longMap(), entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, Integer> key(String key) {
            return lens.andThen(MapLens.key(key));
        }
    }
    
    public static final class LongMapLens {
        public final Lens<DomainEntity, Map<String, Long>> lens = Lens.of(
            DomainEntity::longMap,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), newValue, entity.doubleMap(), entity.booleanMap(), entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, Long> key(String key) {
            return lens.andThen(MapLens.key(key));
        }
    }
    
    public static final class DoubleMapLens {
        public final Lens<DomainEntity, Map<String, Double>> lens = Lens.of(
            DomainEntity::doubleMap,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), newValue, entity.booleanMap(), entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, Double> key(String key) {
            return lens.andThen(MapLens.key(key));
        }
    }
    
    public static final class BooleanMapLens {
        public final Lens<DomainEntity, Map<String, Boolean>> lens = Lens.of(
            DomainEntity::booleanMap,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), newValue, entity.stringMap()
            )
        );
        
        public Lens<DomainEntity, Boolean> key(String key) {
            return lens.andThen(MapLens.key(key));
        }
    }
    
    public static final class StringMapLens {
        public final Lens<DomainEntity, Map<String, String>> lens = Lens.of(
            DomainEntity::stringMap,
            (entity, newValue) -> new DomainEntity(
                entity.intValue(), entity.longValue(), entity.doubleValue(), entity.booleanValue(), entity.stringValue(),
                entity.optionalInt(), entity.optionalLong(), entity.optionalDouble(), entity.optionalBoolean(), entity.optionalString(),
                entity.intList(), entity.longList(), entity.doubleList(), entity.booleanList(), entity.stringList(),
                entity.intMap(), entity.longMap(), entity.doubleMap(), entity.booleanMap(), newValue
            )
        );
        
        public Lens<DomainEntity, String> key(String key) {
            return lens.andThen(MapLens.key(key));
        }
    }
    
    public static final IntMapLens intMap = new IntMapLens();
    public static final LongMapLens longMap = new LongMapLens();
    public static final DoubleMapLens doubleMap = new DoubleMapLens();
    public static final BooleanMapLens booleanMap = new BooleanMapLens();
    public static final StringMapLens stringMap = new StringMapLens();
}
