package model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DomainEntityWithers {

    private DomainEntityWithers() {}

    public static DomainEntity withStringValue(DomainEntity entity, String stringValue) {
        return new DomainEntity(
            stringValue,
            entity.optionalString(),
            entity.stringList(),
            entity.stringMap(),
            entity.nested(),
            entity.optionalNested(),
            entity.nestedList(),
            entity.nestedMap(),
            entity.recursiveNested()
        );
    }

    public static DomainEntity withOptionalString(DomainEntity entity, Optional<String> optionalString) {
        return new DomainEntity(
            entity.stringValue(),
            optionalString,
            entity.stringList(),
            entity.stringMap(),
            entity.nested(),
            entity.optionalNested(),
            entity.nestedList(),
            entity.nestedMap(),
            entity.recursiveNested()
        );
    }

    public static DomainEntity withStringList(DomainEntity entity, List<String> stringList) {
        return new DomainEntity(
            entity.stringValue(),
            entity.optionalString(),
            stringList,
            entity.stringMap(),
            entity.nested(),
            entity.optionalNested(),
            entity.nestedList(),
            entity.nestedMap(),
            entity.recursiveNested()
        );
    }

    public static DomainEntity withStringMap(DomainEntity entity, Map<String, String> stringMap) {
        return new DomainEntity(
            entity.stringValue(),
            entity.optionalString(),
            entity.stringList(),
            stringMap,
            entity.nested(),
            entity.optionalNested(),
            entity.nestedList(),
            entity.nestedMap(),
            entity.recursiveNested()
        );
    }

    public static DomainEntity withNested(DomainEntity entity, Nested nested) {
        return new DomainEntity(
            entity.stringValue(),
            entity.optionalString(),
            entity.stringList(),
            entity.stringMap(),
            nested,
            entity.optionalNested(),
            entity.nestedList(),
            entity.nestedMap(),
            entity.recursiveNested()
        );
    }

    public static DomainEntity withOptionalNested(DomainEntity entity, Optional<Nested> optionalNested) {
        return new DomainEntity(
            entity.stringValue(),
            entity.optionalString(),
            entity.stringList(),
            entity.stringMap(),
            entity.nested(),
            optionalNested,
            entity.nestedList(),
            entity.nestedMap(),
            entity.recursiveNested()
        );
    }

    public static DomainEntity withNestedList(DomainEntity entity, List<Nested> nestedList) {
        return new DomainEntity(
            entity.stringValue(),
            entity.optionalString(),
            entity.stringList(),
            entity.stringMap(),
            entity.nested(),
            entity.optionalNested(),
            nestedList,
            entity.nestedMap(),
            entity.recursiveNested()
        );
    }

    public static DomainEntity withNestedMap(DomainEntity entity, Map<String, Nested> nestedMap) {
        return new DomainEntity(
            entity.stringValue(),
            entity.optionalString(),
            entity.stringList(),
            entity.stringMap(),
            entity.nested(),
            entity.optionalNested(),
            entity.nestedList(),
            nestedMap,
            entity.recursiveNested()
        );
    }

    public static DomainEntity withRecursiveNested(DomainEntity entity, RecursiveNested recursiveNested) {
        return new DomainEntity(
            entity.stringValue(),
            entity.optionalString(),
            entity.stringList(),
            entity.stringMap(),
            entity.nested(),
            entity.optionalNested(),
            entity.nestedList(),
            entity.nestedMap(),
            recursiveNested
        );
    }
}
