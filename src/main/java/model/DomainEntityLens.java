package model;

import lib.AbstractDomainLens;
import lib.Lens;
import lib.ListLens;
import lib.ListLensWrapper;
import lib.MapLens;
import lib.Mutations;
import lib.MapLensWrapper;
import lib.ObjectListLensWrapper;
import lib.ObjectMapLensWrapper;
import lib.OptionalLensWrapper;
import lib.ObjectOptionalLensWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class DomainEntityLens {

    public static Mutations.BoundMutations<DomainEntity> on(DomainEntity entity) { 
        return Mutations.forValue(entity); 
    }

    // Convenience methods for single operations
    public static <T> DomainEntity set(DomainEntity entity, Mutations.LensProvider<DomainEntity, T> lensProvider, T newValue) {
        return on(entity).set(lensProvider, newValue).apply();
    }

    public static <T> DomainEntity set(DomainEntity entity, Lens<DomainEntity, T> lens, T newValue) {
        return on(entity).set(lens, newValue).apply();
    }

    public static <T> DomainEntity mod(DomainEntity entity, Mutations.LensProvider<DomainEntity, T> lensProvider, UnaryOperator<T> modifier) {
        return on(entity).mod(lensProvider, modifier).apply();
    }

    public static <T> DomainEntity mod(DomainEntity entity, Lens<DomainEntity, T> lens, UnaryOperator<T> modifier) {
        return on(entity).mod(lens, modifier).apply();
    }

    public static Lens<DomainEntity, String> stringValue() {
        return Lens.of(
            DomainEntity::stringValue,
            (entity, newValue) -> new DomainEntity(
                newValue, entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested(), entity.nestedList(), entity.nestedMap(), entity.recursiveNested()
            )
        );
    }

    public static Lens<DomainEntity, Optional<String>> optionalString() {
        return Lens.of(
            DomainEntity::optionalString,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), newValue, entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested(), entity.nestedList(), entity.nestedMap(), entity.recursiveNested()
            )
        );
    }

    public static ListLensWrapper<DomainEntity, String> stringList() {
        return new ListLensWrapper<>(
            DomainEntity::stringList,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), newValue, entity.stringMap(), entity.nested(), entity.optionalNested(), entity.nestedList(), entity.nestedMap(), entity.recursiveNested()
            )
        );
    }

    public static MapLensWrapper<DomainEntity, String, String> stringMap() {
        return new MapLensWrapper<>(
            DomainEntity::stringMap,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), newValue, entity.nested(), entity.optionalNested(), entity.nestedList(), entity.nestedMap(), entity.recursiveNested()
            )
        );
    }
    

    public static NestedLens nested() {
        return new NestedLens(
            DomainEntity::nested,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), newValue, entity.optionalNested(), entity.nestedList(), entity.nestedMap(), entity.recursiveNested()
            )
        );
    }

    public static OptionalNestedLens optionalNested() {
        return new OptionalNestedLens(
            DomainEntity::optionalNested,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), newValue, entity.nestedList(), entity.nestedMap(), entity.recursiveNested()
            ),
            NestedLens::new
        );
    }

    public static ObjectListLensWrapper<DomainEntity, Nested, NestedLens> nestedList() {
        return new ObjectListLensWrapper<>(
            DomainEntity::nestedList,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested(), newValue, entity.nestedMap(), entity.recursiveNested()
            ),
            NestedLens::new
        );
    }

    public static ObjectMapLensWrapper<DomainEntity, String, Nested, NestedLens> nestedMap() {
        return new ObjectMapLensWrapper<>(
            DomainEntity::nestedMap,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested(), entity.nestedList(), newValue, entity.recursiveNested()
            ),
            NestedLens::new
        );
    }

    public static RecursiveNestedLens recursiveNested() {
        return RecursiveNestedLens.fromRequired(
            DomainEntity::recursiveNested,
            (entity, newValue) -> new DomainEntity(
                entity.stringValue(), entity.optionalString(), entity.stringList(), entity.stringMap(), entity.nested(), entity.optionalNested(), entity.nestedList(), entity.nestedMap(), newValue
            )
        );
    }

    // Inner lens classes
    
    public static class NestedLens extends AbstractDomainLens<DomainEntity, Nested> {
        
        public NestedLens(Lens<DomainEntity, Nested> lens) {
            super(lens);
        }

        public NestedLens(Function<DomainEntity, Nested> getter, BiFunction<DomainEntity, Nested, DomainEntity> setter) {
            super(getter, setter);
        }
        
        public Lens<DomainEntity, String> nestedValue() {
            return this.lens.andThen(Lens.of(
                Nested::nestedValue,
                (nested, newValue) -> new Nested(newValue, nested.moreNested())
            ));
        }
        
        public MoreNestedLens moreNested() {
            return new MoreNestedLens(
                entity -> lens.get(entity).moreNested(),
                (entity, newMoreNested) -> lens.set(entity, new Nested(lens.get(entity).nestedValue(), newMoreNested))
            );
        }
    }

    public static class MoreNestedLens extends AbstractDomainLens<DomainEntity, MoreNested> {
        
        public MoreNestedLens(Function<DomainEntity, MoreNested> getter, BiFunction<DomainEntity, MoreNested, DomainEntity> setter) {
            super(getter, setter);
        }
        
        public Lens<DomainEntity, String> moreNestedValue() {
            return this.lens.andThen(Lens.of(
                MoreNested::moreNestedValue,
                (moreNested, newValue) -> new MoreNested(newValue)
            ));
        }
    }

    public static class OptionalNestedLens extends ObjectOptionalLensWrapper<DomainEntity, Nested, NestedLens> {

        public OptionalNestedLens(
                final Function<DomainEntity, Optional<Nested>> getter,
                final BiFunction<DomainEntity, Optional<Nested>, DomainEntity> setter,
                final Function<Lens<DomainEntity, Nested>, NestedLens> lensCreator) {
            super(getter, setter, lensCreator);
        }

        public Lens<DomainEntity, String> nestedValue() {
            return createPropertyLens(Nested::nestedValue,
                                    (nested, newValue) -> new Nested(newValue, nested.moreNested()),
                                    "");
        }

        public MoreNestedLens moreNested() {
            return createNestedLens(
                Nested::moreNested,
                (nested, newMoreNested) -> new Nested(nested.nestedValue(), newMoreNested),
                MoreNestedLens::new
            );
        }
    }


    public static class RecursiveNestedLens extends AbstractDomainLens<DomainEntity, Optional<RecursiveNested>> {

        private RecursiveNestedLens(Lens<DomainEntity, Optional<RecursiveNested>> lens) {
            super(lens);
        }
        
        public static RecursiveNestedLens fromRequired(Function<DomainEntity, RecursiveNested> getter, BiFunction<DomainEntity, RecursiveNested, DomainEntity> setter) {
            return new RecursiveNestedLens(Lens.of(
                entity -> Optional.of(getter.apply(entity)),
                (entity, optValue) -> optValue.map(value -> setter.apply(entity, value)).orElse(entity)
            ));
        }
        
        public static RecursiveNestedLens fromOptional(Function<DomainEntity, Optional<RecursiveNested>> getter, BiFunction<DomainEntity, Optional<RecursiveNested>, DomainEntity> setter) {
            return new RecursiveNestedLens(Lens.of(getter, setter));
        }
        
        public Lens<DomainEntity, String> value() {
            return this.lens.andThen(Lens.of(
                opt -> opt.map(RecursiveNested::value).orElse(""),
                (opt, newValue) -> opt.map(nested -> new RecursiveNested(newValue, nested.child()))
            ));
        }
        
        public RecursiveNestedLens child() {
            return RecursiveNestedLens.fromOptional(
                entity -> lens.get(entity).flatMap(RecursiveNested::child),
                (entity, newChild) -> lens.set(entity, 
                    lens.get(entity).map(nested -> new RecursiveNested(nested.value(), newChild)))
            );
        }
    }

}